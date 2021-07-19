package it.units.placesapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

import it.units.placesapp.databinding.ActivityRegistrationBinding;

public class RegistrationActivity extends AppCompatActivity {

    private ActivityRegistrationBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.createUser.setOnClickListener(registrationListener);

    }

    View.OnClickListener registrationListener = v -> {
        final String email = binding.email.getText().toString();
        final String password = binding.password.getText().toString();
        final String name = binding.name.getText().toString();
        final String surname = binding.surname.getText().toString();
        if (checkField(email, password, name, surname)) {
            Toast.makeText(RegistrationActivity.this, R.string.fieldEmpty, Toast.LENGTH_SHORT).show();
        } else if (checkPassword(password)) {
            Toast.makeText(RegistrationActivity.this, R.string.shortPassword, Toast.LENGTH_SHORT).show();
        } else {
            auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(RegistrationActivity.this, new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    HashMap<String, String> data = new HashMap<>();
                    data.put("email", email);
                    data.put("name", name);
                    data.put("surname", surname);
                    String user_id = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                    data.put("uid", user_id);
                    DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference();
                    current_user_db.child("users").child(user_id).setValue(data);
                    Intent intent = new Intent(RegistrationActivity.this, MapsActivity.class);
                    startActivity(intent);
                    finish();
                }


            }).addOnFailureListener(RegistrationActivity.this, e -> Toast.makeText(RegistrationActivity.this, e.getMessage(), Toast.LENGTH_LONG).show());
        }
        ;
    };

    private boolean checkField(String email, String password, String name, String surname) {
        return email.isEmpty() || password.isEmpty() || name.isEmpty() || surname.isEmpty();
    }

    private boolean checkPassword(String password) {
        return password.length() < 5;
    }

}
