package com.optic.BusFlow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PasajeroMenuAccount extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, PasajeroMapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pasajero_menu_account);

        // Referencias a los botones
        Button modifyDataButton = findViewById(R.id.modifyDataButton);
        Button reportIssueButton = findViewById(R.id.reportIssueButton);

        // Agregar clics de botón para dirigirse a otras actividades
        modifyDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dirigirse a la actividad para modificar datos del conductor (ConductorModificarDatosActivity)
                Intent intent = new Intent(PasajeroMenuAccount.this, PasajeroModificarDatosActivity.class);
                startActivity(intent);
            }
        });

        reportIssueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dirigirse a la actividad para informar un problema (ConductorInformarProblemaActivity)
                Intent intent = new Intent(PasajeroMenuAccount.this, PasajeroInformarProblemaActivity.class);
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setSelectedItemId(R.id.menu_account);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_home) {
                goToHome();
                return true;
            } else if (itemId == R.id.menu_account) {
                goToMenu();
                return true;
            } else if (itemId == R.id.menu_exit) {

                logout();
                return true;
            }
            return false;
        });
        fetchAndSetUserName();


    }

    private void goToHome() {
        Intent intent = new Intent(this, PasajeroMapActivity.class);

        // Inicia la actividad HomeActivity
        startActivity(intent);

        // Opcionalmente, puedes finalizar la actividad actual si no deseas volver a ella
        finish();

    }

    private void goToMenu() {
        Intent intent = new Intent(this, PasajeroMenuAccount.class);

        // Inicia la actividad HomeActivity
        startActivity(intent);

        // Opcionalmente, puedes finalizar la actividad actual si no deseas volver a ella
        finish();

    }

    private void logout() {
        Intent intent = new Intent(this, PasajeroMenuAccount.class);

        // Inicia la actividad HomeActivity
        startActivity(intent);

        // Opcionalmente, puedes finalizar la actividad actual si no deseas volver a ella
        finish();

    }



    private void fetchAndSetUserName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // El usuario está autenticado, obtén su UID
            String uid = user.getUid();

            // Obtén una referencia a la base de datos de Firebase Realtime Database
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child("Pasajero").child(uid);

            // Agrega un listener para obtener los datos del usuario
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String fullName = dataSnapshot.child("fullName").getValue(String.class);

                        if (fullName != null && !fullName.isEmpty()) {
                            // Configura el nombre completo en el TextView
                            TextView nameTextView = findViewById(R.id.nameTextView);
                            nameTextView.setText(fullName);
                        } else {
                            Log.d("PasajeroMenuAccount", "El nombre de usuario en Firebase es nulo o vacío.");
                        }
                    } else {
                        Log.d("PasajeroMenuAccount", "No se encontraron datos para el usuario en Firebase.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("PasajeroMenuAccount", "Error al obtener datos de Firebase: " + databaseError.getMessage());
                }
            });

        } else {
            Log.d("PasajeroMenuAccount", "El usuario de Firebase es nulo.");
        }
    }
}
