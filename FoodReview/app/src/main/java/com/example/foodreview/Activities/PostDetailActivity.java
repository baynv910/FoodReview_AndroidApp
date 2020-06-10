package com.example.foodreview.Activities;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodreview.Adapters.CommentAdapter;
import com.example.foodreview.Models.Comment;
import com.example.foodreview.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    ImageView imgPost, imgUserPhoto, imgCurrentUserPhoto;
    TextView txtPostTitle, txtPostDescription, txtPostDateName;
    EditText editTextComment;
    Button btnAddComment;

    FirebaseDatabase database;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String postKey;

    RecyclerView rvComment;
    CommentAdapter commentAdapter;

    List<Comment> listComment;
    static String COMMENT_KEY = "Comment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getSupportActionBar().hide();

        // init
        rvComment = findViewById(R.id.rv_comments);
        imgPost = findViewById(R.id.post_detail_img);
        imgUserPhoto = findViewById(R.id.post_detail_user_photo);
        imgCurrentUserPhoto = findViewById(R.id.post_detail_current_user_photo);

        txtPostTitle = findViewById(R.id.post_detail_title);
        txtPostDescription = findViewById(R.id.post_detail_description);
        txtPostDateName = findViewById(R.id.post_detail_date_name);

        editTextComment = findViewById(R.id.post_detail_comment);
        btnAddComment = findViewById(R.id.post_detail_btn_add_comment);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnAddComment.setVisibility(View.INVISIBLE);
                DatabaseReference commentRef = database.getReference(COMMENT_KEY).child(postKey).push();
                String commentContent = editTextComment.getText().toString();
                String uId = mUser.getUid();
                String uName = mUser.getDisplayName();
                String uImg = mUser.getPhotoUrl().toString();
                Comment comment = new Comment(uId, commentContent, uImg, uName);

                commentRef.setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showMessage("Bình luận đã được thêm!");
                        editTextComment.setText("");
                        btnAddComment.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showMessage("Không thể thêm bình luận! " + e.getMessage());
                    }
                });
            }
        });

        String postImage = getIntent().getExtras().getString("postImage");
        Glide.with(this).load(postImage).into(imgPost);

        String userPhoto = getIntent().getExtras().getString("userPhoto");
        Glide.with(this).load(userPhoto).into(imgUserPhoto);

        String postTitle = getIntent().getExtras().getString("title");
        txtPostTitle.setText(postTitle);

        String postDescription = getIntent().getExtras().getString("description");
        txtPostDescription.setText(postDescription);

        // hinh dai dien o khung binh luan
        Glide.with(this).load(mUser.getPhotoUrl()).into(imgCurrentUserPhoto);

        postKey = getIntent().getExtras().getString("postKey");

        String date = timestampToString(getIntent().getExtras().getLong("postDate"));

        txtPostDateName.setText(date);

        // init recycle view comment
        initRvComments();
    }

    private void initRvComments() {

        rvComment.setLayoutManager(new LinearLayoutManager(this));
        //commentAdapter
        DatabaseReference commentRef = database.getReference(COMMENT_KEY).child(postKey);

        commentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listComment = new ArrayList<>();

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    listComment.add(comment);
                }

                commentAdapter = new CommentAdapter(getApplicationContext(), listComment);
                rvComment.setAdapter(commentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private String timestampToString(long time) {

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy hh:mm", calendar).toString();

        return date;
    }
}
