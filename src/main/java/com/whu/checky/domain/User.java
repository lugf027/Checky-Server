package com.whu.checky.domain;



import java.sql.Date;

public class User {
    //用户id
//    @Id
//    @GeneratedValue
//    private int userid;
//    //用户微信名
//    @Column(nullable = false)
//    private String nickname;
//    //用户微信头像url
//    @Column(nullable = false)
//    private String avatar;
//    //用户性别
//    @Column(nullable = false)
//    private String gender;
//    //用户使用小程序的时间
//    @Column(nullable = false)
//    private Date signindate;
//    //地理位置

    private String sessionID;

    public String getSessionID() {
        return sessionID;
    }

    //    private String openid;
//
    public void setSessionID(String id){
        sessionID=id;
    }



}
