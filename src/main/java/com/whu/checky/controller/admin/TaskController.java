package com.whu.checky.controller.admin;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.plugins.Page;
import com.whu.checky.domain.*;
import com.whu.checky.mapper.TaskSupervisorMapper;
import com.whu.checky.service.*;
import com.whu.checky.util.MyConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/admin/task")
@Component("AdminTaskController")

public class TaskController {

    @Autowired
    private TaskService taskService;
    @Autowired
    private CheckService checkService;
    @Autowired
    private TaskTypeService taskTypeService;
    @Autowired
    private UserService userService;
    @Autowired
    private TaskSupervisorMapper taskSupervisorMapper;
    @Autowired
    private SuperviseService superviseService;
    @Autowired
    private RecordService recordService;

    //    @PostMapping("/all")
//    public JSONObject all(@RequestBody String body) {
//
//    }
    @PostMapping("/tasks")
    public HashMap<String, Object> all(@RequestBody String body) {
        JSONObject json = JSONObject.parseObject(body);
        int page = json.getInteger("page");
        Integer pageSize = json.getInteger("pageSize");
        if(pageSize == null){
            pageSize = 5;
        }
        Page<Task> p = null;
        if (page != -1) {
            p = new Page<>(page, pageSize);
        }
        HashMap<String, Object> resp = new HashMap<>();
        HashMap<String, String> params = new HashMap<>();
        if (json.containsKey("userId")) {
            params.put("user_id", json.getString("userId"));
            resp.put("type", "userId");
        }
        List<Task> taskList = taskService.query(params, p);
        for (Task task : taskList) {
            task.setTypeContent(taskTypeService.queryTaskType(task.getTypeId()).getTypeContent());
        }
        resp.put("tasks", taskList);
        if (p != null) {
            resp.put("size", (int)Math.ceil(p.getTotal() / (double) pageSize));
            resp.put("total", p.getTotal());
        }
        resp.put("state", MyConstants.RESULT_OK);
        return resp;
    }

    //查看任務詳情
    @PostMapping("/detail")
    public JSONObject detail(@RequestBody String body) {
        JSONObject res = new JSONObject();
        JSONObject object = (JSONObject) JSON.parse(body);
        String taskId = (String) object.get("taskId");
        Task task = taskService.queryTask(taskId);
        task.setTypeContent(taskTypeService.queryTaskType(task.getTypeId()).getTypeContent());
        res.put("state", MyConstants.RESULT_OK);
        res.put("task", task);
        return res;
    }

    //查看打卡詳情
    @PostMapping("/check")
    public JSONObject check(@RequestBody String body) {
        JSONObject res = new JSONObject();
        JSONObject object = (JSONObject) JSON.parse(body);
        String taskId = (String) object.get("taskId");
        List<Check> checks = checkService.getTaskChecks(taskId);
        res.put("state", MyConstants.RESULT_OK);
        res.put("checks", checks);
        return res;
    }

    @RequestMapping("/query")
    public JSONObject query(@RequestBody String jsonstr) {
        JSONObject res = new JSONObject();
        JSONObject object = (JSONObject) JSON.parse(jsonstr);
        String startTime = object.getString("startTime");
        startTime = startTime != null && !startTime.equals("") ? startTime : MyConstants.START_TIME;
        String endTime = object.getString("endTime");
        endTime = endTime != null && !endTime.equals("") ? endTime : MyConstants.END_TIME;

        String keyword = object.getString("keyword");
        String searchType = object.getString("searchType");
        Integer page = object.getInteger("page");
        Integer pageSize = object.getInteger("pageSize");

        Page<Task> p = new Page<>(page, pageSize);
        List<Task> tasks = new ArrayList<Task>();
        if(keyword == null || keyword.equals("")){
            tasks = taskService.queryTaskAll(p, startTime, endTime);
        }
        else if(searchType.equals("nickname")){
            tasks = taskService.queryTaskLikeNickname(p, startTime, endTime, keyword);
        }else if(searchType.equals("content")){
            tasks = taskService.queryTaskLikeContent(p, startTime, endTime, keyword);
        }else if(searchType.equals("title")){
            tasks = taskService.queryTaskLikeTitle(p, startTime, endTime, keyword);
        }else{
            res.put("state", MyConstants.RESULT_FAIL);
            return res;
        }
        res.put("state", MyConstants.RESULT_OK);
        res.put("tasks", tasks);
        res.put("size", (int)Math.ceil(p.getTotal() / (double) pageSize));
        res.put("total", p.getTotal());
        return res;
    }

