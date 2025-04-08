package com.zaozhuang.robot;

// ChatMessage.java
public class ChatMessage {
    private String text;
    private final boolean isBot;
    private boolean isCompleted;

    public ChatMessage(boolean isBot) {
        this.isBot = isBot;
        this.text = "";
    }

    // 更新方法
    public void appendText(String newText) {
        this.text += newText;
    }

    // getters & setters
    public String getText() {
        return text;
    }

    public boolean isBot() {
        return isBot;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
