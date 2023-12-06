package com.optic.BusFlow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText emailEditText;
    private Button resetPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailEditText = findViewById(R.id.emailEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener el correo electrónico ingresado por el usuario
                String email = emailEditText.getText().toString().trim();

                // Verificar que el campo de correo electrónico no esté vacío
                if (!TextUtils.isEmpty(email)) {
                    // Enviar una solicitud de restablecimiento de contraseña a Firebase
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // La solicitud de restablecimiento de contraseña se ha enviado con éxito
                                        Toast.makeText(ForgotPasswordActivity.this,
                                                "Se ha enviado un correo electrónico para restablecer tu contraseña.",
                                                Toast.LENGTH_SHORT).show();
                                        finish(); // Cierra la actividad de recuperación de contraseña
                                    } else {
                                        // Error al enviar la solicitud de restablecimiento de contraseña
                                        Toast.makeText(ForgotPasswordActivity.this,
                                                "Error al enviar la solicitud de restablecimiento de contraseña.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    // El campo de correo electrónico está vacío, muestra un mensaje de error
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Por favor, ingresa tu correo electrónico antes de continuar.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
