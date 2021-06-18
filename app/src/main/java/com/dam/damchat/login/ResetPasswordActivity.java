package com.dam.damchat.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dam.damchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

public class ResetPasswordActivity extends AppCompatActivity {

    /**
     * 1 Variables globales
     **/
    private TextInputEditText etEmail;
    private TextView tvMessage;
    private LinearLayout llResetPassword, llMessageResetPassword;
    private Button btnRetry;
    private View progressBar; // PB1

    /**
     * 2 Méthode initUI pour lier le code et le design ou in,itialisation des widgets
     **/
    private void initUi() {
        etEmail = findViewById(R.id.etEmail);
        tvMessage = findViewById(R.id.tvMessage);
        llResetPassword = findViewById(R.id.llResetPassword);
        llMessageResetPassword = findViewById(R.id.llMessageResetPassword);
        btnRetry = findViewById(R.id.btnRetry);
        progressBar = findViewById(R.id.progressBar); // PB2
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_reset_password);

        // Appel des méthodes
        initUi();
    }

    /**
     * 3 Méthode ResetPassword
     **/
    public void btnResetPasswordClick(View v) {
        // Extratction de l'email de l'editText
        String email = etEmail.getText().toString().trim();
        // Vérifications
        if (email.equals("")) {
            etEmail.setError(getString(R.string.enter_email));
        } else {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            progressBar.setVisibility(View.VISIBLE); //PB3
            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    // PB4
                    progressBar.setVisibility(View.GONE);
                    // On change la visibilité des Linear Layout
                    llResetPassword.setVisibility(View.GONE);
                    llMessageResetPassword.setVisibility(View.VISIBLE);

                    if (task.isSuccessful()) {
                        tvMessage.setText(getString(R.string.reset_password_instructions, email));
                        // Dans ce cas il faut afficher le bouton retry mais comme l'utilisateur vient juste d'envoyer
                        // un mail il faut le faire attendre avant de recommencer, nous allons implémenter un Timer
                        new CountDownTimer(60000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                // Affichage du texte sur le bouton retry
                                btnRetry.setText(getString(R.string.resend_timer, String.valueOf(millisUntilFinished / 1000)));
                                // Blocage du clic sur le bouton
                                btnRetry.setOnClickListener(null);
                            }

                            @Override
                            public void onFinish() {
                                btnRetry.setText(R.string.retry);

                                btnRetry.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        llResetPassword.setVisibility(View.VISIBLE);
                                        llMessageResetPassword.setVisibility(View.GONE);

                                    }
                                });
                            }
                        }.start();

                    } else {
                        tvMessage.setText(getString(R.string.failed_to_send_email, task.getException()));
                        btnRetry.setText(R.string.retry);

                        btnRetry.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                llResetPassword.setVisibility(View.VISIBLE);
                                llMessageResetPassword.setVisibility(View.GONE);
                            }
                        });
                    }

                }
            });
        }
    }

    /**
     * Méthode Close
     **/
    public void btnCloseClick(View v) {
        finish();
    }
}