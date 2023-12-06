package com.optic.BusFlow;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import android.Manifest;
import android.location.Location;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Context;

import java.util.HashMap;
import java.util.Map;


public class ConductorMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private DatabaseReference conductorDataRef;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private GeoFire geoFire;
    private ValueEventListener scheduleListener;
    private String prevHorarioInicio = null; // Inicialmente no hay horario previo
    private String prevHorarioFin = null;
    private DatabaseReference userScheduleRef;
    private String userId;
    private float currentBearing = 0f; // Variable para almacenar el bearing actual

    private LocationCallback mLocationCallback;
    private boolean isMyLocationCentered = false;
    private static final int LOCATION_REQUEST_CODE = 1000;
    private Handler autoCenterHandler = new Handler();
    private Runnable autoCenterRunnable = new Runnable() {
        @Override
        public void run() {
            centerMyLocation();
            // Re-postea el Runnable a sí mismo para ejecutarse de nuevo después del intervalo
            autoCenterHandler.postDelayed(this, AUTO_CENTER_INTERVAL);
        }
    };

    LatLng ubicacion1 = new LatLng(-33.0265242,-71.6440323);
    LatLng ubicacion2 = new LatLng(-33.0262627,-71.5539018);

    private long lastMapInteractionTime = 0;
    private static final long AUTO_CENTER_INTERVAL = 10000; // 10 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conductor_map);

        // Inicialización de elementos de UI
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        DatabaseReference geoFireRef = FirebaseDatabase.getInstance().getReference("driversAvailable");
        this.geoFire = new GeoFire(geoFireRef);




        // Obtén la instancia de BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Desactiva la tintura de íconos para mostrar los colores originales
        bottomNavigationView.setItemIconTintList(null);

        // Establece el ítem seleccionado por defecto
        bottomNavigationView.setSelectedItemId(R.id.menu_home);

        // Configura el listener para los eventos de selección de ítems
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

        setupScheduleListener();

    }
    private void setupScheduleListener() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            userScheduleRef = FirebaseDatabase.getInstance().getReference("Users/Conductor/" + userId);

            // Obtén los horarios iniciales una sola vez antes de establecer el ValueEventListener
            userScheduleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    prevHorarioInicio = dataSnapshot.child("horario_inicio").getValue(String.class);
                    prevHorarioFin = dataSnapshot.child("horario_fin").getValue(String.class);

                    // Ahora establece el ValueEventListener para escuchar cambios futuros
                    scheduleListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String newHorarioInicio = dataSnapshot.child("horario_inicio").getValue(String.class);
                            String newHorarioFin = dataSnapshot.child("horario_fin").getValue(String.class);

                            // Verifica si los horarios han cambiado y si no son nulos
                            if ((newHorarioInicio != null && !newHorarioInicio.equals(prevHorarioInicio)) ||
                                    (newHorarioFin != null && !newHorarioFin.equals(prevHorarioFin))) {
                                // Horarios han cambiado
                                showNotificationForScheduleChange(newHorarioInicio, newHorarioFin);
                                prevHorarioInicio = newHorarioInicio;
                                prevHorarioFin = newHorarioFin;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.w("ConductorMapActivity", "loadSchedule:onCancelled", databaseError.toException());
                        }
                    };
                    // Añade el listener que acabamos de crear
                    userScheduleRef.addValueEventListener(scheduleListener);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w("ConductorMapActivity", "loadInitialSchedule:onCancelled", databaseError.toException());
                }
            });
        }
    }


    private void showNotificationForScheduleChange(String newHorarioInicio, String newHorarioFin) {
        // Construir el mensaje que quieres mostrar
        String message = "Tu horario ha cambiado.\nNuevo horario de inicio: " + newHorarioInicio +
                "\nNuevo horario de fin: " + newHorarioFin;

        // Construye el AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(ConductorMapActivity.this);
        builder.setTitle("¡Atención!")
                .setMessage(message)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Si el usuario acepta, puedes realizar alguna acción, o simplemente cerrar el diálogo
                        dialog.dismiss();
                    }
                });
        // Crea y muestra el AlertDialog
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userScheduleRef != null && scheduleListener != null) {
            // Eliminar el listener cuando la actividad es destruida para evitar fugas de memoria
            userScheduleRef.removeEventListener(scheduleListener);
        }
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


    // En el método onMapReady
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Verifica si la aplicación tiene permisos para acceder a la ubicación del dispositivo.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            // Habilita la ubicación del usuario en el mapa
            BitmapDescriptor iconoPersonalizado = BitmapDescriptorFactory.fromResource(R.mipmap.ic_parada);
            mMap.setMyLocationEnabled(true);
            checkLocationSettings();
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.getUiSettings().setScrollGesturesEnabled(true);
            mMap.addMarker(new MarkerOptions()
                    .position(ubicacion1)
                    .title("Buses del pacifico Valpo")
                    .icon(iconoPersonalizado)); // Establecer el icono personalizado aquí

            mMap.addMarker(new MarkerOptions()
                    .position(ubicacion2)
                    .title("Buses viña del mar")
                    .icon(iconoPersonalizado));

            // Solicita una actualización de ubicación inmediata
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        // En este punto, ya tienes la última ubicación conocida. En algunos casos raros, puede ser nulo.
                        if (location != null) {
                            // Centra el mapa en la ubicación del conductor
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        }
                    });

            startAutoCenterTimer();
        }
    }


    private void checkLocationSettings() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(2000); // Intervalo de actualización
        mLocationRequest.setFastestInterval(100); // Intervalo más rápido


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);

        LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
                .addOnSuccessListener(locationSettingsResponse -> startLocationUpdates())
                .addOnFailureListener(e -> {
                    int statusCode = ((ApiException) e).getStatusCode();
                    if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(ConductorMapActivity.this, LOCATION_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                    }
                });
    }

    private float lerp(float start, float end, float fraction) {
        return (1 - fraction) * start + fraction * end;
    }
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String userId = user.getUid();

                        // Obtener el nuevo bearing
                        float newBearing = location.hasBearing() ? location.getBearing() : currentBearing;

                        // Interpola entre el bearing actual y el nuevo
                        float interpolatedBearing = lerp(currentBearing, newBearing, 0.1f);

                        // Crear un HashMap para actualizar la ubicación y el bearing
                        Map<String, Object> updateMap = new HashMap<>();
                        updateMap.put("l/0", location.getLatitude());
                        updateMap.put("l/1", location.getLongitude());
                        updateMap.put("bearing", interpolatedBearing);

                        // Actualizar la ubicación y el bearing en Firebase
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");
                        ref.child(userId).updateChildren(updateMap).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                System.out.println("Location and bearing updated successfully!");
                            } else {
                                System.err.println("Failed to update location and bearing: " + task.getException());
                            }
                        });

                        // Actualiza el bearing actual
                        currentBearing = interpolatedBearing;
                    } else {
                        stopLocationUpdates();
                    }
                }
            }
        };

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }


    // Función de interpolación lineal


    private void stopLocationUpdates() {
        if (mFusedLocationClient != null && mLocationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationSettings();
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        if (mFusedLocationClient != null && mLocationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    private void startAutoCenterTimer() {
        autoCenterHandler.postDelayed(autoCenterRunnable, AUTO_CENTER_INTERVAL);
    }

    private void centerMyLocation() {
        if (mMap != null && mMap.isMyLocationEnabled()) {
            Location lastLocation = mMap.getMyLocation();
            if (lastLocation != null) {
                LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            } else {
                // Si la última ubicación conocida es nula, puedes solicitar una actualización de ubicación inmediata
                // o implementar un callback para manejar esto cuando la ubicación esté disponible.
                // Esto dependerá de cómo esté configurado tu FusedLocationProviderClient.
            }
        }
    }


    private void removeLocationAndLogout() {
        stopLocationUpdates();
        if (mFusedLocationClient != null && mLocationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("ConductorMapActivity", "Ubicación en tiempo real eliminada con éxito.");

                    // Una vez que la ubicación se ha eliminado con éxito, borra la ubicación del usuario en driversAvailable
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference driversAvailableRef = FirebaseDatabase.getInstance().getReference("driversAvailable").child(userId);
                    driversAvailableRef.removeValue().addOnCompleteListener(locationRemovalTask -> {
                        if (locationRemovalTask.isSuccessful()) {
                            Log.d("ConductorMapActivity", "Ubicación eliminada de driversAvailable con éxito.");
                        } else {
                            Log.e("ConductorMapActivity", "Error al eliminar la ubicación de driversAvailable: " + locationRemovalTask.getException());
                        }

                        // Finalmente, cierra sesión
                        FirebaseAuth.getInstance().signOut(); // Cierra sesión en Firebase
                        Intent intent = new Intent(ConductorMapActivity.this, ConductorLoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    Log.e("ConductorMapActivity", "Error al eliminar la ubicación en tiempo real: " + task.getException());
                }
            });
        }
    }

    private void logout() {
        // Construye el AlertDialog
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión") // Título del diálogo
                .setMessage("¿Estás seguro de que deseas desconectar?\nTu ubicación dejará de compartirse")
                .setPositiveButton("Sí, Salir", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // El conductor ha confirmado que quiere desconectar
                        removeLocationAndLogout(); // Llama al método que maneja la desconexión
                    }
                })
                .setNegativeButton("Volver", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
                        bottomNavigationView.setSelectedItemId(R.id.menu_home);
                        dialog.dismiss(); // Cierra el diálogo y vuelve a la app

                    }
                })
                .setIcon(R.mipmap.ic_alerta) // Puedes cambiar el ícono por uno que prefieras
                .show(); // Muestra el diálogo

        // Cambia el color del texto del botón positivo después de que se haya mostrado el diálogo
        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(Color.RED); // Cambia el color a negro
        }

        // Cambia el color del texto del botón negativo
        Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (negativeButton != null) {
            negativeButton.setTextColor(Color.BLACK); // Cambia el color a negro
        }
    }

}
