package com.example.messenger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView userProfileImage;
    private EditText userName, userStatus;
    private Button updateAccountSettings;
    private ImageView backArrow;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private static final int GalleryPick = 1;
    private StorageReference userProfileImageRef;
    private ProgressDialog loadingBar;

    private Toolbar SettingsToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        //Speichert die Images des Users im Firebase-Storage <ProfileImage> ab
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");



        initializeFields();

        // UserName nach setzten nicht mehr änderbar! Unsichbar für Regestrierten <Optional>
        // userName.setVisibility(View.INVISIBLE);


        // Sendet den User zurück zur MainActivity wenn er keine Änderung wünscht
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        });

         // Wenmn der Update Button gedrückt wird....
        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                UpdateSettings();
            }
        });

        //BenutzerInformationen
        RetrieveUserInfo();

        //Was passieren soll wenn User auf ProfileImage klickt
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                // User in die ImageGallery des Telefons senden
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);

            }
        });
    }


    private void initializeFields()
    {
        // Zuweisung der Objekte wo sie sind
        updateAccountSettings = (Button) findViewById(R.id.update_settings_button);
        userName = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_profile_status);
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);
        backArrow = (ImageView) findViewById(R.id.back_arrow);
        loadingBar = new ProgressDialog(this);

        SettingsToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");


    }



    @Override     // Erlaubt dem Benutzter das ausgewählte Bild zu bearbeiten und hochzuladen
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GalleryPick && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();

            //Image Editor Öffnen und Image Zuschneiden oder Bearbeiten
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
              // Wenn Positive das Berechtigung zum weiterverarbeiten des Image
            if (resultCode == RESULT_OK)
            {
                //Einleiten der LoadingBar und setzten der message´s
                loadingBar.setTitle("set Profile Image");
                loadingBar.setMessage("Please wait, your Profile Image is Updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();

                //Image Speichern im Firebase Storage
                StorageReference filepath = userProfileImageRef.child(currentUserID + ".jpg");
                 // Hochladern des Image im FirebaseStorage in Datenbank-Speicherort <Users/Profile Image>
                filepath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        // Anleitung nach neuer <getDownloadUrl> Methode vom Firebase 2022
                        final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                        firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>()
                        {
                            @Override
                            public void onSuccess(Uri uri)
                            {
                                final String downloadUrl = uri.toString();

                                rootRef.child("Users").child(currentUserID).child("image")
                                        .setValue(downloadUrl)

                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if (task.isSuccessful())
                                                {
                                                    Toast.makeText(SettingsActivity.this, "Image save in Database, Successfully...", Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                                                else
                                                {
                                                    String message = task.getException().toString();
                                                    Toast.makeText(SettingsActivity.this, "error: " + message, Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });
                            }
                        });
                    }
                });
            }

        }
    }




    private void UpdateSettings()
    {
        // Speichert die Eingabe vom Benutzer z.b UserName in einen >String<
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName))
        {
            // Wenn kein Username im Profile angegeben wurde
            Toast.makeText(this, "Please write your Username first...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setUserName))
        {
            // Wenn kein Status gesetzt wurde
            Toast.makeText(this, "Please write your Status here...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            // HashMap Datentabelle <Schlüsselpaare>
            HashMap<String, Object> profileMap = new HashMap<>();
                profileMap.put("uid", currentUserID);
                profileMap.put("name", setUserName);
                profileMap.put("status", setStatus);
            rootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                // Profile Update war Erfolgreich..User zur MainActivity senden.
                                SendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile Updated Successfully...", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                //  Wenn Update fehlgeaschlagen zeigt Error an und Fehlercode
                               String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    // BenutzerInfo Methode
    private void RetrieveUserInfo()
    {
        rootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        //  User Info wird angezeigt wenn alle 3 Bedingungen erfüllt <UserName> + <UserStatus> + <ProfileImage>
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image"))))
                        {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                            // Downloader des ProfileImage und einsetzten im UserProfileImage
                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);

                        }
                        // Ausnahme Bedingung greift wenn User Neu und noch keine Informatioen da sind
                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                        {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();


                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                        }
                        else
                        {
                            // Wenn keine Information vorhanden...<Optional--> Username Sichtbar bei Neu Register
                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Please set & Update your Profile Information..", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    private void SendUserToMainActivity()
    {
        // Wenn Settings Abgeschlossen automatische weiterleitung zur MainActivity
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}