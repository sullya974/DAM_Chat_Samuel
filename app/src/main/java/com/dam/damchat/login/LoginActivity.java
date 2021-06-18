package com.dam.damchat.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.dam.damchat.MainActivity;
import com.dam.damchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    /** 1 Ajout de var globales **/
    private TextInputEditText etEmail, etPassword;
    private String email, password;

    /** 8 Ajout de la progressBar **/
    private View progressBar;

    /** 2 Méthode initUI pour faire le lien entre le design et le code **/
    public void initUI(){
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_login);
        /** 3 Appel de la méthode initUI **/
        initUI();
        /** 8.1 Initialisation de la progressBar **/
        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * 4 Méthode pour la gestion du clic sur le bouton login. Cette méthode sera affectée
     * directement via la méthode onClick du xml.
     **/
    public void btnLoginClick(View v){
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();

        // Vérification du remplissage des champs email et password
        if(email.equals("")){
            etEmail.setError(getString(R.string.enter_email));
        } else if (password.equals("")){
            etPassword.setError(getString(R.string.enter_password));
        } else {
            /** 8.2 Si la connexion se fait alors on affiche la progressBar **/
            progressBar.setVisibility(View.VISIBLE);
            /** 5 Connexion à authenticator en utilisant les tools Firebase **/
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<AuthResult> task) {
                        /** 8.3 Que la connexion se fasse ou non on fait disparaître la progressBar **/
                        progressBar.setVisibility(View.GONE);
                        if(task.isSuccessful()){
                            /** 7 Ajout du lien vers mainActivity si l'utilisateur est bien connecté **/
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            // Utilisation de finish() pour fermer l'activité présente
                            finish();
                        } else {
                            // Affichage de l'erreur de connexion, il est possible de
                            // personnaliser, manuelement, le message en fonction du type d'erreur
                            Toast.makeText(LoginActivity.this,
                                    // A noter que pour l'affichage on utilise l'index des réponses et on tuilise la première cf strings (%1s)
                                    getString(R.string.login_failed, task.getException()),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }
    }

    /** 6 Méthode pour le bouton SignUp **/
    public void tvSignupClick(View v){
        startActivity(new Intent(LoginActivity.this, SignupActivity.class));
    }

    /** 6.1 Méthode pour le bouton reset password **/
    public void tvResetPasswordClick(View v){
        startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
    }

    /** Modifier les settings de la console pour afficher un nom correct dans l'email envoyé à
     * l'utilisateur idem si l'on veut changer l'email pour que l'on vous réponde **/

    /** 8 Ajout de la méthode onStart pour la gestion de l'utilisateur déjà loggué.
     * En effet cette utilisateur n'a pas besoin de le refaire et est directement redirigé vers MainActivity,
     * la vérification se fait dans la méthode onStart du cycle de vie de l'app
     */
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }
}









