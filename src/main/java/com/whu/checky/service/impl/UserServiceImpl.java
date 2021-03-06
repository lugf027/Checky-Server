package com.whu.checky.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.whu.checky.domain.User;
import com.whu.checky.mapper.UserMapper;
import com.whu.checky.service.UserService;

import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service("userService")
public class UserServiceImpl implements UserService {

    private static final Random RANDOM = new Random();

    @Autowired
    private UserMapper mapper;

    @Override
    public Integer register(User user) {
        return mapper.insert(user);
    }

    @Override
    public Integer updateUser(User user) {
//        user.setSessionId(id);
        return mapper.updateById(user);
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
    public List<User> getAllUsers(Page<User> page, boolean isAsc) {
        return mapper.selectPage(page, new EntityWrapper<User>()
                .orderBy("user_time", isAsc));
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

    @Override
    public List<User> queryUsersWithPage(Page<User> p, String keyWord) {
        return mapper.selectPage(p, new EntityWrapper<User>().like("user_name", keyWord
        ).orderBy("user_time", false));
    }

    @Override
    public List<User> queryUsersWithPage(Page<User> p) {
        return mapper.selectPage(p, new EntityWrapper<User>().orderBy("user_time", false));
    }

    @Override
    public List<User> getUsersRandomly(int maxNumUsersNeed, String userSelfId) {
//        int cnt = mapper.selectCount(null);
//        int offset = RANDOM.nextInt(cnt-maxNumUsersNeed+1);
//        return mapper.selectPage(new RowBounds(offset, maxNumUsersNeed), new EntityWrapper<User>()
//                .ne("user_id", "System")
//                .ne("user_id", userSelfId));\
        return mapper.selectPage(new Page<User>(0, maxNumUsersNeed), new EntityWrapper<User>()
                .ne("user_id", "System")
                .ne("user_id", userSelfId)
                .orderBy("RAND()"));
    }

    @Override
    public List<User> queryUsersAll(Page<User> p, String startTime, String endTime) {
        return mapper.selectPage(p, new EntityWrapper<User>()
                .ge("user_time", startTime)
                .le("user_time", endTime)
                .orderBy("user_time", false));
    }

    @Override
    public List<User> queryUsersLikeNickname(Page<User> p, String startTime, String endTime, String keyword) {
        return mapper.selectPage(p, new EntityWrapper<User>()
                .like("user_name", keyword)
                .ge("user_time", startTime)
                .le("user_time", endTime)
                .orderBy("user_time", false));
    }
}
