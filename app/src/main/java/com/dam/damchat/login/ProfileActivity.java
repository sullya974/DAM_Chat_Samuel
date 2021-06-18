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
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.dam.damchat.R;
import com.dam.damchat.common.NodesNames;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
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

public class ProfileActivity extends AppCompatActivity {
    /** 1.1 Variables globales des widgets **/
    private TextInputEditText etName, etEmail;
    private ImageView ivAvatar;
    private View progressBar; // PB1

    /** 1.2 Variables globales de Firebase **/
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference fileStorage;
    private FirebaseAuth firebaseAuth;

    /** 1.3 Variables globales pour les URI **/
    private Uri localFileUri, serverFileUri;

    /** 2 Méthode pour l'initialisation des composants initUI() **/
    public void initUI(){
        etEmail = findViewById(R.id.etEmail);
        etName = findViewById(R.id.etName);
        ivAvatar = findViewById(R.id.ivAvatar);
        progressBar = findViewById(R.id.progressBar); //PB2
    }

    /** 3 Méthode pour l'initialisation des composants Firebase **/
    public void initFirebase(){
        // Instance pour le storage des images
        fileStorage = FirebaseStorage.getInstance().getReference();
        // Pour vérifier que l'utilisateur est bien connecté
        firebaseAuth = FirebaseAuth.getInstance(); // Utilisé pour se déconnecter
        firebaseUser = firebaseAuth.getCurrentUser();
        // Si l'utilisateur n'est pas vide alors on rempli les champs
    }

    private void initProfileUser(){
        // A compléter
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_profile);

