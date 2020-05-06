package com.example.lc98034.mywechat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

public class Vadapter extends RecyclerView.Adapter<Vadapter.myviewHolder> {
    private Context context;
    private List<String> mList = new ArrayList<>();


    public Vadapter(Context context,ArrayList<String>list){
        this.context = context;
        this.mList = list;
    }

    @NonNull
    @Override
    public myviewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View itemview = LayoutInflater.from(context).inflate(R.layout.item,parent,false);
        return new myviewHolder(itemview);

    }

    @Override
    public void onBindViewHolder(myviewHolder holder, int posititon) {
        holder.tvContent.setText(mList.get(posititon));
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public class  myviewHolder extends RecyclerView.ViewHolder{
        TextView tvContent;

        public myviewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_content);

        }
    }

}
