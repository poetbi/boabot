package cn.boasoft.boabot.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.boasoft.boabot.R;

public class task extends base<task.ViewHolder> {
    private List<Bundle> dataset;
    private final Context context;

    public task(Context context) {
        super(context);
        this.context = context;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void notifyDataSetChanged(List<Bundle> dataset) {
        this.dataset = dataset;
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return dataset == null ? 0 : dataset.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(context, getInflater().inflate(R.layout.item_task, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(dataset.get(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final Context context;
        private final ImageView icon_view;
        private final TextView name_view;
        private final TextView exec_view;

        public ViewHolder(Context context, View itemView) {
            super(itemView);
            this.context = context;
            icon_view = itemView.findViewById(R.id.icon);
            name_view = itemView.findViewById(R.id.name);
            exec_view = itemView.findViewById(R.id.exec);
        }

        @SuppressLint("DefaultLocale")
        public void setData(Bundle data) {
            String text;

            int status = data.getInt("status");
            if (status == 1) {
                icon_view.setImageResource(R.drawable.status_1);
            } else {
                icon_view.setImageResource(R.drawable.status_0);
            }

            long tid = data.getLong("id");
            String name = data.getString("name");
            name_view.setText(String.format("%d. %s", tid, name));

            int type = data.getInt("type");
            String[] type_v = context.getResources().getStringArray(R.array.type_v);
            text = type_v[type] + " : ";

            switch (type) {
                case 0 :
                    text += "boa://bot/?tid="+ tid +"&val=boabot";
                    break;

                case 1 :
                    String[] arr = data.getString("exec").split(",");
                    int month = Integer.parseInt(arr[4]);
                    int week = Integer.parseInt(arr[3]);
                    int day = Integer.parseInt(arr[2]);
                    int hour = Integer.parseInt(arr[1]);
                    int minute = Integer.parseInt(arr[0]);

                    if(month < 0){
                        text += context.getString(R.string.per_month);
                    }else{
                        text += (month + 1) + context.getString(R.string.unit_month);
                    }

                    if(week < 0 && day < 0){
                        text += context.getString(R.string.per_day);
                    }else{
                        if(week >= 0){
                            String[] week_v = context.getResources().getStringArray(R.array.week_v);
                            text += week_v[week];
                        }else{
                            text += (day + 1) + context.getString(R.string.unit_day);
                        }
                    }

                    if(hour < 0){
                        text += context.getString(R.string.per_hour);
                    }else{
                        text += hour + context.getString(R.string.unit_hour);
                    }

                    text += minute + context.getString(R.string.unit_minute);
                    break;

                case 2 :
                    text += context.getString(R.string.task_set_exec);
                    break;
            }

            exec_view.setText(text);
        }
    }

}