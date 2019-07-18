package com.whu.checky.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.whu.checky.domain.Task;
import com.whu.checky.domain.TaskSupervisor;
import com.whu.checky.mapper.TaskMapper;
import com.whu.checky.mapper.TaskSupervisorMapper;
import com.whu.checky.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service("taskService")
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskSupervisorMapper taskSupervisorMapper;

    private  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    private Calendar cal = Calendar.getInstance();
    private int[] weekDays = {7,1,2,3,4,5,6};

    @Override
    public Integer addTask(Task task) {
        int weekNum=0;
        int res=0;
//        try {
//            Date dateStart = format.parse(task.getTaskStartTime());
//            Date dateEnd = format.parse(task.getTaskEndTime());
//            weekNum=(int)(dateEnd.getTime() - dateStart.getTime())/86400000/7;
//        }catch (Exception e){
//            e.printStackTrace();
//
//        }
        String temp=task.getCheckFrec();
        try {
            Date dateStart = format.parse(task.getTaskStartTime());
            Date dateEnd = format.parse(task.getTaskEndTime());
            weekNum=(int)((dateEnd.getTime() - dateStart.getTime())/86400000/7);
            Date startDate = format.parse(task.getTaskStartTime());
            cal.setTime(startDate);
            int w1 = cal.get(Calendar.DAY_OF_WEEK) - 1; // 开始日期是周几
            Date endDate = format.parse(task.getTaskEndTime());
            cal.setTime(endDate);
            int w2 = cal.get(Calendar.DAY_OF_WEEK) - 1; // 结束日期是周几
            if (w1!=w2){
                    while(w1!=w2){
                        if(temp.charAt(w1)=='1') res++;
                        w1=(w1+1)%7;
                    }
                }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        int Frec=0;
        for(int i=0;i<7;i++){
            if(temp.charAt(i)=='1') Frec++;
        }

        int checkTimes=weekNum*Frec;
        res+=checkTimes;
        task.setCheckTimes(res);
        return taskMapper.insert(task);
    }

    @Override
    public Integer delTask(String taskid) {
        return taskMapper.deleteById(taskid);
    }

    @Override
    public Integer updataTask(Task task) {
        return taskMapper.updateById(task);
    }

    @Override
    public List<Task> queryUserTasks(String userId, String date) {
        List<Task> res;
        if(date==null) res=taskMapper.selectList(new EntityWrapper<Task>().eq("user_id",userId));
        else {
            res = taskMapper.queryUserTasks(userId, date);
            try {
                Date tmpDate = format.parse(date);
                Calendar cal = Calendar.getInstance();
                //int[] weekDays = {7,1,2,3,4,5,6};
                cal.setTime(tmpDate);
                int w = cal.get(Calendar.DAY_OF_WEEK) - 1; // 指示一个星期中的某天。
                List<Task> removeList=new LinkedList<Task>();
                for (Task task:res){
                    if (task.getCheckFrec().charAt(w)!='1') {
                        removeList.add(task);
                    }
                }
                for (Task task:removeList){
                    res.remove(task);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return res;
//                taskMapper.selectList(new EntityWrapper<Task>().eq("user_id",userid).le("task_start_time",date)
//                .ge("task_end_time",date));
    }

    @Override
    public List<Task> listTasks() {
        return taskMapper.selectList(new EntityWrapper<Task>().orderBy("task_start_time"));
    }

    @Override
    public Task queryTask(String taskid) {
        return taskMapper.selectById(taskid);
    }

    @Override
    public HashMap <String, Double> distribute(String taskid) {
        HashMap<String, Double> result = new HashMap <>();
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        Task t = taskMapper.selectById(taskid);

        List<TaskSupervisor> supervisors = taskSupervisorMapper.selectList(new EntityWrapper <TaskSupervisor>().eq("task_id",taskid));
        supervisors.sort(Comparator.comparingInt(TaskSupervisor::getSuperviseNum));

        int length = supervisors.size();
        double money = t.getTaskMoney()*0.8;
        int superviseNum = 0;

        for (TaskSupervisor supvisor:supervisors) {
            superviseNum += supvisor.getSuperviseNum();
        }

        for (TaskSupervisor taskSupervisor : supervisors) {
            taskSupervisor.setBenefit(money * taskSupervisor.getSuperviseNum() / superviseNum);
            taskSupervisor.setRemoveTime(dateFormat.format(now));
            taskSupervisorMapper.update(taskSupervisor, new EntityWrapper <TaskSupervisor>()
                    .eq("task_id", taskSupervisor.getTaskId())
                    .and()
                    .eq("supervisor_id", taskSupervisor.getSupervisorId()));
            result.put(taskSupervisor.getSupervisorId(), money);
        }

        return result;
    }


}
