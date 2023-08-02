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

public class flow extends base<flow.ViewHolder> {
    private List<Bundle> dataset;

    public flow(Context context) {
        super(context);
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
        return new ViewHolder(getInflater().inflate(R.layout.item_flow, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(position, dataset.get(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView icon_view;
        private final TextView flow_view;

        public ViewHolder(View itemView) {
            super(itemView);
            icon_view = itemView.findViewById(R.id.icon);
            flow_view = itemView.findViewById(R.id.flow);
        }

        @SuppressLint("DefaultLocale")
        public void setData(int position, Bundle data) {
            String cmd = data.getString("cmd");
            String[] ctl = data.getString("ctl").split(",");
            int step = Integer.parseInt(ctl[2]);

            if(cmd.equals("goto")){
                icon_view.setImageResource(R.drawable.status_0);
            }else if(cmd.equals("check")){
                icon_view.setImageResource(R.drawable.status_1);
            }else{
                if (step == 1) {
                    icon_view.setImageResource(R.drawable.step_1);
                } else {
                    icon_view.setImageResource(R.drawable.step_0);
                }
            }

            String memo = data.getString("memo");
            flow_view.setText(String.format("%d. %s", (position + 1), memo));
        }
    }

}