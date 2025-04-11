package com.zaozhuang.robot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class JobListAdapter extends BaseAdapter {
    final List<Job> jobs;

    public JobListAdapter(List<Job> jobs) {
        this.jobs = jobs;
    }

    @Override
    public int getCount() {
        return jobs.size();
    }

    @Override
    public Job getItem(int position) {
        return jobs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Job item = getItem(position);
        holder.tvLeft.setText(item.getCompany());

        // 设置右侧点击事件
        holder.rightContainer.setOnClickListener(v -> {
            Toast.makeText(parent.getContext(),
                    "点击项: " + item.getSalary(), Toast.LENGTH_SHORT).show();
        });

        return convertView;
    }

    class ViewHolder {
        TextView tvLeft;
        LinearLayout rightContainer;

        ViewHolder(View view) {
            tvLeft = view.findViewById(R.id.tv_left);
            rightContainer = view.findViewById(R.id.right_container);
        }
    }
    class DataItem {
        String content;

        DataItem(String content) {
            this.content = content;
        }
    }
}
// 数据模型
