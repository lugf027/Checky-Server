package com.whu.checky.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.whu.checky.domain.User;
import com.whu.checky.mapper.UserMapper;
import com.whu.checky.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("userService")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper mapper;

    @Override
    public boolean register(User user) {
        mapper.insert(user);
        return true;
    }

    @Override
    public void updateUser(User user) {
//        user.setSessionId(id);
        mapper.updateById(user);
//        return true;
    }

    @Override
    public User queryUser(String userId) {
        return mapper.selectById(userId);
    }

    @Override
    public void deleteUser(String userId) {
        mapper.deleteById(userId);
    }

    @Override
    public List<User> getAllUsers(int page, int pageSize) {
        return mapper.selectPage(new Page<User>(page, pageSize), new EntityWrapper<User>().orderBy("user_time"));
    }

    @Override
    public int getAllUsersNum() {
        return mapper.selectCount(new EntityWrapper<>());
    }

    @Override
    public List<User> queryUsers(int page, String keyword, int pageSize) {
        return mapper.selectPage(new Page<User>(page, pageSize), new EntityWrapper<User>().like("user_name", keyword
        ).orderBy("user_time"));
    }

    @Override
    public List<User> getUserListForMatch() {
        return mapper.selectList(new EntityWrapper<User>()
                .orderBy("supervise_num", true)
                .orderBy("supervise_num_min", false)
                .last("limit 70")
        );
    }

    @Override
    public List<User> queryUserByNickname(String nickname) {
        return mapper.selectList(new EntityWrapper<User>()
                .like("user_name", nickname)
                .orderBy("user_time", true)
        );
    }

    @Override
    public int queryUsersNum(String keyWord) {
        return mapper.selectCount(new EntityWrapper<User>()
                .like("user_name", keyWord));
    }
}
