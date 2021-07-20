package it.units.placesapp;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import it.units.placesapp.databinding.ActivityUploadImageBinding;

public class UploadImageActivity extends AppCompatActivity {

    private ActivityUploadImageBinding binding;
    FirebaseStorage storage;
    DatabaseReference referenceDatabase = FirebaseDatabase.getInstance().getReference().child("monuments");
    Uri imageUri;
    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage = FirebaseStorage.getInstance();

        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);

        binding.back.setOnClickListener(backButtonListener);
        binding.chooseImage.setOnClickListener(v -> mGetContent.launch("image/*"));
        binding.uploadImage.setOnClickListener(v -> uploadImage());
    }

    View.OnClickListener backButtonListener = v -> finish();


    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            if (result != null) {
                binding.imageMonument.setImageURI(result);
                imageUri = result;
            }
        }
    });

    private void uploadImage() {
        if (imageUri != null) {
            StorageReference reference = storage.getReference().child("images/" + latitude + "_" + longitude + "/" + imageUri.getLastPathSegment());
            reference.putFile(imageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    reference.getDownloadUrl().addOnSuccessListener(uri -> {
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("imageurl", String.valueOf(uri));
                        String saveLatitude = Double.toString(latitude).replace(".", "");
                        String saveLongitude = Double.toString(longitude).replace(".", "");
                        DatabaseReference referenceImage = referenceDatabase.child(saveLatitude + "_" + saveLongitude);
                        referenceImage.push().setValue(hashMap).addOnSuccessListener(unused -> {
                            Toast.makeText(UploadImageActivity.this, R.string.imageUploaded, Toast.LENGTH_SHORT).show();
                            binding.imageMonument.setImageResource(R.drawable.add_image_icons);
                            imageUri = null;
                        });
                    });
                } else {
                    Toast.makeText(UploadImageActivity.this, R.string.errorImageUpload, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(UploadImageActivity.this, R.string.selectAImage, Toast.LENGTH_SHORT).show();
        }
    }

}