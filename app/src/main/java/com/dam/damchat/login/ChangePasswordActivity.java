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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

public class ChangePasswordActivity extends AppCompatActivity {

    /**
     * Variables globales
     **/
    private TextInputEditText etPassword, etConfirmPassword;
    //PB
    private View progressBar;

    private void initUI() {
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etPassword);
        progressBar = findViewById(R.id.progressBar); // PB2

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_change_password);

        // Appel des méthodes
        initUI();
    }

    public void btnSaveNewPassword(View v) {
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (password.equals("")) {
            etPassword.setError(getString(R.string.enter_password));
        } else if (confirmPassword.equals("")) {
            etConfirmPassword.setError(getString(R.string.confirm_password));
        } else if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.password_mismatch));
        } else {
            //Upadate password
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            //PB3
            progressBar.setVisibility(View.VISIBLE);
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

            if (firebaseUser != null) {
                firebaseUser.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE); //PB4
                        if (task.isSuccessful()) {
                            Toast.makeText(ChangePasswordActivity.this, R.string.password_changed_successfully, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(ChangePasswordActivity.this, getString(R.string.something_went_wrong, task.getException()),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }
}

