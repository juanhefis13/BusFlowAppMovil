package com.optic.BusFlow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class ConductorInformarProblemaActivity extends AppCompatActivity {

    Button button1;
    Button button2;
    Button button3;
    Button button4;
    String uid;


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
        setContentView(R.layout.activity_conductor_informar_problema);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // El usuario está logueado
            uid = user.getUid(); // Asigna el UID a la variable a nivel de clase
        } else {
            // No hay usuario logueado
            // Manejar la situación, por ejemplo, redirigiendo al usuario a la pantalla de login
            return; // Salir de onCreate si no hay usuario
        }

        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportar("Accidente");
                mostrarDialogo("Se ha enviado el problema de accidente con éxito", "Accidente button fue presionado");
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportar("Técnico");
                mostrarDialogo("Se ha enviado el problema técnico con éxito", "Técnico button fue presionado");
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportar("Pasajero");
                mostrarDialogo("Se ha enviado el problema de pasajero con éxito", "Pasajero button fue presionado");
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportar("Robo");
                mostrarDialogo("Se ha enviado el problema de robo con éxito", "Robo button fue presionado");
            }
        });
    }

    private void reportar(String tipoReporte) {
        // Referencia a la base de datos de Firebase
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child("Conductor").child(uid);

        // Agrega un listener para obtener los datos del usuario
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Crear un objeto con los datos del conductor
                    DatosConductor datosConductor = dataSnapshot.getValue(DatosConductor.class);
                    datosConductor.setTipoReporte(tipoReporte);
                    datosConductor.actualizarFechaHoraReporte();
                    // Referencia a la base de datos de Firebase para reportes
                    DatabaseReference reportRef = FirebaseDatabase.getInstance().getReference("Reporte").child(tipoReporte).push();

                    // Guardar el reporte en la estructura deseada
                    reportRef.setValue(datosConductor);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejar error
                Log.w("Firebase", "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    private void mostrarDialogo(String titulo, String mensaje) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ConductorInformarProblemaActivity.this);
        builder.setTitle(titulo);
        builder.setMessage(mensaje);
        builder.setPositiveButton("Entendido", null); // Seteamos null por ahora para que no se cierre automáticamente

        AlertDialog alertDialog = builder.create();

        // Mostramos el diálogo
        alertDialog.show();

        // Ahora que el diálogo está mostrado, podemos cambiar el color del botón
        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(getResources().getColor(R.color.alertButtonTextColor));
        }
    }
}


