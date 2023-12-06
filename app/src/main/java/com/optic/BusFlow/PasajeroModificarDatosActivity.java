package com.optic.BusFlow;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PasajeroModificarDatosActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText emergencyPhoneEditText;
    private EditText fullNameEditText;
    private EditText phoneEditText;
    private Button saveButton;

    private FirebaseAuth auth;
    private DatabaseReference userRef;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, PasajeroMapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conductor_modificar_datos);

        emailEditText = findViewById(R.id.emailEditText);
        emergencyPhoneEditText = findViewById(R.id.emergencyPhoneEditText);
        fullNameEditText = findViewById(R.id.fullNameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        saveButton = findViewById(R.id.saveButton);

        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child("Pasajero").child(auth.getUid());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Obtener los datos actuales del usuario y mostrarlos en los campos de entrada
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String emergencyPhone = dataSnapshot.child("emergencyPhone").getValue(String.class);
                    String fullName = dataSnapshot.child("fullName").getValue(String.class);
                    String phone = dataSnapshot.child("phone").getValue(String.class);

                    emailEditText.setText(email);
                    emergencyPhoneEditText.setText(emergencyPhone);
                    fullNameEditText.setText(fullName);
                    phoneEditText.setText(phone);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejar errores de la base de datos si es necesario
            }
        });

        // Dentro del onClickListener del botón saveButton
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener los nuevos valores de los campos de entrada
                String newEmail = emailEditText.getText().toString();
                String newEmergencyPhone = emergencyPhoneEditText.getText().toString();
                String newFullName = fullNameEditText.getText().toString();
                String newPhone = phoneEditText.getText().toString();

                // Actualizar los datos del usuario en Firebase
                userRef.child("email").setValue(newEmail);
                userRef.child("emergencyPhone").setValue(newEmergencyPhone);
                userRef.child("fullName").setValue(newFullName);
                userRef.child("phone").setValue(newPhone);

                // Mostrar una notificación
                showToast("Los cambios se han guardado correctamente");

                // Volver a la actividad ConductorMapActivity
                Intent intent = new Intent(PasajeroModificarDatosActivity.this, PasajeroMapActivity.class);
                startActivity(intent);
                finish(); // Cierra la actividad actual
            }
        });


    }
}