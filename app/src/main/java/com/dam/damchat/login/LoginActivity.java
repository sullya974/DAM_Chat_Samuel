package com.dam.damchat.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.dam.damchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    /** Ajout de var globales **/
    private TextInputEditText etEmail, etPassword;
    private String email, password;

    /** Méthode initUI pour faire le lien entre le design et le code **/
    public void initUI(){
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_login);

        // Appel de la méthode d'initialisation de l'UI
        initUI();
    }

    /** Méthode pour gérer le clic sur le btn login **/
    public void btnLoginClick(View v){
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();

        if(email.equals("")){
            etEmail.setError(getString(R.string.enter_email));
        } else if (password.equals("")){
            etPassword.setError(getString(R.string.enter_password));
        } else {
            /** Connection à Firebase **/
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            // Rien faire
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    getString(R.string.login_failed) + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }
    }

    /** Méthode pour le bouton SignUp **/
    public void tvSignupClick(View v){
        startActivity(new Intent(LoginActivity.this, SignupActivity.class));
    }

    /** Méthode pour le bouton reset password **/
    public void tvResetPasswordClick(View v){
        startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
    }
}









