package com.example.notes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.notes.fragments.NotesFragment;
import com.example.notes.fragments.ProfileFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private TextView appBarTV;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private TextView usernameTv, userEmailTv;
    private FirebaseFirestore firestore;
    private static final String LOG_TAG = "MainLog";
    private View headerView;
    private ImageView imageProfile;
    private String uriImage;

    private ListenerRegistration listenerRegistration;

    private int oldItemId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawer = findViewById(R.id.drawerLayout);
        appBarTV = findViewById(R.id.appbar_text_view);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        oldItemId = R.id.nav_notes;

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);

        drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        navigationView = findViewById(R.id.navigationView);

        //Init textView in nav header.
        headerView = navigationView.getHeaderView(0);
        userEmailTv = headerView.findViewById(R.id.userEmailTv);
        usernameTv = headerView.findViewById(R.id.usernameTv);
        imageProfile = headerView.findViewById(R.id.usernameImage);
        Log.d("MainAct", firebaseAuth.getCurrentUser().getUid());

        getDataUserAndSet();

        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_notes);
            getSupportFragmentManager().beginTransaction().replace(R.id.f_container, new NotesFragment()).commit();
        }

        if (oldItemId == R.id.nav_notes){
            appBarTV.setText("Заметки");
        } else if (oldItemId == R.id.nav_profile){
            appBarTV.setText("Профиль");
        }

    }


    private void getDataUserAndSet() {
        firestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        userEmailTv.setText(firebaseAuth.getCurrentUser().getEmail());
                        usernameTv.setText(documentSnapshot.getString("username"));

                        Picasso.with(MainActivity.this)
                                .load(documentSnapshot.getString("profileImageUri"))
                                .into(imageProfile);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(LOG_TAG, e.getMessage());
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();

        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == oldItemId) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            drawer.closeDrawer(GravityCompat.START);
            switch (item.getItemId()) {
                case R.id.nav_notes:
                    getSupportFragmentManager().beginTransaction().replace(R.id.f_container, new NotesFragment()).commit();
                    appBarTV.setText("Заметки");
                    oldItemId = R.id.nav_notes;
                    break;
                case R.id.nav_profile:
                    getSupportFragmentManager().beginTransaction().replace(R.id.f_container, new ProfileFragment()).commit();
                    appBarTV.setText("Профиль");
                    oldItemId = R.id.nav_profile;
                    break;
                case R.id.nav_signOut:
                    firebaseAuth.signOut();
                    Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(loginIntent);
                    finish();
                    break;
            }
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        listenerRegistration = FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            try {
                                throw e;
                            } catch (FirebaseFirestoreException ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            userEmailTv.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                            usernameTv.setText(documentSnapshot.getString("username"));
                            uriImage = documentSnapshot.getString("profileImageUri");

                            Picasso.with(MainActivity.this)
                                    .load(uriImage)
                                    .into(imageProfile);
                        }

                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        listenerRegistration.remove();
    }
}
