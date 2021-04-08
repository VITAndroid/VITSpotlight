package com.example.vit_spotlight;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Club extends AppCompatActivity {
    FirebaseAuth fAuth;
    Toolbar mainToolbar;
    BottomNavigationView clubbottomNav;
    HomeFragment homeFragment;
    LikedFragment likedFragment;
    GeofenceFragment geofenceFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club);

        fAuth=FirebaseAuth.getInstance();

        mainToolbar=(Toolbar)findViewById(R.id.club_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("VITSpotlight");

        clubbottomNav=findViewById(R.id.clubBottomNav);
        //Fragments
        homeFragment=new HomeFragment();
        likedFragment=new LikedFragment();
        geofenceFragment=new GeofenceFragment();

        replaceFragement(homeFragment);
        clubbottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch ((item.getItemId())){
                    case R.id.bottom_action_home:
                        replaceFragement(homeFragment);
                        return true;
                    case R.id.bottom_action_liked:
                        replaceFragement(likedFragment);
                        return true;
                    case R.id.bottom_action_geofence:
                        replaceFragement(geofenceFragment);
                        return true;
                    default:
                        return false;
                }
            }
        });


    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        if(fUser==null){
            startActivity(new Intent(getApplicationContext(),Login.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.club_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_logout_btn:
                fAuth.signOut();
                startActivity(new Intent(getApplicationContext(),Login.class));
                finish();
                return true;

            case R.id.action_settings_btn:
                Intent settingIntent = new Intent(Club.this,SetupActivity.class);
                startActivity(settingIntent);
                return true;

            case R.id.action_new_post:
                Intent NewpostIntent = new Intent(Club.this,NewPostActivity.class);
                startActivity(NewpostIntent);
                return true;
            default:
                return false;
        }
    }

    private  void replaceFragement(Fragment fragment){
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.club_container,fragment);
        fragmentTransaction.commit();
    }
}