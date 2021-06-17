package com.dam.damchat.login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.dam.damchat.R;
import com.dam.damchat.common.NodesNames;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    /**
     * 1 Variables globales
     **/
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private String name, email, password, confirmPassword;

    private Uri localFileUri, serverFileUri;

    private ImageView ivAvatar;

    /**
     * 5 Ajout de la var FirebaseUser
     **/
    private FirebaseUser firebaseUser;

    /**
     * Ajout de la base RealTime db
     **/
    private DatabaseReference databaseReference;
    /**
     * Ajout de la référence vers le storage
     **/
    private StorageReference fileStorage;

    /**
     * 2 Méthode initUI pour faire le lien entre design et code
     **/
    public void initUI() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        ivAvatar = findViewById(R.id.ivAvatar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);

        /** 3 Appel de la méthode initUI **/
        initUI();

        fileStorage = FirebaseStorage.getInstance().getReference();
    }

    /**
     * 4 Méthode pour la gestion du click sur le bouton SignUp
     **/
    public void btnSignupClick(View v) {
        name = etName.getText().toString().trim();
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        confirmPassword = etConfirmPassword.getText().toString().trim();

        // Si les champs sont vides
        if (name.equals("")) {
            etName.setError(getString(R.string.enter_name));
        } else if (email.equals("")) {
            etEmail.setError(getString(R.string.enter_email));
        } else if (password.equals("")) {
            etPassword.setError(getString(R.string.enter_password));
        } else if (confirmPassword.equals("")) {
            etConfirmPassword.setError(getString(R.string.confirm_password));
        }
        // Vérification des pattern
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.enter_valid_email));
        } else if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.password_mismatch));
        } else {
            /** Connexion à Firebase **/
            final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            // Création de l'utilisateur dans Authentication
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                                if (localFileUri != null) {
                                    updateNameAndPhoto();
                                } else {
                                    updateNameOnly();
                                }

//                                Toast.makeText(SignupActivity.this,
//                                        R.string.user_created_successfully,
//                                        Toast.LENGTH_SHORT).show();
//                                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                            } else {
                                Toast.makeText(SignupActivity.this,
                                        getString(R.string.signup_failed) + task.getException(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
//            .addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(SignupActivity.this,
//                            getString(R.string.signup_failed) + e.getMessage(),
//                            Toast.LENGTH_SHORT).show();
//                }
//            })
        }
    }

    /**
     * Méthode pour faire l'enregistrement des informations d'Authentication dans RealTime
     **/
    private void updateNameOnly() {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(etName.getText().toString().trim())
                .build();

        firebaseUser.updateProfile(request)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            String userId = firebaseUser.getUid();
                            /** Connexion à RealTime **/
                            databaseReference = FirebaseDatabase
                                    .getInstance() // Instance de connexion à la db
                                    .getReference() // Chercher la réf désirée à partir du root de la db
                                    .child(NodesNames.USERS); // La référence en question qui passe comme constante depuis NodesNames

                            // Crréation HashMap pour la gestion des données
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put(NodesNames.NAME, etName.getText().toString().trim());
                            hashMap.put(NodesNames.EMAIL, etEmail.getText().toString().trim());
                            hashMap.put(NodesNames.ONLINE, "true");
                            hashMap.put(NodesNames.AVATAR, "");

                            // Envoi des datas vers RealTime
                            databaseReference.child(userId).setValue(hashMap)
                                    .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            Toast.makeText(SignupActivity.this,
                                                    R.string.user_created_successfully,
                                                    Toast.LENGTH_SHORT).show();
                                            // Lancement de l'activité suivante
                                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                        }
                                    });

                        } else {
                            // S'il y a un problème
                            Toast.makeText(SignupActivity.this,
                                    getString(R.string.failed_to_update_user) + task.getException(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Ajout de la méthode pour la gestion de l'avatar
     **/
    public void pickImage(View v) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 101);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    102);
        }
    }

    /**
     * Ajouter la méthode pour vérifier si l'on a la permission ou non
     **/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions,
                                           @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 101);
            } else {
                Toast.makeText(this,
                        R.string.access_permission_is_required,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Vérifications
        // Le resquestcode est-il le bon ?
        if (requestCode == 101) {
            // Il y'a bien une sélection d'image sinon le resultCode = RESULT_CANCELED
            if (resultCode == RESULT_OK) {
                // Path complet vers l'image sur le terminal
                localFileUri = data.getData();
                // Affecter l'uri à l'avatar
                ivAvatar.setImageURI(localFileUri);
            }
        }
    }

    /**
     * Ajout de la méthode pour l'update de la photo et du contenu dans Realtime db
     **/
    private void updateNameAndPhoto() {
        // Renommer l'image avec l'userId et le type de fichier (ici jpg)
        String strFileName = firebaseUser.getUid() + ".jpg";
        // On place l'image de l'avatar dans le Storage
        final StorageReference fileRef = fileStorage.child("avatars_user/" + strFileName);
        // Upload vers le storage
        fileRef.putFile(localFileUri)
                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            // récupérer l'url de l'avatar dans le storage
                            fileRef.getDownloadUrl()
                                    .addOnSuccessListener(SignupActivity.this,
                                            new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    serverFileUri = uri;
                                                    UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                                            .setDisplayName(etName.getText().toString().trim())
                                                            .setPhotoUri(serverFileUri)
                                                            .build();

                                                    firebaseUser.updateProfile(request)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        String userId = firebaseUser.getUid();
                                                                        /** Connexion à RealTime **/
                                                                        databaseReference = FirebaseDatabase
                                                                                .getInstance() // Instance de connexion à la db
                                                                                .getReference() // Chercher la réf désirée à partir du root de la db
                                                                                .child(NodesNames.USERS); // La référence en question qui passe comme constante depuis NodesNames

                                                                        // Crréation HashMap pour la gestion des données
                                                                        HashMap<String, String> hashMap = new HashMap<>();
                                                                        hashMap.put(NodesNames.NAME, etName.getText().toString().trim());
                                                                        hashMap.put(NodesNames.EMAIL, etEmail.getText().toString().trim());
                                                                        hashMap.put(NodesNames.ONLINE, "true");
                                                                        hashMap.put(NodesNames.AVATAR, serverFileUri.getPath());

                                                                        // Envoi des datas vers RealTime
                                                                        databaseReference.child(userId).setValue(hashMap)
                                                                                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                                                        Toast.makeText(SignupActivity.this,
                                                                                                R.string.user_created_successfully,
                                                                                                Toast.LENGTH_SHORT).show();
                                                                                        // Lancement de l'activité suivante
                                                                                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                                                                    }
                                                                                });

                                                                    } else {
                                                                        // S'il y a un problème
                                                                        Toast.makeText(SignupActivity.this,
                                                                                getString(R.string.failed_to_update_user) + task.getException(),
                                                                                Toast.LENGTH_LONG).show();
                                                                    }
                                                                }
                                                            });
                                                }
                                            });
                        }
                    }
                });
    }
}






















