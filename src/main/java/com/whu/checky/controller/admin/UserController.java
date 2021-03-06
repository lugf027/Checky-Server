package com.whu.checky.controller.admin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.plugins.Page;
import com.whu.checky.domain.User;
import com.whu.checky.service.HobbyService;
import com.whu.checky.service.MoneyService;
import com.whu.checky.service.UserService;
import com.whu.checky.util.MyConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/admin/user")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    MoneyService moneyService;

    @Autowired
    HobbyService hobbyService;

    @PostMapping("/queryHobby")
    HashMap<String, Object> queryHobby(@RequestBody String body){
        HashMap<String, Object> resp = new HashMap<>();
        JSONObject object = (JSONObject) JSON.parse(body);
        String userId = object.getString("userId");
        List<String> hobbies = hobbyService.queryUserHobby(userId);
        resp.put("hobbies", hobbies);
        resp.put("state", MyConstants.RESULT_OK);
        resp.put("total", hobbies.size());
        return resp;
    }


    @PostMapping("/all")
    HashMap<String, Object> getAllUsers(@RequestBody String body) {
        int page = JSON.parseObject(body).getInteger("page");
        Integer pageSize = JSON.parseObject(body).getInteger("pageSize");
        if (pageSize == null) {
            pageSize = 5;
        }
        Page<User> p = new Page<User>(page, pageSize);
        boolean isAsc = false;

        List<User> userList = userService.getAllUsers(p, isAsc);
        HashMap<String, Object> resp = new HashMap<>();
        resp.put("state", MyConstants.RESULT_OK);
        resp.put("size", (int) Math.floor(p.getTotal() / (double) pageSize));
        resp.put("total", p.getTotal());
        resp.put("users", userList);
        return resp;
    }

    @RequestMapping("/queryByKeyWord")
    HashMap<String, Object> queryUserByKeyWord(@RequestBody String body) {
        HashMap<String, Object> res = new HashMap<>();
        JSONObject object = (JSONObject) JSON.parse(body);
        String startTime = object.getString("startTime");
        startTime = startTime != null && !startTime.equals("") ? startTime : MyConstants.START_TIME;
        String endTime = object.getString("endTime");
        endTime = endTime != null && !endTime.equals("") ? endTime : MyConstants.END_TIME;

        String keyword = object.getString("keyword");
        String searchType = object.getString("searchType");
        Integer page = object.getInteger("page");
        Integer pageSize = object.getInteger("pageSize");
        Page<User> p = new Page<User>(page, pageSize);
        List<User> users = new ArrayList<User>();

        if(keyword == null || keyword.equals("")){
            users = userService.queryUsersAll(p, startTime, endTime);
        }
        else if(searchType.equals("nickname")){
            users = userService.queryUsersLikeNickname(p, startTime, endTime, keyword);
        }else{
            res.put("state", MyConstants.RESULT_FAIL);
            return res;
        }
        res.put("state", MyConstants.RESULT_OK);
        res.put("users", users);
        res.put("size", (int)Math.ceil(p.getTotal() / (double) pageSize));
        res.put("total", p.getTotal());
        return res;
    }

    @PostMapping("/modify")
    HashMap<String, Object> modifyUser(@RequestBody String body) {
        JSONObject json = JSON.parseObject(body);
//        String userId = json.getString("userId");
        User newUser = json.getObject("user", User.class);
        userService.updateUser(newUser);
        HashMap<String, Object> resp = new HashMap<>();
        resp.put("state", MyConstants.RESULT_OK);
        return resp;
    }

    @PostMapping("/del")
    HashMap<String, Object> delUser(@RequestBody String body) {
        String userId = JSONObject.parseObject(body).getString("userId");
        userService.deleteUser(userId);
        HashMap<String, Object> resp = new HashMap<>();
        resp.put("state", MyConstants.RESULT_OK);
        return resp;
    }

    @PostMapping("/query")
    HashMap<String, Object> queryUser(@RequestBody String body) {
        String userId = JSONObject.parseObject(body).getString("userId");
        User user = userService.queryUser(userId);
        HashMap<String, Object> resp = new HashMap<>();
        double[] userTotalMoneys =  moneyService.getUserTotalMoneys(userId);// totalTrueOut, totalTrueIn, totalTestOut, totalTestIn
        resp.put("totalTrueOut", userTotalMoneys[0]);
        resp.put("totalTrueIn", userTotalMoneys[1]);
        resp.put("totalTestOut", userTotalMoneys[2]);
        resp.put("totalTestIn", userTotalMoneys[3]);
        resp.put("state", MyConstants.RESULT_OK);
        resp.put("user", user);
        return resp;
    }
}
