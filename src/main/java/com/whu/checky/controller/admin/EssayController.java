package com.whu.checky.controller.admin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.plugins.Page;
import com.whu.checky.domain.Essay;
import com.whu.checky.domain.Record;
import com.whu.checky.domain.User;
import com.whu.checky.service.EssayService;
import com.whu.checky.service.RecordService;
import com.whu.checky.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/essay")
@Component("AdminEssayController")
public class EssayController {
    @Autowired
    private EssayService essayService;
    @Autowired
    private UserService userService;
    @Autowired
    private RecordService recordService;

    //假删除动态
    @RequestMapping("/delete")
    public JSONObject deleteEssayById(@RequestBody String jsonstr) {
        JSONObject res = new JSONObject();
        JSONObject object = (JSONObject) JSON.parse(jsonstr);
        String essayId = (String) object.get("essayId");
        Essay essay = essayService.queryEssayById(essayId);
        essay.setIfDelete(1);
        int deleteResult = essayService.updateEssay(essay);
        if (deleteResult == 1)
            res.put("state", "ok");
        else
            res.put("state", "fail");
        return res;

    }

    //展示动态
    @RequestMapping("/all")
    public JSONObject all(@RequestBody String jsonstr) {
        JSONObject res = new JSONObject();
        JSONObject object = (JSONObject) JSON.parse(jsonstr);
        int currentPage = (Integer) object.get("page");
        Integer pageSize = (Integer) object.get("pageSize");
        if(pageSize == null){
            pageSize = 5;
        }
        Page<Essay> page = new Page<Essay>(currentPage, pageSize);
        List<AdminEssay> adminEssays = new ArrayList<AdminEssay>();
        List<Essay> essays = essayService.displayEssay(page);
        for (Essay essay : essays) {
            List<Record> records = recordService.getRecordsByEssayId(essay.getEssayId());
            for (Record record : records) {
                if (!record.getRecordType().equals("text"))
                    record.setRecordType(record.getRecordType().substring(0, 5));
            }
            AdminEssay adminEssay = new AdminEssay();
            User user = userService.queryUser(essay.getUserId());
            adminEssay.setCommentNum(essay.getCommentNum());
            adminEssay.setEssayContent(essay.getEssayContent());
            adminEssay.setEssayId(essay.getEssayId());
            adminEssay.setEssayTime(essay.getEssayTime());
            adminEssay.setLikeNum(essay.getLikeNum());
            adminEssay.setUserName(user.getUserName());
            adminEssay.setImg(records);
            adminEssays.add(adminEssay);
        }
        res.put("state", "ok");
        res.put("essays", adminEssays);
        res.put("size", (int)Math.ceil(page.getTotal() / (double) pageSize));
        res.put("total", page.getTotal());
        return res;
    }

    //根据username模糊搜索的动态-->目前逻辑没有分页，无需size
    @RequestMapping("/query")
    public JSONObject query(@RequestBody String jsonstr) {
        JSONObject res = new JSONObject();
        JSONObject object = (JSONObject) JSON.parse(jsonstr);
        String username = object.getString("username");
        int page = object.getInteger("page");
        Integer pageSize = object.getInteger("pageSize");
        if(pageSize == null){
            pageSize = 10;
        }
        List<AdminEssay> adminEssays = new ArrayList<AdminEssay>();
        List<Essay> essays = essayService.queryEssaysByUserName(username);
        int end = Math.min((page + 1) * pageSize, essays.size());
        for (int i = page * pageSize; i< end; i++) {
            List<Record> records = recordService.getRecordsByEssayId(essays.get(i).getEssayId());
            for (Record record : records) {
                if (!record.getRecordType().equals("text"))
                    record.setRecordType(record.getRecordType().substring(0, 5));
            }
            AdminEssay adminEssay = new AdminEssay();
            User user = userService.queryUser(essays.get(i).getUserId());
            adminEssay.setCommentNum(essays.get(i).getCommentNum());
            adminEssay.setEssayContent(essays.get(i).getEssayContent());
            adminEssay.setEssayId(essays.get(i).getEssayId());
            adminEssay.setEssayTime(essays.get(i).getEssayTime());
            adminEssay.setLikeNum(essays.get(i).getLikeNum());
            adminEssay.setUserName(user.getUserName());
            adminEssay.setImg(records);
            adminEssays.add(adminEssay);
        }
        res.put("total", essays.size());
        res.put("size",(int)Math.ceil(essays.size() / (double)pageSize));
        res.put("state", "ok");
        res.put("essays", adminEssays);

        return res;
    }


    class AdminEssay {
        private int commentNum;
        private String essayContent;
        private String essayId;
        private String essayTime;
        private int likeNum;
        private String userName;
        private List<Record> img;

        public int getCommentNum() {
            return commentNum;
        }

        public void setCommentNum(int commentNum) {
            this.commentNum = commentNum;
        }

        public String getEssayContent() {
            return essayContent;
        }

        public void setEssayContent(String essayContent) {
            this.essayContent = essayContent;
        }

        public String getEssayId() {
            return essayId;
        }

        public void setEssayId(String essayId) {
            this.essayId = essayId;
        }

        public String getEssayTime() {
            return essayTime;
        }

        public void setEssayTime(String essayTime) {
            this.essayTime = essayTime;
        }

        public int getLikeNum() {
            return likeNum;
        }

        public void setLikeNum(int likeNum) {
            this.likeNum = likeNum;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public List<Record> getImg() {
            return img;
        }

        public void setImg(List<Record> img) {
            this.img = img;
        }
    }


}
