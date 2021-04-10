package com.example.vit_spotlight;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ObjectStreamField;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;


public class Register extends AppCompatActivity {
    EditText fullName,email,password,phone;
    Button registerBtn,goToLogin;
    ImageView profilepic;
    boolean valid = true;
    FirebaseAuth fAuth;
    StorageReference sRef;
    FirebaseFirestore fStore;
    CheckBox isClubBox,isStudentBox;
    private Uri mainImageURI=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fAuth=FirebaseAuth.getInstance();
        sRef=FirebaseStorage.getInstance().getReference();
        fStore=FirebaseFirestore.getInstance();

        profilepic = findViewById(R.id.profilepic);
        fullName = findViewById(R.id.registerName);
        email = findViewById(R.id.registerEmail);
        password = findViewById(R.id.registerPassword);
        phone = findViewById(R.id.registerPhone);
        registerBtn = findViewById(R.id.registerBtn);
        goToLogin = findViewById(R.id.gotoLogin);
        isClubBox = findViewById(R.id.isClub);
        isStudentBox=findViewById(R.id.isStudent);

        final ProgressDialog progressDialog = new ProgressDialog(getApplicationContext());
        progressDialog.setMessage("Loading Data...");
        progressDialog.setCancelable(false);

        ProgressDialog progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage("Sending.. Please wait!");
        progress.setCancelable(false);

        profilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(Register.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(Register.this,"Permission Denied", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(Register.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
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

        isStudentBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if(compoundButton.isChecked()){
                isClubBox.setChecked(false);
            }
        });
        isClubBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if(compoundButton.isChecked()){
                isStudentBox.setChecked(false);
            }
        });

        registerBtn.setOnClickListener(v -> {
            checkField(fullName);
            checkField(email);
            checkField(password);
            checkField(phone);
            String Vpassword = password.getText().toString().trim();

            if(!(isClubBox.isChecked()||isStudentBox.isChecked())){
                Toast.makeText(Register.this,"Select Account Type",Toast.LENGTH_SHORT).show();
                return;
            }
            if(mainImageURI == null){
                Toast.makeText(Register.this,"Select Photo",Toast.LENGTH_SHORT).show();
                return;
            }
            if(isClubBox.isChecked()){
                String Vemail = email.getText().toString().trim();
                String VemailPattern = "[a-zA-Z0-9.]+@vit.ac.in";
                if(!(Vemail.matches(VemailPattern))){
                    email.setError("Invalid Club Email ID");
                    return;
                }
            }
            if(isStudentBox.isChecked()){
                String Vemail = email.getText().toString().trim();
                String VemailPattern = "[a-zA-Z0-9.]+@vitstudent.ac.in";
                if(!(Vemail.matches(VemailPattern))){
                    email.setError("Invalid Student Email ID");
                    return;
                }
            }
            if(Vpassword.length()<7){
                password.setError("Password Must be >= 8 Characters");
                return;
            }
            progress.show();
            int count=0;
            if(valid){
                fAuth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnSuccessListener(authResult -> {
                    FirebaseUser user =fAuth.getCurrentUser();

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
                                Uri downloadUri = task.getResult();
                                Map<String,String> useInfo = new HashMap<>();
                                useInfo.put("Profile Pic",downloadUri.toString());
                                useInfo.put("FullName",fullName.getText().toString());
                                useInfo.put("UserEmail",email.getText().toString());
                                useInfo.put("PhoneNumber",phone.getText().toString());

                                if(isClubBox.isChecked()){
                                    useInfo.put("isClub","1");
                                }
                                if(isStudentBox.isChecked()){
                                    useInfo.put("isStudent","1");
                                }

                                fStore.collection("Users").document(user.getUid()).set(useInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(Register.this, "Account Created", Toast.LENGTH_LONG).show();

                                            final AlertDialog.Builder verifyDialog = new AlertDialog.Builder(v.getContext());
                                            verifyDialog.setTitle("Verify Your Email");
                                            verifyDialog.setMessage("Click Verify to get your Account Verified.!");
                                            verifyDialog.setCancelable(false);
                                            verifyDialog.setPositiveButton("Verify", (dialog, which) -> {
                                                user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(Register.this, "Reset Link Sent To Your Email. \n Please verify before login", Toast.LENGTH_LONG).show();
                                                        fAuth.signOut();
                                                        startActivity(new Intent(getApplicationContext(),Login.class));
                                                        finish();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(Register.this, "Error ! Verified Link is Not Sent" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                            });

                                            verifyDialog.create().show();


                                        }else{
                                            String error = task.getException().getMessage();
                                            Toast.makeText(Register.this, "FireStore Error : " + error, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                // Handle failures
                                String error = task.getException().getMessage();
                                Toast.makeText(Register.this, "FireStore Error : " + error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }).addOnFailureListener(e -> {
                    Toast.makeText(Register.this,"failed to create Account " + e.getMessage(),Toast.LENGTH_LONG).show();
                });
            }
        });
        progress.dismiss();

        goToLogin.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),Login.class)));

    }

    private void BringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(Register.this);
    }

    public boolean checkField(EditText textField){
        if(textField.getText().toString().isEmpty()){
            textField.setError("Empty Field");
            valid = false;
        }else {
            valid = true;
        }

        return valid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageURI = result.getUri();
                profilepic.setImageURI(mainImageURI);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


}