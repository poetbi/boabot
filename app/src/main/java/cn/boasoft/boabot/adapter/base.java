package cn.boasoft.boabot.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public abstract class base<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private final LayoutInflater inflater;

    public base(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    public LayoutInflater getInflater() {
        return inflater;
    }

    public abstract void notifyDataSetChanged(List<Bundle> dataset);

}
