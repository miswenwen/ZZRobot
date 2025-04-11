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
        holder.title_tv.setText(item.getTitle());
        holder.company_tv.setText(item.getCompany());
        holder.salary_tv.setText(item.getSalary());
        holder.guide_btn.setVisibility(item.isSupportsBoothGuidance()?View.VISIBLE:View.GONE);
        holder.remote_btn.setVisibility(item.isSupportsRemoteInterview()?View.VISIBLE:View.GONE);
        holder.deliver_btn.setVisibility(item.isSupportsResumeDelivery()?View.VISIBLE:View.GONE);

        holder.deliver_btn.setOnClickListener(v -> {
            Toast.makeText(parent.getContext(),
                    "投递成功", Toast.LENGTH_SHORT).show();
            holder.deliver_btn.setText("已投递");
        });

        return convertView;
    }

    class ViewHolder {
        TextView tvLeft;
        LinearLayout rightContainer;
        TextView title_tv;
        TextView company_tv;
        TextView salary_tv;
        TextView guide_btn;
        TextView remote_btn;
        TextView deliver_btn;

        ViewHolder(View view) {
            title_tv = view.findViewById(R.id.title);
            company_tv = view.findViewById(R.id.company);
            salary_tv = view.findViewById(R.id.salary);
            guide_btn = view.findViewById(R.id.guide_btn);
            remote_btn = view.findViewById(R.id.remote_btn);
            deliver_btn = view.findViewById(R.id.deliver_btn);

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
