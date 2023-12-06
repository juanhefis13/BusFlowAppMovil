package com.optic.BusFlow;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;

import android.content.Intent;


import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.FirebaseDatabase;

public class PasajeroRegisterActivity extends AppCompatActivity {
    private EditText mEmail, mPassword;
    private Button mRegister;
    private FirebaseAuth mAuth;

    private EditText mPasswordConfirm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pasajero_register);

        mAuth = FirebaseAuth.getInstance();

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mRegister = findViewById(R.id.register);
        mPasswordConfirm = findViewById(R.id.passwordConfirm);
        CheckBox termsCheckbox = findViewById(R.id.termsCheckbox);
        Button registerButton = findViewById(R.id.register);




        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!termsCheckbox.isChecked()) {
                    Toast.makeText(PasajeroRegisterActivity.this, "Debe aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
                    return;
                }

                final String email = mEmail.getText().toString().trim();
                final String password = mPassword.getText().toString().trim();
                final String passwordConfirm = mPasswordConfirm.getText().toString().trim();
                final String fullName = ((EditText) findViewById(R.id.fullName)).getText().toString().trim();
                final String phone = ((EditText) findViewById(R.id.phone)).getText().toString().trim();
                final String rut = ((EditText) findViewById(R.id.rut)).getText().toString().trim();
                final String emergencyPhone = ((EditText) findViewById(R.id.emergencyPhone)).getText().toString().trim();

                if(email.isEmpty() || password.isEmpty() || fullName.isEmpty() ||
                        phone.isEmpty() ||  rut.isEmpty() ||
                        emergencyPhone.isEmpty()) {
                    Toast.makeText(PasajeroRegisterActivity.this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+").matcher(email).matches()) {
                    Toast.makeText(PasajeroRegisterActivity.this, "Email no es válido", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(PasajeroRegisterActivity.this, "La contraseña es demasiado corta", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(passwordConfirm)) {
                    Toast.makeText(PasajeroRegisterActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!fullName.matches("[a-zA-Z ]+")) {
                    Toast.makeText(PasajeroRegisterActivity.this, "El nombre contiene caracteres no válidos", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (phone.length() != 8 || !Pattern.compile("[0-9]+").matcher(phone).matches()) {
                    Toast.makeText(PasajeroRegisterActivity.this, "Número de teléfono no válido", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (rut.length() != 8 || !Pattern.compile("[0-9a-zA-Z]+").matcher(rut).matches()) {
                    Toast.makeText(PasajeroRegisterActivity.this, "Cedula de identidad no valida", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (emergencyPhone.length() != 8 || !Pattern.compile("[0-9]+").matcher(emergencyPhone).matches()) {
                    Toast.makeText(PasajeroRegisterActivity.this, "Número de teléfono de emergencia no válido", Toast.LENGTH_SHORT).show();
                    return;
                }


                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(PasajeroRegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String user_id = mAuth.getCurrentUser().getUid();
                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Pasajero").child(user_id);

                            Map<String, String> userData = new HashMap<>();
                            userData.put("email", email);
                            userData.put("fullName", fullName);
                            userData.put("phone", phone);
                            userData.put("rut", rut);
                            userData.put("emergencyPhone", emergencyPhone);

                            current_user_db.setValue(userData);

                            Toast.makeText(PasajeroRegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(PasajeroRegisterActivity.this, PasajeroLoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(PasajeroRegisterActivity.this, "El correo electrónico ya está en uso.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(PasajeroRegisterActivity.this, "Error en el registro: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

            }

        });

    }
}