package com.optic.BusFlow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ConductorLoginActivity extends AppCompatActivity {
    private EditText mEmail, mPassword;
    private Button mLogin, mRegistro;
    private CheckBox mCheckBox;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conductor_login);

        mAuth = FirebaseAuth.getInstance();
        mCheckBox = findViewById(R.id.checkBox);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mLogin = findViewById(R.id.login);
        mRegistro = findViewById(R.id.registro);

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    final String email = user.getEmail();
                    if (email != null) {
                        // Verificar si el usuario es un conductor
                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child("Conductor");

                        Query query = usersRef.orderByChild("email").equalTo(email);

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // El usuario es un conductor, redirige a la actividad de conductor
                                    Intent intent = new Intent(ConductorLoginActivity.this, ConductorMapActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // El usuario no es un conductor, muestra un mensaje de error
                                    Toast.makeText(ConductorLoginActivity.this, "No eres un conductor", Toast.LENGTH_SHORT).show();
                                    mAuth.signOut(); // Cierra la sesión del usuario no conductor
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Manejar el error de base de datos si es necesario
                                Log.e("ConductorLoginActivity", "Error en la consulta de usuario: " + databaseError.getMessage());
                            }
                        });
                    }
                }
            }
        };

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString().trim();
                final String password = mPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(ConductorLoginActivity.this, "Los campos de correo y contraseña no pueden estar vacíos", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(ConductorLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("Login", "Inicio de sesión exitoso");
                        } else {
                            Log.e("Login", "Error al iniciar sesión: " + task.getException().getMessage());
                            Toast.makeText(ConductorLoginActivity.this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        TextView forgotPassword = findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abre una nueva actividad para recuperar contraseña o muestra un cuadro de diálogo
                // donde los usuarios pueden ingresar su correo electrónico para restablecer la contraseña.
                // Puedes usar un Intent para abrir una nueva actividad o mostrar un cuadro de diálogo.
                // Por ejemplo:
                Intent intent = new Intent(ConductorLoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        mRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ConductorLoginActivity.this, ConductorRegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuthListener != null) {
            mAuth.removeAuthStateListener(firebaseAuthListener);
        }
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("mantener_sesion_activa", mCheckBox.isChecked());
        editor.apply();
    }
}
