package com.optic.BusFlow;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.util.regex.Pattern;

public class ConductorModificarDatosActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText emergencyPhoneEditText;
    private EditText fullNameEditText;
    private EditText phoneEditText;
    private Spinner rutaSpinner;

    private Button saveButton;

    private FirebaseAuth auth;
    private DatabaseReference userRef;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ConductorMapActivity.class);
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
        rutaSpinner = findViewById(R.id.rutaSpinner);

        saveButton = findViewById(R.id.saveButton);
        String[] rutas = new String[]{"301", "302", "Quillota","Valparaiso","Concon","Quintero","Santiago","La calera"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, rutas);
        rutaSpinner.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child("Conductor").child(auth.getUid());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Obtener los datos actuales del usuario y mostrarlos en los campos de entrada
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String emergencyPhone = dataSnapshot.child("emergencyPhone").getValue(String.class);
                    String fullName = dataSnapshot.child("fullName").getValue(String.class);
                    String phone = dataSnapshot.child("phone").getValue(String.class);
                    String ruta = dataSnapshot.child("ruta").getValue(String.class);


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

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener los nuevos valores de los campos de entrada
                String newEmail = emailEditText.getText().toString().trim();
                String newEmergencyPhone = emergencyPhoneEditText.getText().toString().trim();
                String newFullName = fullNameEditText.getText().toString().trim();
                String newPhone = phoneEditText.getText().toString().trim();
                String newRuta = rutaSpinner.getSelectedItem().toString();

                if (newEmail.isEmpty() || newEmergencyPhone.isEmpty() || newFullName.isEmpty() || newPhone.isEmpty()) {
                    showToast("Todos los campos son obligatorios");
                    return;
                }

                if (!Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+").matcher(newEmail).matches()) {
                    showToast("Email no es válido");
                    return;
                }

                // Puedes aplicar otras validaciones como longitud de teléfono, caracteres válidos para el nombre, etc.
                // Por ejemplo:
                if (newPhone.length() != 8 || !Pattern.compile("[0-9]+").matcher(newPhone).matches()) {
                    showToast("Número de teléfono no válido");
                    return;
                }

                if (newEmergencyPhone.length() != 8 || !Pattern.compile("[0-9]+").matcher(newEmergencyPhone).matches()) {
                    showToast("Número de teléfono de emergencia no válido");
                    return;
                }

                if (newRuta.isEmpty()) {
                    showToast("La ruta es obligatoria");
                    return;
                }

                // Actualizar los datos del usuario en Firebase
                userRef.child("email").setValue(newEmail);
                userRef.child("emergencyPhone").setValue(newEmergencyPhone);
                userRef.child("fullName").setValue(newFullName);
                userRef.child("phone").setValue(newPhone);
                userRef.child("ruta").setValue(newRuta);

                // Mostrar una notificación
                showToast("Los cambios se han guardado correctamente");

                // Volver a la actividad ConductorMapActivity
                Intent intent = new Intent(ConductorModificarDatosActivity.this, ConductorMapActivity.class);
                startActivity(intent);
                finish(); // Cierra la actividad actual
            }
        });


    }
}
