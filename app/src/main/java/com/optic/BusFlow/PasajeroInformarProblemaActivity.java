package com.optic.BusFlow;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

public class PasajeroInformarProblemaActivity extends AppCompatActivity {

    private Spinner problemTypeSpinner;
    private EditText messageEditText;
    private Button sendButton;

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
        setContentView(R.layout.activity_pasajero_informar_problema);

        problemTypeSpinner = findViewById(R.id.problemTypeSpinner);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        String[] problemas = {"Robo", "Accidente", "Retraso", "Otro"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, problemas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        problemTypeSpinner.setAdapter(adapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedProblemType = problemTypeSpinner.getSelectedItem().toString();
                String message = messageEditText.getText().toString();

                // Aquí puedes enviar el mensaje a través de una API o servicio

                // Muestra una alerta de envío exitoso
                Toast.makeText(PasajeroInformarProblemaActivity.this, "Mensaje enviado: Tipo de problema: " + selectedProblemType + ", Mensaje: " + message, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(PasajeroInformarProblemaActivity.this, PasajeroMapActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}


