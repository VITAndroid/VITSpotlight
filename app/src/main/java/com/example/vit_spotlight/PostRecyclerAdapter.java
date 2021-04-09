package com.example.vit_spotlight;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.ViewHolder> {
    public List<EventPost> post_list;
    public List<User> user_list;
    public Context context;
    public FirebaseFirestore fStore;
    public FirebaseAuth fAuth;
    public PostRecyclerAdapter(List<EventPost> post_list,List<User> user_list){
        this.post_list=post_list;
        this.user_list=user_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_list_item, parent,false);
        context=parent.getContext();
        fStore=FirebaseFirestore.getInstance();
        fAuth=FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            holder.setIsRecyclable(false);

            final String PostId=post_list.get(position).PostId;
            String currentUserId= fAuth.getCurrentUser().getUid();

            String desc_data = post_list.get(position).getDesc();
            holder.setDescText(desc_data);

            String imageurl = post_list.get(position).getImage_url();
            holder.setPostImage(imageurl);

            long millisecond= post_list.get(position).getTimestamp().getTime();
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy  hh:mm aa");
            String dateString = dateFormat.format(new Date(millisecond));
            holder.setDate(dateString);

            String user_id=post_list.get(position).getUser_id();
            if(user_id.equals(currentUserId)){
                /*holder.postDeleteBtn.setEnabled(true);
                holder.postDeleteBtn.setVisibility(View.VISIBLE);*/
                holder.postdel.setVisibility(View.VISIBLE);
            }
            fStore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if(task.isSuccessful()){
                        String userName=task.getResult().getString("FullName");
                        String userImage=task.getResult().getString("Profile Pic");
                        holder.setUserData(userName, userImage);
                    }else{
                        //firebase Expection
                    }

                }
            });

            fStore.collection("Posts/"+PostId+"/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (value != null) {
                        if (!value.isEmpty()) {
                            int count = value.size();
                            holder.updateLikesCount(count);
                        } else {
                            holder.updateLikesCount(0);
                        }
                    }
                }
            });


            fStore.collection("Posts/"+PostId+"/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                 if (documentSnapshot != null) {
                    if (documentSnapshot.exists()) {
                        holder.postLikeBtn.setImageDrawable(context.getDrawable(R.drawable.action_like_accent));
                    } else {
                        holder.postLikeBtn.setImageDrawable(context.getDrawable(R.drawable.action_like_gray));
                    }
                }
                }
            });

            holder.postLikeBtn.setOnClickListener(v -> {
                fStore.collection("Posts/"+PostId+"/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(!task.getResult().exists()){
                            Map<String, Object> likesMap= new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());
                            fStore.collection("Posts/"+PostId+"/Likes").document(currentUserId).set(likesMap);
                        }else{
                            fStore.collection("Posts/"+PostId+"/Likes").document(currentUserId).delete();
                        }
                    }
                });
            });

            holder.postdel.setOnClickListener(v -> {
                fStore.collection("Posts").document(PostId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        post_list.remove(position);
                        user_list.remove(position);
                    }
                });
            });

    }

    @Override
    public int getItemCount() {
        return post_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        View mview;
        TextView descView,postDate,postuserName,postLikeCount;
        ImageView postImageView,postuserImage,postLikeBtn,postdel;
        Button postDeleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mview=itemView;
            postLikeBtn=mview.findViewById(R.id.post_like_button);
            postDeleteBtn=mview.findViewById(R.id.post_delete_btn);
            postdel=mview.findViewById(R.id.postdel);
        }
        public void setDescText(String descText){
                descView=mview.findViewById(R.id.post_desc);
                descView.setText(descText);
        }
        public void setPostImage(String downloaduri){
            postImageView=mview.findViewById(R.id.post_image);
            ImagePopup imagePopup = new ImagePopup(context);
            imagePopup.setWindowHeight(1000);
            imagePopup.setWindowWidth(1000);
            imagePopup.setFullScreen(false);
            imagePopup.setHideCloseIcon(true);
            imagePopup.setImageOnClickClose(true);
            Glide.with(context).load(downloaduri).into(postImageView);
            imagePopup.initiatePopupWithGlide(downloaduri);
            postImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imagePopup.viewPopup();
                }
            });
        }
        public void setDate(String date){
            postDate=mview.findViewById(R.id.post_date);
            postDate.setText(date);
        }
        public void setUserData(String name, String image){
            postuserImage=mview.findViewById(R.id.post_user_image);
            postuserName=mview.findViewById(R.id.post_user_name);
            postuserName.setText(name);
            RequestOptions placeholderOption=new RequestOptions();
            placeholderOption.placeholder(R.drawable.defaultprofile);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(postuserImage);
            ImagePopup imagePopup = new ImagePopup(context);
            imagePopup.setWindowHeight(800);
            imagePopup.setWindowWidth(800);
            imagePopup.setFullScreen(false);
            imagePopup.setHideCloseIcon(true);
            imagePopup.setImageOnClickClose(true);
            imagePopup.initiatePopupWithGlide(image);
            postuserImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imagePopup.viewPopup();
                }
            });

        }
        public void updateLikesCount(int count){
            postLikeCount= mview.findViewById(R.id.post_like_count);
            postLikeCount.setText(count+" Interested");
        }
    }

}
