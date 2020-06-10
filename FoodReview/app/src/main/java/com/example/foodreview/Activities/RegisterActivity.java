/*
 * Main activity
*/
package com.example.foodreview.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.foodreview.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {

    ImageView regUserPhoto;
    static int PRegCode = 1;
    static int REQUEST_CODE = 1;
    Uri pickedImgUri;
    private boolean isUserPickedImg = false;

    private EditText txtRegName, txtRegEmail, txtRegPassword, txtRegPassword2;

    private ProgressBar loadingProgress;
    private Button btnReg;
    private TextView linkToLogin;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Đăng ký tài khoản");

        txtRegName = findViewById(R.id.txtRegName);
        txtRegEmail = findViewById(R.id.txtRegEmail);
        txtRegPassword = findViewById(R.id.txtRegPassword);
        txtRegPassword2 = findViewById(R.id.txtRegPassword2);
        loadingProgress = findViewById(R.id.regProgressBar);

        linkToLogin = findViewById(R.id.linkToLogin);

        linkToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginActivity);
                finish();
            }
        });

        btnReg = findViewById(R.id.btnReg);

        mAuth = FirebaseAuth.getInstance();

        loadingProgress.setVisibility(View.INVISIBLE);

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnReg.setVisibility(View.INVISIBLE);
                loadingProgress.setVisibility(View.VISIBLE);

                final String name = txtRegName.getText().toString();
                final String email = txtRegEmail.getText().toString();
                final String password = txtRegPassword.getText().toString();
                final String passwordCf = txtRegPassword2.getText().toString();
                if (isUserPickedImg == false) {
                    showMessage("Vui lòng chọn một hình ảnh để tiếp tục!");
                    btnReg.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);
                } else if (name.isEmpty() || email.isEmpty() || password.isEmpty() || !passwordCf.equals(password)) {
                    // Nguoi dung chua nhap day du thong tin de dang nhap
                    showMessage("Vui lòng kiểm tra lại thông tin!");
                    btnReg.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);
                } else {
                    // Nguoi dung da nhap tat ca du lieu, bay gio co the tien hanh tao tai khoan cho nguoi dung
                    CreateUserAccount(name, email, password);
                }

            }
        });

        regUserPhoto = findViewById(R.id.regUserPhoto);

        regUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT > 22) {
                    checkAndRequestForPermission();
                }
                else {
                    openGallery();
                }
            }
        });
    }

    private void CreateUserAccount(final String name, String email, String password) {
        // phuong thuc tao moi tai khoan cho nguoi dung

        mAuth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // tai khoan duoc tao thanh cong
                    showMessage("Tài khoản đã được tạo!");

                    // sau khi tao tai khoan thanh cong, chung ta can cap nhat anh dai dien va ten nguoi dung
                    updateUserInfo(name, pickedImgUri, mAuth.getCurrentUser());
                }
                else {
                    // tai khoan khong duoc tao
                    showMessage("Tạo tài khoản không thành công! " + task.getException().getMessage());
                    btnReg.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    // cap nhat anh dai dien va ten nguoi dung
    private void updateUserInfo(final String name, Uri userImgUri, final FirebaseUser currentUser) {
        // dau tien, chung ta se tai len hinh anh va lay duong dan url

        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photos");
        final StorageReference imgFilePath;
        try {
            imgFilePath = mStorage.child(userImgUri.getLastPathSegment());
            imgFilePath.putFile(userImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // hinh anh duoc tai len thanh cong
                    imgFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // uri chua duong dan hinh anh cua nguoi dung

                            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .setPhotoUri(uri)
                                    .build();

                            currentUser.updateProfile(profileUpdate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // thong tin nguoi dung da duoc cap nhat

                                                showMessage("Đăng ký thành công!");

                                                updateUI();
                                            }
                                        }
                                    });
                        }
                    });
                }
            });
        }
        catch (Exception ex) {
            showMessage(ex + "");
            btnReg.setVisibility(View.VISIBLE);
            loadingProgress.setVisibility(View.INVISIBLE);
        }

    }

    private void updateUI() {
        Intent homeActivity = new Intent(getApplicationContext(), Home.class);
        startActivity(homeActivity);
        finish();
    }

    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private void openGallery() {
        // SOLVED TODO: open Gallery intent and wait for user to pick an image

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUEST_CODE);
    }

    private void checkAndRequestForPermission() {
        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(RegisterActivity.this, "Vui lòng chấp nhận yêu cầu truy cập!", Toast.LENGTH_SHORT).show();
            }
            else {
                ActivityCompat.requestPermissions(RegisterActivity.this,
                                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE}, PRegCode);
            }
        }
        else {
            openGallery();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE && data != null) {
            // Nguoi dung da chon hinh anh - chung ta can luu tru no vao mot uri
            pickedImgUri = data.getData();

            regUserPhoto.setImageURI(pickedImgUri);
            isUserPickedImg = true;
        }
    }
}
