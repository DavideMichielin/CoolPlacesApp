package it.units.placesapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.units.placesapp.databinding.ActivityMonumentBinding;
import utils.MonumentImageHandler;
import utils.Review;


public class MonumentActivity extends AppCompatActivity {

    private ActivityMonumentBinding binding;
    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String name, latitude, longitude, uid;
    double latitudeD, longitudeD;
    boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMonumentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.back.setOnClickListener(backButtonListener);
        binding.uploadImage.setOnClickListener(uploadImageListener);
        binding.likeButton.setOnClickListener(addOrRemoveMonumentFromFavourite);
        binding.uploadReview.setOnClickListener(newReview);
        name = getIntent().getStringExtra("monumentName");
        latitudeD = getIntent().getDoubleExtra("latitude", 0);
        longitudeD = getIntent().getDoubleExtra("longitude", 0);
        latitude = Double.toString(latitudeD).replace(".", "");
        longitude = Double.toString(longitudeD).replace(".", "");
        getReview();

    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.nameMonument.setText(name);

        if (user == null) {
            binding.likeButton.setVisibility(View.GONE);
            binding.uploadImage.setVisibility(View.GONE);
            binding.uploadReview.setVisibility(View.GONE);
        } else {
            uid = user.getUid();
            checkIfMonumentsIsPrefer(name);
        }

        showImageMonument(latitude, longitude);


    }

    private void checkIfMonumentsIsPrefer(String name) {
        Query query = FirebaseDatabase.getInstance().getReference("users").child(uid).child("favorite").orderByChild("name").equalTo(name);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    binding.likeButton.setBackgroundResource(R.drawable.round_button_favourite);
                    isFavorite = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showImageMonument(String latitude, String longitude) {
        String imageFolder = latitude + "_" + longitude;
        DatabaseReference imageRef = rootRef.child("monuments").child(imageFolder);
        List<SlideModel> slider = new ArrayList<>();
        imageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MonumentImageHandler m = dataSnapshot.getValue(MonumentImageHandler.class);
                    slider.add(new SlideModel(m.getImageUrl(), "", ScaleTypes.CENTER_INSIDE));
                }
                binding.imageMonument.setImageList(slider, ScaleTypes.FIT);
                if(slider.size() == 0){
                    binding.imageMonument.setBackgroundResource(R.drawable.background_places_no);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getReview() {
        binding.progressLayout.setVisibility(View.VISIBLE);
        binding.containerLayout.setAlpha(0.4f);
        DatabaseReference reviewRef = rootRef.child("review").child(latitude + "_" + longitude);
        reviewRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Review> reviews = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Review m = ds.getValue(Review.class);
                    reviews.add(m);
                }
                ArrayAdapter reviewAdapter = new ArrayAdapter(MonumentActivity.this, R.layout.list_item_review, R.id.review, reviews);
                binding.listview.setAdapter(reviewAdapter);
                binding.listview.setEmptyView(binding.empty);
                binding.progressLayout.setVisibility(View.GONE);
                binding.containerLayout.setAlpha(1f);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }

    View.OnClickListener addOrRemoveMonumentFromFavourite = v -> {
        HashMap<String, Object> infoMonument = new HashMap<>();
        infoMonument.put("name", name);
        infoMonument.put("latitude", latitudeD);
        infoMonument.put("longitude", longitudeD);
        if (!isFavorite) {
            rootRef.child("users").child(uid).child("favorite").child(latitude + "_" + longitude).setValue(infoMonument);
            binding.likeButton.setBackgroundResource(R.drawable.round_button_favourite);
            isFavorite = true;
            Toast.makeText(MonumentActivity.this, R.string.addFavourite, Toast.LENGTH_SHORT).show();
        } else {
            rootRef.child("users").child(uid).child("favorite").child(latitude + "_" + longitude).removeValue();
            binding.likeButton.setBackgroundResource(R.drawable.round_button);
            isFavorite = false;
            Toast.makeText(MonumentActivity.this, R.string.removeFavorite, Toast.LENGTH_SHORT).show();
        }
    };

    View.OnClickListener newReview = v -> {
        Intent intent = new Intent(MonumentActivity.this, UploadReviewActivity.class);
        intent.putExtra("latitude", latitudeD);
        intent.putExtra("longitude", longitudeD);
        startActivity(intent);
    };


    View.OnClickListener backButtonListener = v -> finish();

    View.OnClickListener uploadImageListener = v -> {
        Intent intent = new Intent(MonumentActivity.this, UploadImageActivity.class);
        intent.putExtra("latitude", latitudeD);
        intent.putExtra("longitude", longitudeD);
        startActivity(intent);
    };
}