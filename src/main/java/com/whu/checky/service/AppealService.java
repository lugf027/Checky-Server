package com.whu.checky.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.whu.checky.domain.Appeal;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AppealService {
    List<Appeal> getAllAppeals();
    //添加申诉
    boolean addAppeal(Appeal appeal);
    //撤销申诉
    boolean deleteAppeal(String appealId);
    //对申诉处理
    Appeal getAppealById(String appealId);
    Integer updateAppeal(Appeal appeal);
    //查询申诉
    List<Appeal> queryAppealFromUser(String userId);
    //管理员查看所有申诉
    List<Appeal> displayAppeals(Page<Appeal> page);
    //根据用户名检索
    List<Appeal> queryAppealByUserName(String username);

    List<Appeal> queryAppealsAll(Page<Appeal> p, String startTime, String endTime);

    List<Appeal> queryAppealsLikeNickname(Page<Appeal> p, String startTime, String endTime, String keyword);

    List<Appeal> queryAppealsLikeContent(Page<Appeal> p, String startTime, String endTime, String keyword);

//    int queryAllAppealNum();
}
