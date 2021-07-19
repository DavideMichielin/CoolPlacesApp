package it.units.placesapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import it.units.placesapp.databinding.ActivityAccountBinding;
import utils.Monument;

public class AccountActivity extends AppCompatActivity {
    private ActivityAccountBinding binding;
    private DatabaseReference mDatabase;
    private FirebaseAuth firebaseAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.back.setOnClickListener(backButtonListener);
        binding.changeInformation.setOnClickListener(changeInformationListener);
        binding.logoutButton.setOnClickListener(logoutListener);


    }

    @Override
    protected void onStart() {
        super.onStart();
        ArrayList<Monument> monuments = new ArrayList<>();
        firebaseAuth =FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        String uid = user.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userName = dataSnapshot.child(uid).child("name").getValue(String.class);
                String userSurname = dataSnapshot.child(uid).child("surname").getValue(String.class);
                String email = dataSnapshot.child(uid).child("email").getValue(String.class);
                binding.completeName.setText(userName + " " + userSurname);
                binding.email.setText(email);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mDatabase = mDatabase.child(uid).child("favorite");
        ArrayAdapter<Monument> monumentArrayAdapter = new ArrayAdapter<Monument>(AccountActivity.this, R.layout.list_item, R.id.monumentName, monuments);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Monument m = ds.getValue(Monument.class);
                    monuments.add(m);
                }
                binding.listview.setAdapter(monumentArrayAdapter);
                binding.listview.setEmptyView(binding.empty);
                binding.listview.setOnItemClickListener((parent, view, position, id) -> {
                    Monument m = monuments.get(position);
                    Intent intent = new Intent(AccountActivity.this, MonumentActivity.class);
                    intent.putExtra("monumentName", m.getName());
                    intent.putExtra("latitude", m.getLatitude());
                    intent.putExtra("longitude", m.getLongitude());
                    startActivity(intent);
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }

    View.OnClickListener logoutListener = v -> {
        firebaseAuth.signOut();
        Intent intent = new Intent(AccountActivity.this, MapsActivity.class);
        startActivity(intent);
        finish();
    };

    View.OnClickListener backButtonListener = v -> {
        finish();
    };

    View.OnClickListener changeInformationListener = v -> {
        Intent intent = new Intent(AccountActivity.this, ChangeInfoActivity.class);
        startActivity(intent);
    };

}