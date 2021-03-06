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
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.dam.damchat.R;
import com.dam.damchat.common.NodesNames;
import com.google.android.gms.tasks.OnCompleteListener;
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
    /**
     * 5.1 Ajout de la variable FirebaseUser
     **/
    private FirebaseUser firebaseUser;

    /**
     * 6.1 Ajout de la base RealTime db
     **/
    private DatabaseReference databaseReference;
    /**
     * 7.1 Ajout de la variable de liaison avec FirebaseStorage
     **/
    private StorageReference fileStorage; // Ne pas oublier son initialisation dans le onCreate 7.2
    /**
     * 7.5 Variables des Uri du fichier image de l'avatar utilisateur
     */
    private Uri localFileUri, serverFileUri;
    /**
     * 7.6 Variable pour la localisation de l'ImageView
     */
    private ImageView ivAvatar;
    /**
     * 11 Ajout de la progressBar (PB)
     **/
    private View progressBar;

    /**
     * 2 M??thode initUI pour faire le lien entre design et code
     **/
    public void initUI() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        /** 7.6 Bis lien avec le design **/
        ivAvatar = findViewById(R.id.ivAvatar);
        /** 11.1 Init PB **/
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);

        /** 3 Appel de la m??thode initUI **/
        initUI();
        /** 7.2 Initialisation du bucket pour le stockage des avatars utilisateurs **/
        fileStorage = FirebaseStorage.getInstance().getReference();
    }

    /** 4 M??thode pour la gestion du click sur le bouton directement via la m??thode onClick du xml
     *  et ajout des v??rifications suivantes :
     *  - Nom non vide
     *  - Mail non vide et du bon type (utilisation des patterns android)
     *  - Password non vide et comprenant au minimum 6 caract??res (par d??faut)
     *  - V??rification du password avec confirmpassword pour un match identique
     **/
    public void btnSignupClick(View v) {
        name = etName.getText().toString().trim();
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        confirmPassword = etConfirmPassword.getText().toString().trim();
        // Les v??rifications
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
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { // La pattern pour v??rifier si il s'agit bien d'un email
            etEmail.setError(getString(R.string.enter_valid_email));
        } else if (!password.equals(confirmPassword)) {  // V??rification password identique
            etConfirmPassword.setError(getString(R.string.password_mismatch));
        } else {
            /** 11.2 PB Visible **/
            progressBar.setVisibility(View.VISIBLE);
            // La connexion ?? Firebase
            final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            // Cr??ation de l'utilisateur dans Authentication
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    // Ajout la m??thode addOnCompleteListener pour v??rifier la bonne transmition des
                    // informations ?? Firebase Authenticator
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                            /** 11.3 PB Gone **/
                            progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                /** 8 On lance la bonne m??thode d'enregistrement dans la base en fonction
                                 *  de l'ajout d'un avatar ou non **/
                                if (localFileUri != null) {
                                    updateNameAndPhoto();
                                } else {
                                    updateNameOnly();
                                }
                            } else {
                                Toast.makeText(SignupActivity.this,
                                        getString(R.string.signup_failed, task.getException()),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
//            A noter que l'on peut aussi utiliser la m??thode suivante pour remplacer le else ci-dessus
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
     * 5.1 Ajout de la m??thode pour changer le nom de l'utilisateur elle est aussi utilis??e pour l'ajout ?? la premi??re connexion
     **/
    private void updateNameOnly() {
        /** 11.4 PB Visible **/
        progressBar.setVisibility(View.VISIBLE);
        // Utilisation de la m??thode UserProfileChangeRequest pour charger le nom de l'utilisateur qui s'est enregistr??
        // Gestion de remplissage d'Authenticator
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(etName.getText().toString().trim())
                .build();

        /** 5.3 Update du nom du profile utilisateur ?? partir de l'edittext  **/
        firebaseUser.updateProfile(request)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        /** 11.5 PB Gone **/
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // On r??cup??re l'id du User pour l'utiliser comme id dans RealTime
                            String userId = firebaseUser.getUid();
                            /** 6.2 Insertion dans la Realtime database **/
                            // Connexion ?? RealTime
                            databaseReference = FirebaseDatabase
                                    .getInstance() // Instance de connexion ?? la db
                                    .getReference() // Chercher la r??f d??sir??e ?? partir du root de la db
                                    .child(NodesNames.USERS); // La r??f??rence en question qui passe comme constante depuis NodesNames

                            // Cr??ation HashMap pour la gestion des donn??es
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put(NodesNames.NAME, etName.getText().toString().trim());
                            hashMap.put(NodesNames.EMAIL, etEmail.getText().toString().trim());
                            hashMap.put(NodesNames.ONLINE, "true");
                            hashMap.put(NodesNames.AVATAR, "");

                            /** 11.6 PB Visible **/
                            progressBar.setVisibility(View.VISIBLE);

                            // Envoi des datas vers RealTime
                            databaseReference.child(userId).setValue(hashMap)
                                    // On v??rifie le bon d??roulement avec .addOnCompleteListener()
                                    // Si tout se passe bien l'utilisateur est dirig?? vers la page de login
                                    // A noter qu'il faut rappeler le contexte (l'endroit o?? s'ex??cute la m??thode
                                    // pour que l'action soit valid??e
                                    .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            /** 11.7 PB Gone **/
                                            progressBar.setVisibility(View.GONE);
                                            // Affichage d'un toast de r??ussite
                                            Toast.makeText(SignupActivity.this,
                                                    R.string.user_created_successfully,
                                                    Toast.LENGTH_SHORT).show();
                                            // Lancement de l'activit?? suivante
                                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                        }
                                    });

                        } else {
                            // Il y a un probl??me
                            Toast.makeText(SignupActivity.this,
                                    getString(R.string.nameUpdateFailed, task.getException()),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * 7.3 Ajout de la m??thode pour la gestion de l'avatar de l'utilisateur
     * Ne pas oublier de la lier ?? l'imageView dans le XML
     **/
    public void pickImage(View v) {
        /**
         *  9 Ajout de la v??rification de la permission de parcourir les dossiers du terminal
         * Avant toute chose il faut ajouter la permission dans le manifest
         **/
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            // Ajout de l'intent implicite qui va ouvrir la galerie du terminal pour choisir un photo : Intent.ACTION_PICK
            // Il faut ensuite ajouter l'espace de stockage dans lequel recherch??, ici les images stock??es sur tout le terminal
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 101);
            // A noter que le request code peut-??tre n'importe quoi, il n'y en a un qu'un seul dans cette activit??
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    102);
        }
    }

    /**
     * 10 Ajout de la m??thode pour v??rifier si l'on ?? la permission ou non
     **/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions,
                                           @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // The Old Way, la m??thode startActivityForResult ??tant d??pr??ci??e
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

    /**
     * 7.4 Action ?? effectu??e en r??sultat de la m??thode pickImage() // r??ponse ?? startActivityForResult
     **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // V??rifications
        // Le resquestcode est-il le bon ?
        if (requestCode == 101) {
            // Il y'a bien une s??lection d'image, sinon le resultCode = RESULT_CANCELED
            if (resultCode == RESULT_OK) {
                // Ajout des variables globales des uri cf 7.5
                // Path complet vers l'image sur le terminal
                localFileUri = data.getData();
                // Affectation de l'image s??lectionn??e ?? l'avatar (pour la variable globale cf 7.5)
                ivAvatar.setImageURI(localFileUri);
            }
        }
    }

    /**
     * 7.7 Ajout de la m??thode pour uploader l'image sur le storage et r??cup??rer son URL pour remplir la db Users
     */
    private void updateNameAndPhoto() {
        /** 11.8 PB Visible **/
        progressBar.setVisibility(View.VISIBLE);
        // Renommer l'image avec l'userId et le type de fichier (ici jpg)
        String strFileName = firebaseUser.getUid() + ".jpg";
        // On place l'image de l'avatar dans le Storage
        final StorageReference fileRef = fileStorage.child("avatars_user/" + strFileName);
        // Upload vers le storage
        fileRef.putFile(localFileUri)
                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                        /** 11.9 PB Gone **/
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // r??cup??rer l'url de l'avatar dans le storage
                            fileRef.getDownloadUrl()
                                    .addOnSuccessListener(SignupActivity.this,
                                            new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    serverFileUri = uri;
                                                    // Que l'on transmet ?? Authenticator
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
                                                                        // Puis on renseigne les champs de RealTime
                                                                        databaseReference = FirebaseDatabase
                                                                                .getInstance() // Instance de connexion ?? la db
                                                                                .getReference() // Chercher la r??f d??sir??e ?? partir du root de la db
                                                                                .child(NodesNames.USERS); // La r??f??rence en question qui passe comme constante depuis NodesNames

                                                                        // Cr??ation HashMap pour la gestion des donn??es
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
                                                                                        // Lancement de l'activit?? suivante
                                                                                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                                                                    }
                                                                                });

                                                                    } else {
                                                                        // S'il y a un probl??me
                                                                        Toast.makeText(SignupActivity.this,
                                                                                getString(R.string.nameUpdateFailed, task.getException()),
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






















