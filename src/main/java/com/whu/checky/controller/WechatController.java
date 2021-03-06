package com.whu.checky.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whu.checky.config.UploadConfig;
import com.whu.checky.domain.MoneyFlow;
import com.whu.checky.domain.User;
import com.whu.checky.service.MoneyService;
import com.whu.checky.service.ParameterService;
import com.whu.checky.service.RedisService;
import com.whu.checky.service.UserService;
import com.whu.checky.util.MyConstants;
import com.whu.checky.util.MyStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/wechat")
public class WechatController {

    private String wxspAppid = "wx5f1aa0197013dad6";
    private String wxspSecret = "0b82e68c443bcc8ba76b3c9eeb327cf5";
    private String wxspAPI = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ParameterService parameterService;
    @Autowired
    private UploadConfig uploadConfig;

    @Autowired
    private MoneyService moneyService;

    private static final Logger log = LoggerFactory.getLogger(WechatController.class);

    @PostMapping("/logout")
    public HashMap<String, Object> logout(@RequestBody String body) {
        HashMap<String, Object> ans = new HashMap<>();
        try {
            JSONObject data = JSON.parseObject(body);
            String sessionKey = data.getString("sessionKey");
            redisService.delSessionId(sessionKey);
        } catch (Exception e) {
            ans.put("state", MyConstants.RESULT_FAIL);
        }
        return ans;
    }

    /**
     *
     * @param body
     * @return state: "fail" fail code->openId
     *          state: "insertFail" fail register new user
     *          state: "updateFail" fail update exiting userInfo
     */
    @PostMapping("/login")
    public HashMap<String, Object> login(@RequestBody String body){
        HashMap<String, Object> ret = new HashMap<>(); // 返回值
        JSONObject object = JSONObject.parseObject(body);

        String openId = this.getOpenIdByCode(object.getString("code"));
        if (openId == null) {
            ret.put("state", MyConstants.RESULT_FAIL);
            log.info("openId is null");
            return ret;
        }
        User user = userService.queryUser(openId);

        if (user == null) { // 新用户注册
            user = this.buildNewUser(object, openId);
            if (userService.register(user) != 1) {
                ret.put("state", MyConstants.RESULT_INSERT_FAIL);
                return ret;
            }
            MoneyFlow moneyFlow = new MoneyFlow();
            moneyFlow.setIfTest(1);
            moneyFlow.setUserID(user.getUserId());
            moneyFlow.setFlowMoney(user.getTestMoney());
            moneyFlow.setFlowTime(user.getUserTime());
            moneyFlow.setFlowIo(MyConstants.MONEY_FLOW_IN);
            moneyFlow.setFlowId(UUID.randomUUID().toString());
            moneyFlow.setFlowType(MyConstants.MONEY_FLOW_TYPE_INIT);
            moneyService.addTestMoneyRecord(moneyFlow);
        } else {  // 老用户登录
            redisService.delSessionId(user.getSessionId());
            log.info("user " + user.getUserId() + "/" + user.getUserName() + " logined at " + new Date());
            this.updateOldUser(user, object);
            if (userService.updateUser(user) != 1) {
                ret.put("state", MyConstants.RESULT_UPDATE_FAIL);
                return ret;
            }
        }
        redisService.saveUserOrAdminBySessionId(user.getSessionId(), user);

        Boolean ifTrueMoneyAccess = Integer.
                parseInt(parameterService.getValueByParam("if_true_money_access").getParamValue()) != 0;
        Boolean ifNewTaskHighSettingAccess = Integer.
                parseInt(parameterService.getValueByParam("if_new_task_high_set").getParamValue()) != 0;
        ret.put("ifTrueMoneyAccess", ifTrueMoneyAccess);
        ret.put("ifNewTaskHighSettingAccess", ifNewTaskHighSettingAccess);

        ret.put("state", MyConstants.RESULT_OK);
        ret.put("openId", user.getUserId());
        ret.put("sessionKey", user.getSessionId());
        ret.put("userGender", user.getUserGender());
        ret.put("userNickname", user.getUserName());
        String userAvatar = "";
        if (!MyStringUtil.isEmpty(user.getUserAvatar()) && user.getUserAvatar().length()>11) {
            userAvatar  =   user.getUserAvatar().substring(0, 11).equals("/" + uploadConfig.getStaticPath() + "/") ?
                    object.getString("baseIp") + user.getUserAvatar() : user.getUserAvatar();
        }
        ret.put("userAvatar", userAvatar);

        return ret;
    }

    private String getOpenIdByCode(String code) {
        String response = "";
        try {
            RestTemplate restTemplate = new RestTemplate();
            String params = "?appid=" + wxspAppid + "&secret=" + wxspSecret + "&js_code=" + code + "&grant_type=authorization_code";
            String url = wxspAPI + params;
            response = restTemplate.getForObject(url, String.class);

            JsonNode node = this.mapper.readTree(response);
            return node.get("openid").asText();
        } catch (Exception ex) {
            log.error("Something Wrong When Get OpenId from code: " + code + "\n" + ex.getMessage() + "\n" +
                    " the real response is: " + response);
            return null;
        }
    }

    private User buildNewUser(JSONObject object, String openid) {
        User user = new User();
        user.setUserId(openid);
        String testMoneyPar = "test_money";
        user.setTestMoney(Double.parseDouble(parameterService.getValueByParam(testMoneyPar).getParamValue()));
        user.setUserTime(MyConstants.DATETIME_FORMAT.format(new Date()));

        JSONObject userInfo = (JSONObject) object.get("userInfo");
        // 基本的userInfo，考虑到用户在本小程序内更换昵称、性别与头像等，应该一次保存后不再同步微信的
        if (userInfo != null) {
            user.setUserName(userInfo.getString("nickName"));
            user.setUserGender(userInfo.getInteger("gender"));
            user.setUserAvatar(userInfo.getString("avatarUrl"));
        }

        this.updateOldUser(user, object);
        return user;
    }

    private void updateOldUser(User user, JSONObject object) {
        // 经纬度
        try{
            JSONObject location = (JSONObject) object.get("location");
            user.setLatitude(location.getBigDecimal("latitude").doubleValue());
            user.setLongtitude(location.getBigDecimal("longitude").doubleValue());
        } catch (Exception ex){
            log.warn("user is logging in without location info\n" + ex.getMessage());
        }
        // sessionId
        String userSessionId = UUID.randomUUID().toString();
        user.setSessionId(userSessionId);
    }
}
