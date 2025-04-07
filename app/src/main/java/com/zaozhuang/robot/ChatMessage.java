package com.zaozhuang.robot;

public class ChatMessage {
    private final String text;
    private final boolean isBot;

    public ChatMessage(String text, boolean isBot) {
        this.text = text;
        this.isBot = isBot;
    }

    public String getText() {
        return text;
    }

    public boolean isBot() {
        return isBot;
    }
}
