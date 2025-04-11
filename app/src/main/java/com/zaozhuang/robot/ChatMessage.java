package com.zaozhuang.robot;

import java.util.Collections;
import java.util.List;

public class ChatMessage {
    // 主消息类型
    public static final int TYPE_USER = 0;
    public static final int TYPE_BOT = 1;

    // 机器人子类型
    public static final int BOT_TYPE_TEXT = 0;
    public static final int BOT_TYPE_JOBS = 1;
    public static final int BOT_TYPE_POLICY = 2;

    private final int mainType;
    private final int botSubType;
    private String content;//原始文本，整个的
    private String text;//动态的，用来模拟打字的效果，长度动态变化的
    private List<Job> jobs;
    private String policyId; // 新增政策卡片标识字段
    private boolean isCompleted;

    // 私有构造方法（强制使用工厂方法）
    private ChatMessage(int mainType, int botSubType,
                        String content, List<Job> jobs, String policyId) {
        this.mainType = mainType;
        this.botSubType = botSubType;
        this.content = content;
        this.text = "";
        this.jobs = jobs;
        this.policyId = policyId;
    }

    // 工厂方法
    public static ChatMessage createUserMessage(String text) {
        return new ChatMessage(TYPE_USER, -1, text, null, null);
    }

    public static ChatMessage createBotTextMessage(String text) {
        return new ChatMessage(TYPE_BOT, BOT_TYPE_TEXT, text, null, null);
    }

    public static ChatMessage createBotJobMessage(String text, List<Job> jobs) {
        return new ChatMessage(TYPE_BOT, BOT_TYPE_JOBS, text, jobs, null);
    }

    public static ChatMessage createBotPolicyMessage(String policyId) {
        return new ChatMessage(TYPE_BOT, BOT_TYPE_POLICY, null, null, policyId);
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    // 更新方法
    public void appendText(String newText) {
        this.text += newText;
    }

    public String getText() {
        return text;
    }

    public int getMainType() {
        return mainType;
    }

    public int getBotSubType() {
        return botSubType;
    }

    public String getContent() {
        return content;
    }

    public List<Job> getJobs() {
        return Collections.unmodifiableList(jobs);
    }

    public String getPolicyId() {
        return policyId;
    }
}

class Job {
    private final String title;
    private final String company;
    private final String location;
    private final String salary;

    public Job(String title, String company,
               String location, String salary) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.salary = salary;
    }

    // Getter 方法
    public String getTitle() {
        return title;
    }

    public String getCompany() {
        return company;
    }

    public String getLocation() {
        return location;
    }

    public String getSalary() {
        return salary;
    }
}
