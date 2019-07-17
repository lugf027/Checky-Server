package com.whu.checky.util;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.whu.checky.domain.Check;
import com.whu.checky.domain.Supervise;
import com.whu.checky.domain.Task;
import com.whu.checky.mapper.CheckMapper;
import com.whu.checky.mapper.SuperviseMapper;
import com.whu.checky.mapper.TaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

//仲裁模块
@Component
public class Judge {

    @Value("${jobs.match.maxNum}")
    int matchMax;

    @Value("${jobs.judge.timeoutDay}")
    int timeoutDay;

    @Autowired
    TaskMapper taskMapper;

    @Autowired
    CheckMapper checkMapper;

    @Autowired
    SuperviseMapper superviseMapper;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


//    业务逻辑
//    取出所有未判断的每日打卡记录
//    若监督的数量已足够则进行判定
//    否则判断是否过期（过了一定的天数还没有足够的监督人数
//    若过期则从监督人数通过是否过半进行判定，无人监督则算通过
    @Scheduled(cron = "${jobs.judge.cron}")
    public void dailyCheckSupervisedJudge(){
        System.out.println("Task start!");
        List<Check> checkList = checkMapper.selectList(new EntityWrapper<Check>()
                .eq("check_state","unknown")
        );

        for(Check c: checkList){
            try {
                int supervisorNum = taskMapper.selectById(c.getTaskId()).getSupervisorNum();
                double timeDiff = (new Date().getTime() - sdf.parse(c.getCheckTime()).getTime()) / (1000 * 60 * 60 * 24);//超过一定天数后监督人数不足自动判定
                if(c.getSuperviseNum()==supervisorNum||timeDiff>=timeoutDay) {
                    if (c.getPassNum() * 2 >= c.getSuperviseNum()) c.setCheckState("pass");
                    else c.setCheckState("deny");

                    checkMapper.updateById(c);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

//            List<Supervise> superviseList = superviseMapper.selectList(new EntityWrapper<Supervise>()
//                .eq("check_id",c.getCheckId())
//            );
//            int supervisorNum = taskMapper.selectById(c.getTaskId()).getSupervisorNum();
//            if(superviseList.size()==supervisorNum){
//                int count=0;
//                for(Supervise s:superviseList){
//                    if(s.getSuperviseState().equals("pass")) count++;
//                }
//                if(count*2>c.getSuperviseNum()){
//                    c.setCheckState("pass");
//                }else{
//                    c.setCheckState("deny");
//                }
//                checkMapper.updateById(c);
//            }
//            try {
//                double timeDiff = (new Date().getTime() - sdf.parse(c.getCheckTime()).getTime()) / (1000 * 60 * 60 * 24);//超过一定天数后监督人数不足自动判定
//                if(timeDiff>=timeoutDay){
//                    int count=0;
//                    for(Supervise s:superviseList){
//                        if(s.getSuperviseState().equals("pass")) count++;
//                    }
//                    if(count==0) c.setCheckState("pass");
//                    else if(count*2>=superviseList.size()) c.setCheckState("pass");
//                    else c.setCheckState("deny");
//                    checkMapper.updateById(c);
//                }
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
        }



}
