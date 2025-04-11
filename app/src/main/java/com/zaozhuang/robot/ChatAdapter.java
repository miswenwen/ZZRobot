package com.zaozhuang.robot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // 主视图类型
    private static final int TYPE_USER = 0;
    private static final int TYPE_BOT_TEXT = 1;
    private static final int TYPE_BOT_JOBS = 2;
    private static final int TYPE_BOT_POLICY = 3;
    final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        if (msg.getMainType() == ChatMessage.TYPE_USER) {
            return TYPE_USER;
        } else {
            // 根据机器人子类型返回不同视图类型
            switch (msg.getBotSubType()) {
                case ChatMessage.BOT_TYPE_JOBS:
                    return TYPE_BOT_JOBS;
                case ChatMessage.BOT_TYPE_POLICY:
                    return TYPE_BOT_POLICY;
                default: // 默认文本类型
                    return TYPE_BOT_TEXT;
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_BOT_JOBS:
                return new BotJobViewHolder(
                        inflater.inflate(R.layout.item_bot_jobs_message, parent, false)
                );
            case TYPE_BOT_POLICY:
                return new BotViewHolder(inflater.inflate(R.layout.item_bot_message, parent, false));
            case TYPE_BOT_TEXT:
                return new BotViewHolder(inflater.inflate(R.layout.item_bot_message, parent, false));
            default: // 用户消息
                return new UserViewHolder(inflater.inflate(R.layout.item_user_message, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind(message);
        } else if (holder instanceof BotViewHolder) {
            ((BotViewHolder) holder).bind(message);
        } else if (holder instanceof BotJobViewHolder) {
            ((BotJobViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // 保持原有ViewHolder结构，增加更新方法
    public void updateMessage(int position, String text) {
        messages.get(position).appendText(text);
        notifyItemChanged(position);
    }

    public void completeMessage(int position) {
        messages.get(position).setCompleted(true);
        notifyItemChanged(position);
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;

        UserViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }

        void bind(ChatMessage message) {
            tvMessage.setText(message.getText());
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;

        public BotViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }

        void bind(ChatMessage message) {
            tvMessage.setText(message.getText());
        }
    }

    static class BotJobViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;

        public BotJobViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }

        void bind(ChatMessage message) {
            tvMessage.setText(message.getText());
        }
    }
}
