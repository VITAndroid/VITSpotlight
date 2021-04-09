package com.example.vit_spotlight;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static com.example.vit_spotlight.Notification.CHANNEL1_ID;


public class NewPostActivity extends AppCompatActivity {
    Toolbar mainToolbar;
    ImageView newPostImage;
    EditText newPostDesc;
    Button newPostBtn;
    Uri postImageUri=null;
    StorageReference sRef;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    String Current_Uid;
    private NotificationManagerCompat notificationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        notificationManager=NotificationManagerCompat.from(getApplicationContext());

        sRef= FirebaseStorage.getInstance().getReference();
        fStore=FirebaseFirestore.getInstance();
        fAuth=FirebaseAuth.getInstance();
        Current_Uid=fAuth.getCurrentUser().getUid();

        mainToolbar=(Toolbar)findViewById(R.id.post_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newPostImage=findViewById(R.id.new_post_image);
        newPostDesc=findViewById(R.id.new_post_desc);
        newPostBtn=findViewById(R.id.post_btn);

        newPostImage.setOnClickListener(v -> {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    //.setMinCropResultSize(512,512)
                    .setAspectRatio(1,1)
                    .start(NewPostActivity.this);
        });

        newPostBtn.setOnClickListener(v -> {

            String title="New Post Added";
            String msg="Check Out New Post Added";
            android.app.Notification notification =new NotificationCompat.Builder(getApplicationContext(), CHANNEL1_ID)
                    .setSmallIcon(R.drawable.icnotifications_active_24)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_EVENT)
                    .build();
            notificationManager.notify(1, notification);

            String Desc=newPostDesc.getText().toString();
            if(!TextUtils.isEmpty(Desc) && postImageUri!=null){

                String randomName = UUID.randomUUID().toString();
                final StorageReference Image_path = sRef.child("Post_Images").child(randomName + ".jpg");
                UploadTask uploadTask = Image_path.putFile(postImageUri);
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        // Continue with the task to get the download URL
                        return Image_path.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            Map<String,Object> postMap = new HashMap<>();
                            postMap.put("image_url",downloadUri.toString());
                            postMap.put("desc",Desc);
                            postMap.put("user_id",Current_Uid);
                            postMap.put("timestamp",FieldValue.serverTimestamp());

                            fStore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(NewPostActivity.this, "Post Added", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(getApplicationContext(),Club.class));
                                        finish();

                                    }else{
                                        String error = task.getException().getMessage();
                                        Toast.makeText(NewPostActivity.this, "FireStore Error : " + error, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            // Handle failures
                            String error = task.getException().getMessage();
                            Toast.makeText(NewPostActivity.this, "FireStore Error : " + error, Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }else{
                Toast.makeText(NewPostActivity.this, "Enter Content And Select Picture", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void sendchannel(View v) {
            String title="New Post Added";
            String msg="Check Out New Post Added";
            android.app.Notification notification =new NotificationCompat.Builder(getApplicationContext(), CHANNEL1_ID)
                    .setSmallIcon(R.drawable.icnotifications_active_24)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_EVENT)
                    .build();
            notificationManager.notify(1, notification);
            notificationManager.notifyAll();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                    postImageUri=result.getUri();
                    newPostImage.setImageURI(postImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }



}