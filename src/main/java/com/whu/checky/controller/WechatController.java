package com.whu.checky.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whu.checky.domain.User;
import com.whu.checky.service.ParameterService;
import com.whu.checky.service.RedisService;
import com.whu.checky.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/wechat")
public class WechatController {

    private String wxspAppid="wx5f1aa0197013dad6";
    private String wxspSecret="0b82e68c443bcc8ba76b3c9eeb327cf5";

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ParameterService parameterService;

    private static final Logger log = LoggerFactory.getLogger(WechatController.class);

    @PostMapping("/logout")
    public HashMap<String,Object> logout(@RequestBody String body){
        HashMap<String,Object> ans = new HashMap<>();
        try {
            JSONObject data = JSON.parseObject(body);
            String sessionKey = data.getString("sessionKey");
            redisService.delSessionId(sessionKey);
        }catch (Exception e){
            ans.put("state","fail");
        }
        return ans;
    }

    @PostMapping("/login")
    public HashMap<String,Object> login(@RequestBody String body) throws IOException {
        HashMap<String,Object> ret = new HashMap<>(); // 返回值
        JSONObject object= JSONObject.parseObject(body);
        String code=(String)object.get("code");
        JSONObject userinfo= (JSONObject) object.get("userInfo");
        RestTemplate restTemplate = new RestTemplate();// 发送request请求
        String params = "appid=" + wxspAppid + "&secret=" + wxspSecret + "&js_code=" +code+"&grant_type=authorization_code";//参数
        String url = "https://api.weixin.qq.com/sns/jscode2session?"+params;// 微信接口 用于查询oponid
        String response = restTemplate.getForObject(url,String.class);

        JsonNode node = this.mapper.readTree(response);
        String openid = node.get("openid").asText();
//        String sessionKey = node.get("session_key").asText(); // 微信返回的没用到

        double latitude = 0.0;
        double longitude = 0.0;
        try{
            latitude = Double.parseDouble(((JSONObject) object.get("location")).get("latitude").toString());
            longitude = Double.parseDouble(((JSONObject) object.get("location")).get("longitude").toString());
        }catch (Exception ex){
            log.warn("发现新用户登录，而此时小程序尚未获得经纬度返回值");
        }

        User user = new User();
        user.setUserId(openid);
        paserJson2User(userinfo,user);
        String skey = UUID.randomUUID().toString();
        user.setSessionId(skey);
        User check = userService.queryUser(openid);
        if(check==null){ // 新用户
            user.setLatitude(latitude);
            user.setLongtitude(longitude);
            userService.register(user);
            check = user;
        }else{ // 老用户
            redisService.delSessionId(check.getSessionId());
            updateFromWeixin(check,user);
            check.setLatitude(latitude);
            check.setLongtitude(longitude);
            userService.updateUser(check);
        }
        redisService.saveUserOrAdminBySessionId(skey,check);
        Boolean ifTrueMoneyAccess = Integer.
                parseInt(parameterService.getValueByParam("if_true_money_access").getParamValue()) != 0;
        Boolean ifNewTaskHighSettingAccess = Integer.
                parseInt(parameterService.getValueByParam("if_new_task_high_set").getParamValue()) != 0;
        ret.put("ifTrueMoneyAccess", ifTrueMoneyAccess);
        ret.put("ifNewTaskHighSettingAccess", ifNewTaskHighSettingAccess);
        ret.put("states",openid);
        ret.put("sessionKey",skey);
        ret.put("userGender", check.getUserGender());
        ret.put("userNickname", check.getUserName());
        if(check.getUserAvatar().substring(0, 11).equals("/resources/")){
            String baseIp = object.getString("baseIp");
            ret.put("userAvatar", baseIp + check.getUserAvatar());
        }else{
            ret.put("userAvatar", check.getUserAvatar());
        }
        return ret;
    }



    private void paserJson2User(JSONObject userinfo,User user){
        user.setUserName((String) userinfo.get("nickName"));
        user.setUserGender((Integer) userinfo.get("gender"));
        user.setUserAvatar((String) userinfo.get("avatarUrl"));
    }


    private void updateFromWeixin (User check, User user){
//        check.setUserName(user.getUserName());
//        check.setUserGender(user.getUserGender());
//        check.setUserAvatar(user.getUserAvatar());
        check.setLongtitude(user.getLongtitude());
        check.setLatitude(user.getLatitude());
        check.setSessionId(user.getSessionId());
    }


}
