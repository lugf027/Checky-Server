package com.whu.checky.controller.admin;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.plugins.Page;
import com.whu.checky.domain.MoneyFlow;
import com.whu.checky.service.MoneyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/admin")
@Component("AdminMoneyController")
public class MoneyController {

    @Autowired
    MoneyService moneyService;

    @PostMapping("/moneyFlows")
    public HashMap<String, Object> all(@RequestBody String body) {
        JSONObject json = JSONObject.parseObject(body);
        int page = json.getInteger("page");
        Page<MoneyFlow> p = null;

        if (page != -1) {
            p = new Page<>(page, 5);
        }

        HashMap<String, Object> resp = new HashMap<>();
        HashMap<String, String> params = new HashMap<>();
        if (json.containsKey("userId")) {
            resp.put("type", "userId");
            resp.put("moneyFlows", moneyService.queryUserMoneyFlow(json.getString("userId"), p));
        } else {
            resp.put("moneyFlows", moneyService.queryAllMoneyFlow(p));
            resp.put("type", "all");
        }
        if (p != null) resp.put("moneyFlowsSize", p.getTotal());
        resp.put("state", "ok");
        return resp;
    }

}
