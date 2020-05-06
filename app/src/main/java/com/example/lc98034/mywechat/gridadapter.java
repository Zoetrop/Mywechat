package com.example.lc98034.mywechat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class gridadapter extends RecyclerView.Adapter<gridadapter.GridViewHolder> {


    private List<String> mList=new ArrayList<>();
    public gridadapter(List<String> list){
        mList=list;
        notifyDataSetChanged();
    }

    @Override
    public gridadapter.GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.grid,parent,false);
        return new gridadapter.GridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull gridadapter.GridViewHolder holder, int position) {
        holder.gridone.setText(position+1+"");
        holder.gridtext.setText(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public class GridViewHolder extends RecyclerView.ViewHolder{
        TextView gridone,gridtext;

        public GridViewHolder(@NonNull View itemView) {
            super(itemView);
            gridone=itemView.findViewById(R.id.grid_one);
            gridtext=itemView.findViewById(R.id.grid_text);
        }
    }
}
