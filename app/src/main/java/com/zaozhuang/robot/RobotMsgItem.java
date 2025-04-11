package com.zaozhuang.robot;

public class RobotMsgItem {
    //回答文本
    String content;
    //type 0---政策问答 1---岗位推荐
    //岗位推荐的时候Recyclerview，机器人回答那里要加个ListView展示岗位
    int type = 0;

    RobotMsgItem(String content) {
        this.content = content;
    }
}
