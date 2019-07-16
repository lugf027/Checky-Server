package com.whu.checky.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.whu.checky.domain.MoneyFlow;
import com.whu.checky.service.MoneyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MoneyController {
    @Autowired
    private MoneyService moneyService;

    @RequestMapping("/pay")
    public void pay(@RequestBody String jsonstr){
        //微信支付相关


        //成功之后在服务器数据库记录流水
        MoneyFlow moneyFlow= JSON.parseObject(jsonstr,new TypeReference<MoneyFlow>(){});
        int result=moneyService.addMoneyRecord(moneyFlow);
        if(result==1){
            //记录成功
        }else {
            //记录失败
        }
    }

    @RequestMapping("/payback")
    public void payback(){
        //进行分成算法
        //然后对每个监督者进行退款并且记录在数据库当中

    }

    @RequestMapping("/queryMoneyRecord")
    public List<MoneyFlow> queryMoneyRecord(@RequestBody String jsonstr){
        String userId= (String) JSON.parse(jsonstr);
        return moneyService.queryUserMoneyFlow(userId);
    }
}
