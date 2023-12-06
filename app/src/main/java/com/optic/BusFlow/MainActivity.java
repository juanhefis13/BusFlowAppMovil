package com.optic.BusFlow;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mConductor, mPasajero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mConductor = (Button) findViewById(R.id.Conductor);
        mPasajero = (Button) findViewById(R.id.Pasajero);

        mConductor.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, ConductorLoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mPasajero.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, PasajeroLoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}