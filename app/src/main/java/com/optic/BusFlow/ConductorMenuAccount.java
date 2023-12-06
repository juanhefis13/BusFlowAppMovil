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

public class ConductorMenuAccount extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ConductorMapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conductor_menu_account);

        // Referencias a los botones
        Button modifyDataButton = findViewById(R.id.modifyDataButton);
        Button reportIssueButton = findViewById(R.id.reportIssueButton);

        // Agregar clics de botón para dirigirse a otras actividades
        modifyDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dirigirse a la actividad para modificar datos del conductor (ConductorModificarDatosActivity)
                Intent intent = new Intent(ConductorMenuAccount.this, ConductorModificarDatosActivity.class);
                startActivity(intent);
            }
        });

        reportIssueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dirigirse a la actividad para informar un problema (ConductorInformarProblemaActivity)
                Intent intent = new Intent(ConductorMenuAccount.this, ConductorInformarProblemaActivity.class);
                startActivity(intent);
            }
        });

        // Obtén la instancia de BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Desactiva la tintura de íconos para mostrar los colores originales
        bottomNavigationView.setItemIconTintList(null);

        // Establece el ítem seleccionado por defecto
        bottomNavigationView.setSelectedItemId(R.id.menu_account);

        // Establece el ítem seleccionado por defecto
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
        Intent intent = new Intent(this, ConductorMapActivity.class);

        // Inicia la actividad HomeActivity
        startActivity(intent);

        // Opcionalmente, puedes finalizar la actividad actual si no deseas volver a ella
        finish();

    }

    private void goToMenu() {
        Intent intent = new Intent(this, ConductorMenuAccount.class);

        // Inicia la actividad HomeActivity
        startActivity(intent);

        // Opcionalmente, puedes finalizar la actividad actual si no deseas volver a ella
        finish();

    }

    private void logout() {
        Intent intent = new Intent(this, ConductorMapActivity.class);

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
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child("Conductor").child(uid);

            // Agrega un listener para obtener los datos del usuario
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Configura el nombre completo en el TextView
                        String fullName = dataSnapshot.child("fullName").getValue(String.class);
                        String horarioInicio = dataSnapshot.child("horario_inicio").getValue(String.class);
                        String horarioFin = dataSnapshot.child("horario_fin").getValue(String.class);

                        TextView nameTextView = findViewById(R.id.nameTextView);
                        TextView horarioInicioTextView = findViewById(R.id.horarioInicioTextView);
                        TextView horarioFinTextView = findViewById(R.id.horarioFinTextView);

                        if (fullName != null && !fullName.isEmpty()) {
                            nameTextView.setText(fullName);
                        }
                        if (horarioInicio != null && !horarioInicio.isEmpty()) {
                            horarioInicioTextView.setText("Horario de inicio: " + horarioInicio);
                        }
                        if (horarioFin != null && !horarioFin.isEmpty()) {
                            horarioFinTextView.setText("Horario de fin: " + horarioFin);
                        }
                    } else {
                        Log.d("ConductorMenuAccount", "No se encontraron datos para el usuario en Firebase.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("ConductorMenuAccount", "Error al obtener datos de Firebase: " + databaseError.getMessage());
                }
            });

        } else {
            Log.d("ConductorMapActivity", "El usuario de Firebase es nulo.");
        }
    }

}
