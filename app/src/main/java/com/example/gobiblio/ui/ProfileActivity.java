package com.example.gobiblio.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.gobiblio.BuildConfig;
import com.example.gobiblio.LoadDialog;
import com.example.gobiblio.MainActivity;
import com.example.gobiblio.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_IMAGE = 0;
    ChipNavigationBar navigationBar;
    ImageView profilePicture;
    TextView userName, userEmail;
    FirebaseAuth firebaseAuth;
    FirebaseStorage firebaseStorage;
    FirebaseFirestore firebaseFirestore;
    private String uID;
    private String name, image;
    private Uri imageUri;
    LayoutInflater layoutInflater;
    View dialogView;
    ImageView currentImage;
    TextInputEditText currentName;
    AlertDialog alertDialog;
    StorageReference storageReference;
    RelativeLayout progress;
    LoadDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_profile);

        setBottomNavigation();
        initDesign();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        uID = firebaseAuth.getUid();

        getUserData();
    }

    private void getUserData() {
        firebaseFirestore.collection("Users").document(uID).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dialog.dismissDialog();
                        if (Objects.requireNonNull(task.getResult()).exists()) {
                            name = task.getResult().getString("userName");
                            String email = task.getResult().getString("userEmail");
                            image = task.getResult().getString("profilePicture");
                            userName.setText(name);
                            userEmail.setText(email);
                            Picasso.get().load(image).into(profilePicture);
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressLint("InflateParams")
    private void initDesign() {
        profilePicture = findViewById(R.id.img_profile);
        userName = findViewById(R.id.txt_name);
        userEmail = findViewById(R.id.txt_email);
        findViewById(R.id.txt_aboutApp).setOnClickListener(this);
        findViewById(R.id.txt_changePassword).setOnClickListener(this);
        findViewById(R.id.txt_shareApp).setOnClickListener(this);
        findViewById(R.id.txt_reportBug).setOnClickListener(this);
        findViewById(R.id.txt_signOut).setOnClickListener(this);
        findViewById(R.id.txt_edit).setOnClickListener(this);
        layoutInflater = LayoutInflater.from(ProfileActivity.this);
        dialog = new LoadDialog(ProfileActivity.this);
        //Setting progress dialog
        dialog.showDialog();
        //Making profile picture curved
        profilePicture.setClipToOutline(true);
    }

    @SuppressLint("InflateParams")
    private void showEditAlert() {
        dialogView = layoutInflater.inflate(R.layout.edit_data_alert, null);
        currentName = dialogView.findViewById(R.id.et_currentName);
        currentImage = dialogView.findViewById(R.id.img_currentProfile);
        AppCompatButton change = dialogView.findViewById(R.id.btn_choose);
        AppCompatButton save = dialogView.findViewById(R.id.btn_save);
        TextView back = dialogView.findViewById(R.id.txt_back);
        progress = dialogView.findViewById(R.id.progressLayout);

        currentName.setText(name);
        Picasso.get().load(image).into(currentImage);
        currentImage.setClipToOutline(true);        //Rounded Corners

        alertDialog = new AlertDialog.Builder(ProfileActivity.this).setView(dialogView).create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        alertDialog.setCancelable(false);

        change.setOnClickListener(v -> choosePhoto());
        save.setOnClickListener(v -> updateData());
        back.setOnClickListener(v -> alertDialog.dismiss());

    }

    private void updateData() {
        progress.setVisibility(View.VISIBLE);
        DocumentReference documentReference = firebaseFirestore.collection("Users")
                .document(uID);
        Map<String, Object> map = new HashMap<>();
        map.put("userName", Objects.requireNonNull(currentName.getText()).toString());

        StorageReference file = storageReference.child("users/" + uID + "/profile.jpg");
        if (imageUri != null) {
            file.putFile(imageUri).addOnSuccessListener(taskSnapshot -> file.getDownloadUrl().addOnSuccessListener(uri -> {
                map.put("profilePicture", uri.toString());
                Picasso.get().load(uri).into(this.profilePicture);
            })).addOnFailureListener(e ->
                    Toast.makeText(ProfileActivity.this, "Uh-oh! Please try again.", Toast.LENGTH_SHORT)
                            .show());
        }

        documentReference.update(map)
                .addOnSuccessListener(aVoid -> {
                    progress.setVisibility(View.GONE);
                    alertDialog.dismiss();
                    getUserData();
                }).addOnFailureListener(e ->
                Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void choosePhoto() {
        Dexter.withContext(ProfileActivity.this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), REQUEST_IMAGE);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(ProfileActivity.this, "Permission Denied :(", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
            assert data != null;
            imageUri = data.getData();
            cropImage(imageUri);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                assert result != null;
                imageUri = result.getUri();
                currentImage.setImageURI(imageUri);
            }
        }
    }

    private void cropImage(Uri imageUri) {
        CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true).setAspectRatio(1, 1).start(this);
    }

    @SuppressLint("NonConstantResourceId")
    private void setBottomNavigation() {
        navigationBar = findViewById(R.id.navigationBar);
        navigationBar.setItemSelected(R.id.profile, true);
        navigationBar.setOnItemSelectedListener(i -> {
            switch (i) {
                case R.id.home:
                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                    break;
                case R.id.library:
                    startActivity(new Intent(ProfileActivity.this, LibraryActivity.class));
                    break;
                case R.id.profile:
                    break;
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txt_edit:
                showEditAlert();
                break;
            case R.id.txt_aboutApp:
                Toast.makeText(ProfileActivity.this, "About App", Toast.LENGTH_SHORT).show();
                break;
            case R.id.txt_shareApp:
                Toast.makeText(ProfileActivity.this, "Share App", Toast.LENGTH_SHORT).show();
                break;
            case R.id.txt_changePassword:
                changePassword();
                break;
            case R.id.txt_reportBug:
                reportBug();
                break;
            case R.id.txt_signOut:
                signOut();
                break;
        }
    }

    private void changePassword() {
        LayoutInflater layoutInflater = LayoutInflater.from(ProfileActivity.this);
        View view = layoutInflater.inflate(R.layout.change_password_alert, null);

        TextInputEditText currentPass = view.findViewById(R.id.et_currentPassword);
        TextInputEditText newPass = view.findViewById(R.id.et_newPassword);
        Button send = view.findViewById(R.id.btn_changePass);
        TextView back = view.findViewById(R.id.txt_close);
        LinearLayout successLayout = view.findViewById(R.id.layout_success);

        AlertDialog alertDialog = new AlertDialog.Builder(ProfileActivity.this).setView(view).create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        alertDialog.setCancelable(false);

        send.setOnClickListener(v -> {
            String email = firebaseAuth.getCurrentUser().getEmail();
            String currentPassword = Objects.requireNonNull(currentPass.getText()).toString().trim();
            String newPassword = Objects.requireNonNull(newPass.getText().toString().trim());
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (TextUtils.isEmpty(currentPassword)) {
                currentPass.setError("Password is required");
                currentPass.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(newPassword)) {
                newPass.setError("Password is required");
                newPass.requestFocus();
                return;
            }
            AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);
            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    successLayout.setVisibility(View.VISIBLE);
                                    new Handler().postDelayed(() -> {
                                        alertDialog.dismiss();
                                        signOut();
                                    }, 3000);
                                } else {
                                    Toast.makeText(ProfileActivity.this, "Uh-oh! Please try again", Toast.LENGTH_SHORT).show();
                                    alertDialog.dismiss();
                                }
                            });
                        } else {
                            Toast.makeText(ProfileActivity.this, "Couldn't Verify. Please check password again", Toast.LENGTH_LONG).show();
                        }
                    });

        });

        back.setOnClickListener(v -> alertDialog.dismiss());

    }

    @SuppressLint("IntentReset")
    private void reportBug() {
        String recipient = "ashutoshsahni9@gmail.com";
        String subject = "[GoBiblio] Android Bug Report";
        String opening = "Summary:\n-...\n\nSteps to reproduce:\n-...\n\nExpected Results:\n-...\n\nActual Results:\n-...\n\n";
        String deviceInfo = "\n\nDEVICE INFO\n\n";
        String separator = "*****************************";
        String androidVersion = getAndroidVersion();
        String model = Build.MODEL;
        String product = Build.PRODUCT;
        int apiLevel = Build.VERSION.SDK_INT;
        String appVersion = BuildConfig.VERSION_NAME;
        String manufacturer = Build.MANUFACTURER;
        String account = firebaseAuth.getCurrentUser().getEmail();
        String closing = "Thanks for filing a bug!";
        String heart = new String(Character.toChars(0x2764));

        String message = opening + separator + deviceInfo +
                "App Version: " + appVersion + "\nAndroid Version: " + androidVersion + "\nAPI Level: "
                + apiLevel + "\nManufacturer: " + manufacturer +
                "\nPhone Model: " + model + " (" + product + ")" + "\nUser: " + account + "\n\n"
                + separator + "\n\n" + closing + heart;

        Intent sendMail = new Intent(Intent.ACTION_SENDTO);
        sendMail.setType("text/plain");
        sendMail.setData(Uri.parse("mailto:"));
        sendMail.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
        sendMail.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendMail.putExtra(Intent.EXTRA_TEXT, message);

        try {
            startActivity(Intent.createChooser(sendMail, "Choose an e-mail client"));
        } catch (Exception e) {
            Toast.makeText(ProfileActivity.this, "Uh-oh! Please try again.", Toast.LENGTH_SHORT).show();
        }

    }

    @NotNull
    private String getAndroidVersion() {
        double release = Double.parseDouble(Build.VERSION.RELEASE.replaceAll("(\\d+[.]\\d+)(.*)", "$1"));
        String codeName = "Unsupported";//below Jelly bean OR above Oreo
        if (release >= 4.1 && release < 4.4) codeName = "Jelly Bean";
        else if (release < 5) codeName = "Kit Kat";
        else if (release < 6) codeName = "Lollipop";
        else if (release < 7) codeName = "Marshmallow";
        else if (release < 8) codeName = "Nougat";
        else if (release < 9) codeName = "Oreo";
        else if (release < 10) codeName = "Pie";
        else if (release < 11) codeName = "Android 10";
        else if (release < 12) codeName = "Android 11";
        return codeName + " v" + release;
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
        finishAffinity();
    }
}