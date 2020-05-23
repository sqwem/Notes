package com.example.notes;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button register_btn, login_btn, forgot_password_btn;
    private ImageView image;
    private TextView logoText, sloganText;
    private TextInputLayout emailEt, passwordEt;
    private String email, password;
    private FirebaseAuth firebaseAuth;

    private ProgressBar loginProgressBar;
    private ConstraintLayout loginLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().setStatusBarColor(getColor(R.color.colorPrimary));

        register_btn = findViewById(R.id.register_btn);
        image = findViewById(R.id.logo_image);
        logoText = findViewById(R.id.main_textView);
        sloganText = findViewById(R.id.second_textView);
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        login_btn = findViewById(R.id.login_btn);
        forgot_password_btn = findViewById(R.id.forgot_password_btn);

        loginProgressBar = findViewById(R.id.loginProgressBar);
        loginLayout = findViewById(R.id.loginLayout);

        firebaseAuth = FirebaseAuth.getInstance();

        register_btn.setOnClickListener(this);
        login_btn.setOnClickListener(this);
        forgot_password_btn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_btn:
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);

                //Animation
                Pair[] pairs = new Pair[6];
                pairs[0] = new Pair<View, String>(image, "logo_image");
                pairs[1] = new Pair<View, String>(logoText, "logo_text");
                pairs[2] = new Pair<View, String>(emailEt, "email_tran");
                pairs[3] = new Pair<View, String>(passwordEt, "password_tran");
                pairs[4] = new Pair<View, String>(login_btn, "go_tran");
                pairs[5] = new Pair<View, String>(register_btn, "register_tran");

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this, pairs);
                startActivity(intent, options.toBundle());

                break;

            case R.id.login_btn:
                email = emailEt.getEditText().getText().toString();
                password = passwordEt.getEditText().getText().toString();

                if (!validateEmail() | !validatePassword()) {
                    return;
                } else {

                    loginLayout.setVisibility(View.GONE);
                    loginProgressBar.setVisibility(View.VISIBLE);

                    firebaseAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent1);
                                        finish();
                                        loginProgressBar.setVisibility(View.GONE);
                                    } else {
                                        loginProgressBar.setVisibility(View.GONE);
                                        loginLayout.setVisibility(View.VISIBLE);
                                        Toast.makeText(getApplicationContext(), "Неверный логин или пароль", Toast.LENGTH_LONG).show();
                                    }

                                }
                            });
                }
                break;
            case R.id.forgot_password_btn:

                final View resetPasswordLayout = getLayoutInflater().inflate(R.layout.reset_password_dialog, null);

                new AlertDialog.Builder(this)
                        .setTitle("Восстановление пароля")
                        .setMessage("Введите email")
                        .setView(resetPasswordLayout)
                        .setPositiveButton("Восстановить", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText emailEditText = resetPasswordLayout.findViewById(R.id.emailEditText);

                                String emailStr = emailEditText.getText().toString().trim();

                                if (!validateDialogEmail(emailStr)) {
                                    Toast.makeText(getApplicationContext(), "Введите корректный email", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                firebaseAuth.sendPasswordResetEmail(emailStr)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getApplicationContext(), "Проверьте свой email", Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getApplicationContext(), "Ошибка", Toast.LENGTH_LONG).show();
                                            }
                                        });

                            }
                        })
                        .setNegativeButton("Отмена", null)
                        .show();

                break;
        }
    }

    private Boolean validateDialogEmail(String email) {
        String emailRegex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
        Pattern patternEmail = Pattern.compile(emailRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patternEmail.matcher(email);
        return matcher.find();
    }

    private Boolean validatePassword() {
        if (password.isEmpty()) {
            passwordEt.setError("Поле должно быть заполненным");
            return false;
        } else {
            passwordEt.setError(null);
            passwordEt.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateEmail() {

        if (email.isEmpty()) {
            emailEt.setError("Поле должно быть заполненным");
            return false;
        } else if (!email.contains("@")) {
            emailEt.setError("Неккоректный адрес");
            return false;
        } else {
            emailEt.setError(null);
            emailEt.setErrorEnabled(false);
            return true;
        }
    }
}
