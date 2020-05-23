package com.example.notes;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout usernameEt, emailEt, passwordEt;
    private String username, email, password;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    String LOG_TAG = "RegisterActivity";
    private ProgressBar registerProgressBar;

    private ConstraintLayout registerLayoyt;

    private final String defaultImageUri = "https://firebasestorage.googleapis.com/v0/b/notes-584c3.appspot.com/o/profileImages%2Fdefaultuser.png?alt=media&token=8228e977-91b2-46a2-8a4a-5df8321f76c8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getWindow().setStatusBarColor(getColor(R.color.colorPrimary));

        usernameEt = findViewById(R.id.username);
        emailEt = findViewById(R.id.email);
        passwordEt = findViewById(R.id.password);

        registerProgressBar = findViewById(R.id.registerProgressBar);
        registerLayoyt = findViewById(R.id.registerLayout);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public void onRegister(View view) {
        username = usernameEt.getEditText().getText().toString().trim();
        email = emailEt.getEditText().getText().toString().trim();
        password = passwordEt.getEditText().getText().toString().trim();

        if (!validateName() | !validateEmail() | !validatePassword()) {
            return;
        } else {
            registerUser(email, password, username);
            registerLayoyt.setVisibility(View.GONE);
            registerProgressBar.setVisibility(View.VISIBLE);
        }

    }

    private void registerUser(final String email, String password, final String username) {

        Log.i(LOG_TAG, email + " " + password);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            saveUserInfoToDatabase(username);
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                            registerProgressBar.setVisibility(View.GONE);
                        } else {
                            registerLayoyt.setVisibility(View.VISIBLE);
                            registerProgressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Регистрация не удалась, попробуйте еще раз", Toast.LENGTH_LONG).show();
                            Log.i(LOG_TAG, String.valueOf(task.getException()));
                        }
                    }
                });


    }

    public void saveUserInfoToDatabase(String username) {
        Map<String, Object> mapInfoUser = new HashMap<>();
        mapInfoUser.put("username", username);
        mapInfoUser.put("profileImageUri", defaultImageUri);

        Log.i(LOG_TAG, firebaseAuth.getCurrentUser().getUid());
        firestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid())
                .set(mapInfoUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(LOG_TAG, "Complete save data");
                        } else {
                            Log.d(LOG_TAG, "Fail save data");
                        }
                    }
                });


    }

    public void onHaveAccount(View view) {
        onBackPressed();
    }


    private Boolean validateName() {
        if (username.isEmpty()) {
            usernameEt.setError("Поле должно быть заполненным");
            return false;
        } else if (username.length() <= 3) {
            usernameEt.setError("Слишком короткое имя пользователя");
            return false;
        } else if (username.length() > 12) {
            usernameEt.setError("Слишком длинное имя пользователя");
            return false;
        } else {
            usernameEt.setError(null);
            usernameEt.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateEmail() {
        if (email.isEmpty()) {
            emailEt.setError("Поле должно быть заполненным");
            return false;
        } else if (!validatingEmail(email)) {
            emailEt.setError("Неккоректный адрес");
            return false;
        } else {
            emailEt.setError(null);
            emailEt.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword() {
        if (password.isEmpty()) {
            passwordEt.setError("Поле должно быть заполненным");
            return false;
        } else if (password.length() < 7) {
            passwordEt.setError("Слишком короткий пароль");
            return false;
        } else {
            passwordEt.setError(null);
            passwordEt.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatingEmail(String email) {
        String emailRegex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
        Pattern patternEmail = Pattern.compile(emailRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patternEmail.matcher(email);
        return matcher.find();
    }

}
