package com.example.gobiblio.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.example.gobiblio.LoadDialog;
import com.example.gobiblio.MainActivity;
import com.example.gobiblio.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

public class OnboardingActivity extends AppCompatActivity {
    TextView greeting, skip;
    TextInputEditText fullName;
    ImageView profilePicture;
    AppCompatButton go, choose;
    FirebaseFirestore fireStore;
    FirebaseAuth firebaseAuth;
    StorageReference storageReference;
    private Uri imageUri;
    LoadDialog loadDialog;
    private static final int REQUEST_IMAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        firebaseAuth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        initDesign();

    }

    private void initDesign() {
        greeting = findViewById(R.id.txt_welcome);
        choose = findViewById(R.id.btn_upload);
        skip = findViewById(R.id.txt_skip);
        fullName = findViewById(R.id.et_email);
        profilePicture = findViewById(R.id.img_profile);
        go = findViewById(R.id.btn_go);
        loadDialog = new LoadDialog(OnboardingActivity.this);
        //Make ImageView Round
        profilePicture.setClipToOutline(true);
        //Setting two colors
        Spannable first = new SpannableString("Hey! You must be\nnew");
        Spannable second = new SpannableString(" here");
        first.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack)), 0, first.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        second.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), R.color.colorYellow)), 0, second.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        greeting.setText(first);
        greeting.append(second);
        //Saving default uri for image
        imageUri = Uri.parse("android.resource://com.example.gobiblio/" + R.drawable.default_image);
        //Upload Picture
        choose.setOnClickListener(view -> choosePhoto());
        //Skip without name
        skip.setOnClickListener(view ->
                {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                            OnboardingActivity.this);
                    alertDialog.setTitle("Skip for now?");
                    alertDialog.setMessage("Do you want to set your profile later?");
                    alertDialog.setPositiveButton("Yes",
                            (dialog, which) ->
                            {
                                startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
                                overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
                                finish();
                            });
                    alertDialog.setNegativeButton("No",
                            (dialog, which) -> dialog.cancel());
                    alertDialog.show();
                }
        );
        //Upload to FireStore
        go.setOnClickListener(view -> uploadData());
    }

    private void choosePhoto() {
        Dexter.withContext(OnboardingActivity.this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), REQUEST_IMAGE);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(OnboardingActivity.this, "Permission Denied :(", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    private void uploadData() {
        final String uID = firebaseAuth.getCurrentUser().getUid();
        final String email = firebaseAuth.getCurrentUser().getEmail();
        final String name = fullName.getText().toString();

        if (TextUtils.isEmpty(name)) {
            fullName.setError("Email is required");
            fullName.requestFocus();
            return;
        }
        loadDialog.showDialog();
        StorageReference file = storageReference.child("users/" + uID + "/profile.jpg");
        file.putFile(imageUri).addOnSuccessListener(taskSnapshot -> file.getDownloadUrl().addOnSuccessListener(uri -> storeData(uri, uID, email, name)))
                .addOnFailureListener(e ->
                        Toast.makeText(OnboardingActivity.this, "Uh-oh! Please try again.", Toast.LENGTH_SHORT).show());
    }

    private void storeData(Uri uri, String uID, String email, String name) {
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("userID", uID);
        dataMap.put("userEmail", email);
        dataMap.put("profilePicture", uri.toString());
        dataMap.put("userName", name);

        fireStore.collection("Users").document(uID).set(dataMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadDialog.dismissDialog();
                        startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
                        overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
                        finish();
                    } else {
                        Toast.makeText(OnboardingActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

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
                profilePicture.setImageURI(imageUri);
                Toast.makeText(OnboardingActivity.this, "Looking good!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void cropImage(Uri imageUri) {
        CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true).setAspectRatio(1, 1).start(this);
    }
}


