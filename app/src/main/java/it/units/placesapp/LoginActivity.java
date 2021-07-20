package it.units.placesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

import it.units.placesapp.databinding.ActivityLoginBinding;


public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String text = binding.registration.getText().toString();

        SpannableString ss = new SpannableString(text);
        ClickableSpan cs = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        };
        if (Locale.getDefault().getDisplayLanguage().equals("italiano")) {
            ss.setSpan(cs, 27, 37, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            ss.setSpan(cs, 20, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        binding.registration.setText(ss);
        binding.registration.setMovementMethod(LinkMovementMethod.getInstance());
        binding.login.setOnClickListener(loginListener);
    }

    View.OnClickListener loginListener = v -> {
        final String email = binding.email.getText().toString();
        final String password = binding.password.getText().toString();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, R.string.completeAllField, Toast.LENGTH_LONG).show();
        } else {
            auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(LoginActivity.this, authResult -> {
                Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                startActivity(intent);
                finish();
            }).addOnFailureListener(e -> Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show());
        }
    };
}