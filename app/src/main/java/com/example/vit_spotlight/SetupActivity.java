package com.example.vit_spotlight;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SetupActivity extends AppCompatActivity {
    ImageView profilepic;
    TextView email,regAs;
    EditText setupname,setupphone;
    Button updatebtn;
    String user_ID;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    StorageReference sRef;
    private Uri mainImageURI=null;
    boolean isChanged= false;
    boolean valid = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        Toolbar setupToolbar=findViewById(R.id.toolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Setup");

        fAuth=FirebaseAuth.getInstance();
        user_ID=fAuth.getCurrentUser().getUid();
        FirebaseUser user = fAuth.getCurrentUser();

        fStore= FirebaseFirestore.getInstance();
        sRef= FirebaseStorage.getInstance().getReference();

        profilepic = findViewById(R.id.profilepic);
        email=findViewById(R.id.emailid);
        regAs=findViewById(R.id.RegAs);
        setupphone= findViewById(R.id.phone);
        setupname = findViewById(R.id.name);
        updatebtn = findViewById(R.id.updatebtn);


        fStore.collection("Users").document(user_ID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if(Objects.requireNonNull(task.getResult()).exists()){
                        String name=task.getResult().getString("FullName");
                        String image = task.getResult().getString("Profile Pic");
                        String phone = task.getResult().getString("PhoneNumber");
                        String emailid = task.getResult().getString("UserEmail");
                        mainImageURI=Uri.parse(image);
                        if(task.getResult().getString("isClub")!=null){
                            regAs.setText("Register As Club");
                        }
                        if(task.getResult().getString("isStudent")!=null){
                            regAs.setText("Register As Student");
                        }
                        setupname.setText(name);
                        setupphone.setText(phone);
                        email.setText(emailid);
                        RequestOptions placeholderReq=new RequestOptions();
                        placeholderReq.placeholder(R.drawable.defaultprofile);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderReq).load(image).into(profilepic);
                    }
                }else{
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "FireStore Retrieve Error : " + error, Toast.LENGTH_LONG).show();
                }
            }
        });

        profilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(SetupActivity.this,"Permission Denied", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }
                    else{
                        BringImagePicker();

                    }
                }
                else {
                    BringImagePicker();
                }
            }

        });

        updatebtn.setOnClickListener(v -> {
            checkField(setupphone);
            checkField(setupname);

            if(valid) {
                if (isChanged) {
                    final StorageReference Image_path = sRef.child("Profile_Images").child(user.getUid() + ".jpg");
                    UploadTask uploadTask = Image_path.putFile(mainImageURI);
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
                                storeFirestore(task, user);
                            } else {
                                // Handle failures
                                String error = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this, "FireStore Error : " + error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    storeFirestore(null, user);
                }
            }
        });


    }

    private boolean checkField(EditText textField) {
        if(textField.getText().toString().isEmpty()){
            textField.setError("Empty Field");
            valid = false;
        }else {
            valid = true;
        }
        return valid;
    }

    private void storeFirestore(@NonNull Task<Uri> task, FirebaseUser user) {
        Uri downloadUri;
        if(task!=null){
            downloadUri = task.getResult();
        }
        else{
            downloadUri = mainImageURI;
        }

        Map<String,String> useInfo = new HashMap<>();
        useInfo.put("Profile Pic",downloadUri.toString());
        useInfo.put("FullName",setupname.getText().toString());
        useInfo.put("UserEmail",email.getText().toString());
        useInfo.put("PhoneNumber",setupphone.getText().toString());
        if(regAs.getText().toString().contains("Club")){
            useInfo.put("isClub","1");
        }
        if(regAs.getText().toString().contains("Student")){
            useInfo.put("isStudent","1");
        }
        fStore.collection("Users").document(user.getUid()).set(useInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(SetupActivity.this, "Account Updated", Toast.LENGTH_LONG).show();
                }else{
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "FireStore Error : " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void BringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(SetupActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageURI = result.getUri();
                profilepic.setImageURI(mainImageURI);

                isChanged=true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }
}