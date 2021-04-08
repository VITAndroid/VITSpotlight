package com.example.vit_spotlight;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.ViewHolder> {
    public List<EventPost> post_list;
    public Context context;

    public PostRecyclerAdapter(List<EventPost> post_list){
        this.post_list=post_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_list_item, parent,false);
        context=parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String desc_data = post_list.get(position).getDesc();
            holder.setDescText(desc_data);
            String imageurl = post_list.get(position).getImage_url();
            holder.setPostImage(imageurl);
            long millisecond= post_list.get(position).getTimestamp().getTime();
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy  hh:mm aa");
            String dateString = dateFormat.format(new Date(millisecond));
            holder.setDate(dateString);
    }

    @Override
    public int getItemCount() {
        return post_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        View mview;
        TextView descView,postDate;
        ImageView postImageView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mview=itemView;
        }
        public void setDescText(String descText){
                descView=mview.findViewById(R.id.post_desc);
                descView.setText(descText);
        }
        public void setPostImage(String downloaduri){
            postImageView=mview.findViewById(R.id.post_image);
            Glide.with(context).load(downloaduri).into(postImageView);

        }
        public void setDate(String date){
            postDate=mview.findViewById(R.id.post_date);
            postDate.setText(date);
        }
    }

}