    //查看打卡詳情
    @PostMapping("/check/detail")
    public JSONObject checkDetail(@RequestBody String body) {
        JSONObject res = new JSONObject();
        JSONObject object = (JSONObject) JSON.parse(body);
        String checkId = (String) object.get("checkId");
        String taskId = (String) object.get("taskId");
        AdminCheckDetail adminCheckDetail = new AdminCheckDetail();
        //打卡详情
        Check check = checkService.queryCheckById(checkId);
        List<Record> records = recordService.getRecordsByCheckId(check.getCheckId());
        Record textRecord = null;
        for (Record record : records) {
            if (record.getRecordType().equals(MyConstants.RECORD_TYPE_TEXT)) {
                textRecord = record;
            }
        }
        records.remove(textRecord);
        CheckHistory checkHistory = new CheckHistory();
        check.setTaskTitle(taskService.getTitleById(check.getTaskId()));
        checkHistory.setCheck(check);
        checkHistory.setImages(records);
        checkHistory.setText(textRecord);
        adminCheckDetail.setCheckHistory(checkHistory);
        User user = userService.queryUser(check.getUserId());
        adminCheckDetail.setUserName(user.getUserName());
        //监督详情
        List<SupervisorState> supervisorStates = superviseService.querySuperviseState(taskId, checkId);
        adminCheckDetail.setSupervisorStates(supervisorStates);
        res.put("state", MyConstants.RESULT_OK);
        res.put("adminCheckDetail", adminCheckDetail);
        return res;
    }


    //查看监督者
    @PostMapping("/supervisors")
    public JSONObject supervisors(@RequestBody String body) {
        JSONObject res = new JSONObject();
        JSONObject object = (JSONObject) JSON.parse(body);
        String taskId = (String) object.get("taskId");
        List<String> supervisorsId = taskSupervisorMapper.getTaskSupervisors(taskId);
        List<AdminTaskSupervisor> adminTaskSupervisors = new ArrayList<AdminTaskSupervisor>();
        for (String supervisorId : supervisorsId) {
            AdminTaskSupervisor adminTaskSupervisor = new AdminTaskSupervisor();
            User user = userService.queryUser(supervisorId);
            TaskSupervisor taskSupervisor = taskSupervisorMapper.getTaskSupervisor(taskId, supervisorId);
            adminTaskSupervisor.setAddTime(taskSupervisor.getAddTime());
            adminTaskSupervisor.setBenefit(taskSupervisor.getBenefit());
            adminTaskSupervisor.setSuperviseNum(taskSupervisor.getSuperviseNum());
            adminTaskSupervisor.setUserId(supervisorId);
            adminTaskSupervisor.setUserName(user.getUserName());
            adminTaskSupervisors.add(adminTaskSupervisor);
        }
        res.put("state", MyConstants.RESULT_OK);
        res.put("supervisors", adminTaskSupervisors);
        return res;
    }

    class AdminTaskSupervisor {
        private String addTime;
        private double benefit;
        private int superviseNum;
        private String userId;
        private String userName;

        public String getAddTime() {
            return addTime;
        }

        public void setAddTime(String addTime) {
            this.addTime = addTime;
        }

        public double getBenefit() {
            return benefit;
        }

        public void setBenefit(double benefit) {
            this.benefit = benefit;
        }

        public int getSuperviseNum() {
            return superviseNum;
        }

        public void setSuperviseNum(int superviseNum) {
            this.superviseNum = superviseNum;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }


    class AdminCheckDetail {
        private CheckHistory checkHistory;
        private List<SupervisorState> supervisorStates;
        private String userName;

        public CheckHistory getCheckHistory() {
            return checkHistory;
        }

        public void setCheckHistory(CheckHistory checkHistory) {
            this.checkHistory = checkHistory;
        }

        public List<SupervisorState> getSupervisorStates() {
            return supervisorStates;
        }

        public void setSupervisorStates(List<SupervisorState> supervisorStates) {
            this.supervisorStates = supervisorStates;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }

    class CheckHistory {
        private Check check;
        private List<Record> images;
        private Record text;

        public Check getCheck() {
            return check;
        }

        public void setCheck(Check check) {
            this.check = check;
        }


        public List<Record> getImages() {
            return images;
        }

        public void setImages(List<Record> images) {
            this.images = images;
        }

        public Record getText() {
            return text;
        }

        public void setText(Record text) {
            this.text = text;
        }
    }


}
