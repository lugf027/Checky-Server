package com.whu.checky.service;

import com.baomidou.mybatisplus.plugins.Page;
import com.whu.checky.domain.Essay;

import java.util.List;

public interface EssayService {
    //发布动态
    int addEssay(Essay essay);
    //修改动态
    int modifyEssay(Essay essay);
    //删除动态
    int deleteEssay(String essayId);
    //上传文件
    void uploadFile();
    //展示动态
    List<Essay> displayEssay(Page<Essay> page);
    //展示动态
    List<Essay> displayEssay(String targetUserId, Page<Essay> page);
    //展示动态
    List<Essay> queryUserEssays(String userId);
    //展示动态
    Essay queryEssayById(String essayId);

    //展示动态
    List<Essay> queryEssaysByUserName(String username);


    int updateEssay(Essay essay);

    List<Essay> queryEssaysAll(Page<Essay> p, String startTime, String endTime);

    List<Essay> queryEssaysLikeNickname(Page<Essay> p, String startTime, String endTime, String keyword);

    List<Essay> queryEssaysLikeContent(Page<Essay> p, String startTime, String endTime, String keyword);
}