        /** 4 Appel des méthodes d'initialisation **/
        initUI();
        initFirebase();
        initProfileUser();
    }

    /** 5 Copie des méthodes updateOnlyUser et updateNameAndPhoto
     * sans oublier de changer le context par celui de l'activité dans laquelle nous sommes **/
    private void updateNameOnly() {
        // PB3
        progressBar.setVisibility(View.VISIBLE);
        // Utilisation de la méthode UserProfileChangeRequest pour charger le nom de l'utilisateur
        // qui s'est enregistré
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(etName.getText().toString().trim())
                .build();

        /** 5.3 Update du nom du profile utilisateur à partir de l'editText  **/
        firebaseUser.updateProfile(request)
                // Ajout d'un listener qui affiche un Toast si tout c'est bien déroulé
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                        //PB4
                        progressBar.setVisibility(View.GONE);
                        // Tout c'est bien passé
                        if (task.isSuccessful()) {
                            String userID = firebaseUser.getUid();
                            /** 6.2 Insertion dans la base de données **/
                            databaseReference = FirebaseDatabase
                                    .getInstance() // Obtient une instance de connexion à la db
                                    .getReference() // Cherche la référence souhaitée à partir de la racine de la db
                                    .child(NodesNames.USERS); // La référence en question qui vient de la classe NodesNames

                            // Insertion dans la bd à l'aide d'un hashmap
                            HashMap<String, String> hashMap = new HashMap<>();
                            // On supprime la MAJ de l'email, online et Photo
                            hashMap.put(NodesNames.NAME, etName.getText().toString().trim());

                            // Envoie des données vers la db
                            databaseReference.child(userID).setValue(hashMap)
                                    // On vérifie le bon déroulement avec .addOnCompleteListener()
                                    // Si tout se passe bien l'utilisateur est dirigé vers la page de login
                                    // A noter qu'il faut rappeler le contexte (l'endroit où s'exécute la méthode
                                    // pour que l'action soit validée
                                    .addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                                            finish();
                                        }
                                    });

                        } else {
                            // Il y a un problème
                            Toast.makeText(ProfileActivity.this,
                                    getString(R.string.nameUpdateFailed, task.getException()),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateNameAndPhoto() {
        progressBar.setVisibility(View.VISIBLE); //PB5
        //  Renommage du fichier avec l'userid + le type de fichier (ici jpg)
        String strFileName = firebaseUser.getUid() + ".jpg";
        // On place la photo dans un dossier dans le storage
        final StorageReference fileRef = fileStorage.child("avatars_user/" + strFileName);
        // On fait l'upload
        fileRef.putFile(localFileUri)
                .addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        //PB6
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // On récupére l'url de l'image uploadée
                            fileRef.getDownloadUrl().addOnSuccessListener(ProfileActivity.this, new OnSuccessListener<Uri>() {
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
                                                public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        String userID = firebaseUser.getUid();
                                                        databaseReference = FirebaseDatabase.getInstance().getReference().child(NodesNames.USERS);

                                                        HashMap<String, String> hashMap = new HashMap<>();
                                                        // On supprime la MAJ de ONLINE et de l'EMAIL
                                                        hashMap.put(NodesNames.NAME, etName.getText().toString().trim());
                                                        hashMap.put(NodesNames.AVATAR, serverFileUri.getPath());

                                                        databaseReference.child(userID).setValue(hashMap)
                                                                .addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                                                                        finish();
                                                                    }
                                                                });

                                                    } else {
                                                        Toast.makeText(ProfileActivity.this,
                                                                getString(R.string.nameUpdateFailed, task.getException()),
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                }

                            });
                        }

                    }
                });
    }

    /** 6 Copie des méthodes pickImage onActityResult et onRequestPermissionResult
     * Noter que l'on rend privée et que l'on retire les attributs de la méthode pickImage,
     * en effet nous allons l'appeler dans la méthode changeAvatar()  que nous allons créer ci-dessous
     *
     * **/
    private void pickImage() {
        /**
         *  9 Ajout de la vérification de la permission de parcourir les dossiers du terminal
         * Avant toute chose il faut ajouter la permission dans le manifest
         **/
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 101);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 102);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                localFileUri = data.getData();
                ivAvatar.setImageURI(localFileUri);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==102){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 101);
            } else {
                Toast.makeText(this, R.string.access_permission_is_required, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** 7 Ajout de la méthode changeAvatar qui permet à l'utilisateur de changer sa photo de profile
     * Ne pas oublier de lier le widget de l'image à cette méthode **/
    public void changeAvatar(View v){
//       A compléter
    }

    /** 8 Ajout de la méthode deleteAvatar pour supprimer l'image **/
    private void deleteAvatar(){
        // Google Authenticator
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(etName.getText().toString().trim())
                .setPhotoUri(null)
                .build();
        // Base de données
        firebaseUser.updateProfile(request)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            String userID = firebaseUser.getUid();
                            databaseReference = FirebaseDatabase.getInstance().getReference().child(NodesNames.USERS);

                            HashMap<String, String> hashMap = new HashMap<>();
                            // On rempli la base Users avec du vide
                            hashMap.put(NodesNames.AVATAR, "");

                            databaseReference.child(userID).setValue(hashMap)
                                    .addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                                            // Changement du Toast
                                            Toast.makeText(ProfileActivity.this, R.string.avatar_deleted_successfully, Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        } else {
                            Toast.makeText(ProfileActivity.this,
                                    getString(R.string.nameUpdateFailed, task.getException()),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /** 9 Ajout de la méthode de gestion du clic sur Save **/
    public void btnSaveClick(View v){
        // Avant d'envoyer les données vers les bases on vérifie que les chamsp du formulaire ne soit pas vide
        if(etName.getText().toString().trim().equals("")){
            etName.setError(getString(R.string.enter_name));
        } else {
            if(localFileUri != null){
                updateNameAndPhoto();
            } else {
                updateNameOnly();
            }
        }
    }

    /** 10 Ajout de la méthode pour bouton signout */
    public void btnSignOut(View v){
        firebaseAuth.signOut();
        // On renvoie l'utilisateur vers LoginActivity
        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
        // On ferme l'activité courante
        finish();
    }

    public void btnChangePasswordClick(View v){
        startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));
    }
}