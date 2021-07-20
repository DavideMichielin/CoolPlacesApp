package it.units.placesapp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import it.units.placesapp.databinding.ActivityChangeInfoBinding;


public class ChangeInfoActivity extends AppCompatActivity {
    private ActivityChangeInfoBinding binding;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangeInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.submit.setOnClickListener(submitChange);
        binding.resetPassword.setOnClickListener(changePassword);
        binding.back.setOnClickListener(backListener);
    }

    View.OnClickListener submitChange = v -> {
        String name = binding.changeName.getText().toString();
        String surname = binding.changeSurname.getText().toString();
        if (name.isEmpty() &&
                surname.isEmpty()) {
            Toast.makeText(ChangeInfoActivity.this, R.string.atLeastOne, Toast.LENGTH_LONG).show();
        } else {
            if (!name.isEmpty()) {
                changeName(name);
            }
            if (!surname.isEmpty()) {
                changeSurname(surname);
            }
        }
    };


    View.OnClickListener changePassword = v -> {
        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
        passwordResetDialog.setTitle(R.string.passwordReset);
        passwordResetDialog.setMessage(R.string.insertPassword6Character);
        LinearLayout layout = new LinearLayout(v.getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText oldPassword = new EditText(v.getContext());
        oldPassword.setHint(R.string.oldPassword);
        layout.addView(oldPassword);
        final EditText newPassword = new EditText(v.getContext());
        newPassword.setHint(R.string.newPassword);
        layout.addView(newPassword);
        passwordResetDialog.setView(layout);

        passwordResetDialog.setPositiveButton(R.string.change, (dialog, which) -> {
            String verify = oldPassword.getText().toString();
            String password = newPassword.getText().toString();
            if (password.length() < 6) {
                Toast.makeText(ChangeInfoActivity.this, R.string.shortPassword, Toast.LENGTH_LONG).show();
            } else {
                AuthCredential authCredential = EmailAuthProvider.getCredential(Objects.requireNonNull(user.getEmail()), verify);
                user.reauthenticate(authCredential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.updatePassword(password)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(ChangeInfoActivity.this, R.string.passwordUpdated, Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(ChangeInfoActivity.this, R.string.wrongPassword, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        passwordResetDialog.show();
    };

    View.OnClickListener backListener = v -> finish();

    private void changeName(String name) {
        ref.child("users").child(user.getUid()).child("name").setValue(name);
        Toast.makeText(ChangeInfoActivity.this, R.string.nameUpdated, Toast.LENGTH_SHORT).show();
    }

    private void changeSurname(String surname) {
        ref.child("users").child(user.getUid()).child("surname").setValue(surname);
        Toast.makeText(ChangeInfoActivity.this, R.string.surnameUpdated, Toast.LENGTH_SHORT).show();
    }

}