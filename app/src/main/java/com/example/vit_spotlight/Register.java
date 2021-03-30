package com.example.vit_spotlight;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class Register extends AppCompatActivity {
    EditText fullName,email,password,phone;
    Button registerBtn,goToLogin;
    boolean valid = true;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    CheckBox isClubBox,isStudentBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        fullName = findViewById(R.id.registerName);
        email = findViewById(R.id.registerEmail);
        password = findViewById(R.id.registerPassword);
        phone = findViewById(R.id.registerPhone);
        registerBtn = findViewById(R.id.registerBtn);
        goToLogin = findViewById(R.id.gotoLogin);
        isClubBox = findViewById(R.id.isClub);
        isStudentBox=findViewById(R.id.isStudent);

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
            /*if(isClubBox.isChecked()){
                String Vemail = email.getText().toString().trim();
                String VemailPattern = "[a-zA-Z0-9.]+@vit.ac.in";
                if(!(Vemail.matches(VemailPattern))){
                    email.setError("Invalid Club Email ID");
                    return;
                }
            }*/
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

            if(valid){
                fAuth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnSuccessListener(authResult -> {
                    FirebaseUser user =fAuth.getCurrentUser();
                    Toast.makeText(Register.this, "Account Created",Toast.LENGTH_SHORT).show();
                    DocumentReference df=fStore.collection("Users").document(user.getUid());
                    Map<String,Object> useInfo = new HashMap<>();
                    useInfo.put("FullName",fullName.getText().toString());
                    useInfo.put("UserEmail",email.getText().toString());
                    useInfo.put("PhoneNumber",phone.getText().toString());

                    if(isClubBox.isChecked()){
                        useInfo.put("isClub","1");
                    }
                    if(isStudentBox.isChecked()){
                        useInfo.put("isStudent","1");
                    }

                    df.set(useInfo);
                    final AlertDialog.Builder verifyDialog = new AlertDialog.Builder(v.getContext());
                    verifyDialog.setTitle("Verify Your Email");
                    verifyDialog.setMessage("Click Verify to get your Account Verified.!");
                    verifyDialog.setCancelable(false);
                    verifyDialog.setPositiveButton("Verify", (dialog, which) -> {
                        user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Register.this, "Reset Link Sent To Your Email. Please verify before login", Toast.LENGTH_LONG).show();
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

                }).addOnFailureListener(e -> {
                    Toast.makeText(Register.this,"failed to create Account",Toast.LENGTH_SHORT).show();
                });
            }
        });

        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));
            }
        });

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
}