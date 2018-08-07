package com.example.jake1.designproject;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class DirectionsRecyclerViewAdapter extends RecyclerView.Adapter<DirectionsRecyclerViewAdapter.DirectionsViewHolder> {

    private List<String> mData;
    private LayoutInflater mInflater;

    DirectionsRecyclerViewAdapter(Context context, List<String> data) {
        this.mInflater= LayoutInflater.from(context);
        this.mData= data;
    }

    @NonNull
    @Override
    public DirectionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_directionview, parent, false);
        return new DirectionsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DirectionsViewHolder holder, int position) {
        String DirectionsString = mData.get(position);
        holder.DirectionView.setText(DirectionsString);
    }

    @Override
    public int getItemCount() {
        return mData.size();

    }

    public void setData(List<String> newData) {
        mData = newData;
        notifyDataSetChanged();
    }

    public class DirectionsViewHolder extends RecyclerView.ViewHolder{

        TextView DirectionView;

        DirectionsViewHolder (View itemView){
            super(itemView);
            DirectionView = itemView.findViewById(R.id.tv_directionDisplay);
        }
    }


}
