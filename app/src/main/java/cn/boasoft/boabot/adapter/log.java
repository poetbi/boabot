package cn.boasoft.boabot.adapter;

import static cn.boasoft.boabot.library.tool.time2str;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.boasoft.boabot.R;

public class log extends base<log.ViewHolder> {
    private List<Bundle> dataset;

    public log(Context context) {
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
        return new ViewHolder(getInflater().inflate(R.layout.item_log, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(dataset.get(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView logid_view;
        private final TextView title_view;
        private final TextView time_view;

        public ViewHolder(View itemView) {
            super(itemView);
            logid_view = itemView.findViewById(R.id.logid);
            title_view = itemView.findViewById(R.id.title);
            time_view = itemView.findViewById(R.id.time);
        }

        public void setData(Bundle data) {
            long id = data.getLong("id");
            long fid = data.getLong("fid");

            String title;
            if(fid > 0){ //工作流执行
				int res = data.getInt("res");
                title = data.getString("memo") + "("+ res +")";
            }else{ //系统事件
                title = data.getString("val");
            }

            String time = time2str("MM/dd HH:mm:ss", data.getLong("time"));

            logid_view.setText(String.valueOf(id));
            title_view.setText(title);
            time_view.setText(time);
        }
    }
}