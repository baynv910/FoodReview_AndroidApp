package com.example.foodreview.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.ui.AppBarConfiguration;

import com.bumptech.glide.Glide;
import com.example.foodreview.Fragments.HomeFragment;
import com.example.foodreview.Fragments.ProfileFragment;
import com.example.foodreview.Fragments.SettingsFragment;
import com.example.foodreview.Models.Post;
import com.example.foodreview.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_CODE = 2;
    private static final int PRegCode = 2;
    private AppBarConfiguration mAppBarConfiguration;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    Dialog popAddPost;
    ImageView popupUserPhoto, popupPostPhoto, popupAddButton;
    TextView popupTitle, popupDescription;
    ProgressBar popupProgressBar;
    private Uri pickedImgUri = null;
    FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // khoi tao
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // khoi tao popup tao bai viet
        initPopup();
        setupPopUpImgClick();


        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popAddPost.show();
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });

        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        updateNavHeader();

        // thiet lap home frag ment la man hinh mac dinh khoi dau
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new HomeFragment()).commit();
    }

    private void setupPopUpImgClick() {

        popupPostPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestForPermission();
            }
        });




    }

    private void checkAndRequestForPermission() {
        if (ContextCompat.checkSelfPermission(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(Home.this, "Vui lòng chấp nhận yêu cầu truy cập!", Toast.LENGTH_SHORT).show();
            }
            else {
                ActivityCompat.requestPermissions(Home.this,
                        new String[] { Manifest.permission.READ_EXTERNAL_STORAGE}, PRegCode);
            }
        }
        else {
            openGallery();
        }
    }

    private void openGallery() {
        // SOLVED TODO: open Gallery intent and wait for user to pick an image

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE && data != null) {
            // Nguoi dung da chon hinh anh - chung ta can luu tru no vao mot uri
            pickedImgUri = data.getData();

            popupPostPhoto.setImageURI(pickedImgUri);
        }
    }

    private void initPopup() {
        popAddPost = new Dialog(this);
        popAddPost.setContentView(R.layout.popup_add_post);
        popAddPost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popAddPost.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        popAddPost.getWindow().getAttributes().gravity = Gravity.TOP;

        popupUserPhoto = popAddPost.findViewById(R.id.popup_user_photo);
        popupPostPhoto = popAddPost.findViewById(R.id.popup_img);
        popupTitle = popAddPost.findViewById(R.id.popup_title);
        popupDescription = popAddPost.findViewById(R.id.popup_description);
        popupAddButton = popAddPost.findViewById(R.id.popup_add);
        popupProgressBar = popAddPost.findViewById(R.id.popup_progressBar);

        Glide.with(this).load(currentUser.getPhotoUrl()).into(popupUserPhoto);

        popupAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupProgressBar.setVisibility(View.VISIBLE);
                popupAddButton.setVisibility(View.INVISIBLE);

                if (!popupTitle.getText().toString().isEmpty() && !popupDescription.getText().toString().isEmpty()
                && pickedImgUri != null) {
                    // TODO: Tao moi bai viet va them vao firebase db
                    // 1st: Upload hinh anh len firebase storage
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("blog_images");
                    final StorageReference imageFilePath = storageReference.child(pickedImgUri.getLastPathSegment());

                    imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageDownloadLink = uri.toString();
                                    // Bay gio da co the tao bai viet

                                    Post post = new Post(popupTitle.getText().toString(), popupDescription.getText().toString(),
                                            imageDownloadLink, currentUser.getUid(), currentUser.getPhotoUrl().toString());

                                    // them bai viet vao firebase database
                                    addPost(post);

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    showMessage(e.getMessage());
                                    popupProgressBar.setVisibility(View.INVISIBLE);
                                    popupAddButton.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    });

                } else {
                    showMessage("Vui lòng kiểm tra lại tiêu đề, nội dung bài viết và hình ảnh cho bài viết!");
                    popupProgressBar.setVisibility(View.INVISIBLE);
                    popupAddButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void addPost(Post post) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Posts").push();

        // lay id duy nhat cho moi bai viet
        String key = myRef.getKey();
        post.setPostKey(key);

        // them du lieu vao firebase database
        myRef.setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showMessage("Bài viết đã được tạo!");
                popupProgressBar.setVisibility(View.INVISIBLE);
                popupAddButton.setVisibility(View.VISIBLE);

                // reset cac truong nhap cua popup
                popupTitle.setText("");
                popupDescription.setText("");
                popupPostPhoto.setImageURI(null);

                // close popup
                popAddPost.dismiss();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showMessage("Đã xảy ra lỗi khi thêm dữ liệu!");
            }
        });
    }

    private void showMessage(String message) {
        Toast.makeText(Home.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //showMessage("Coming soon!");
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragment()).commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            fab.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle("Trang chủ");
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new HomeFragment()).commit();
        } else if (id == R.id.nav_profile) {
            fab.setVisibility(View.INVISIBLE);
            getSupportActionBar().setTitle("Hồ sơ");
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new ProfileFragment()).commit();
        } else if (id == R.id.nav_support) {
            fab.setVisibility(View.INVISIBLE);
            getSupportActionBar().setTitle("Chăm sóc khách hàng");

            getSupportFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragment()).commit();
        } else if (id == R.id.nav_signout) {
            FirebaseAuth.getInstance().signOut();
            Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(loginActivity);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*@Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }*/

    public void updateNavHeader() {
        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.nav_username);
        TextView navUserEmail = headerView.findViewById(R.id.nav_user_email);
        ImageView navUserPhoto = headerView.findViewById(R.id.nav_user_photo);

        navUsername.setText(currentUser.getDisplayName());
        navUserEmail.setText(currentUser.getEmail());

        // load user photo
        Glide.with(this).load(currentUser.getPhotoUrl()).into(navUserPhoto);

    }
}
