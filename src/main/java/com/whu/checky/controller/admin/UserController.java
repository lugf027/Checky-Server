package com.whu.checky.controller.admin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.plugins.Page;
import com.whu.checky.domain.User;
import com.whu.checky.mapper.UserMapper;
import com.whu.checky.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/admin/user")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/all")
    HashMap<String,Object> getAllUsers(@RequestBody String body){
        int page = JSON.parseObject(body).getInteger("page");
        Integer pageSize = JSON.parseObject(body).getInteger("pageSize");
        if(pageSize == null){
            pageSize = 5;
        }
        Page<User> p = new Page<User>(page, pageSize);
        boolean isAsc = false;

        List<User> userList = userService.getAllUsers(p, isAsc);
        HashMap<String,Object> resp = new HashMap<>();
        resp.put("state","ok");
        resp.put("size",(int)Math.floor(p.getTotal() / (double)pageSize));
        resp.put("total", p.getTotal());
        resp.put("users",userList);
        return resp;
    }

    @RequestMapping("/queryByKeyWord")
    HashMap<String,Object> queryUserByKeyWord(@RequestBody String body){
        JSONObject object= (JSONObject) JSON.parseObject(body).get("params");
        int page = object.getInteger("page");
        Integer pageSize = object.getInteger("pageSize");
        if(pageSize == null){
            pageSize = 5;
        }
        String keyWord=object.getString("keyword");
        List<User> userList=userService.queryUsers(page,keyWord, pageSize);
        HashMap<String,Object> resp = new HashMap<>();
        resp.put("state","ok");
        resp.put("users",userList);
        resp.put("size", (int)Math.ceil(userService.queryUsersNum(keyWord) / (double)pageSize));
        resp.put("total", userService.queryUsersNum(keyWord));
        return resp;
    }

    @PostMapping("/modify")
    HashMap<String,Object> modifyUser(@RequestBody String body){
        JSONObject json = JSON.parseObject(body);
//        String userId = json.getString("userId");
        User newUser = json.getObject("user",User.class);
        userService.updateUser(newUser);
        HashMap<String,Object> resp = new HashMap<>();
        resp.put("state","ok");
        return resp;
    }

    @PostMapping("/del")
    HashMap<String,Object> delUser(@RequestBody String body){
        String userId = JSONObject.parseObject(body).getString("userId");
        userService.deleteUser(userId);
        HashMap<String,Object> resp = new HashMap<>();
        resp.put("state","ok");
        return resp;
    }

    @PostMapping("/query")
    HashMap<String,Object> queryUser(@RequestBody String body){
        String userId = JSONObject.parseObject(body).getString("userId");
        User user = userService.queryUser(userId);
        HashMap<String,Object> resp = new HashMap<>();
        resp.put("state","ok");
        resp.put("user",user);
        return resp;
    }
}
