package com.optic.BusFlow;

import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.optic.BusFlow.databinding.ActivityConductorMapBinding;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PasajeroMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference driversLocationRef;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private Marker lastOpened = null;
    // Variables para almacenar las ubicaciones del pasajero y del conductor
    private LatLng conductorLatLng;
    private LocationCallback mLocationCallback;
    private LatLng passengerLocation;
    private ActivityConductorMapBinding binding;
    private TextView textViewDuration;
    private TextView textViewDistance;
    private static final int LOCATION_REQUEST_CODE = 1000;
    private GeoFire geoFire;
    private boolean isMyLocationCentered = false;
    private Marker currentFocusedMarker;
    private boolean shouldKeepCentering = false;
    private Handler autoCenterHandler = new Handler();
    private Runnable autoCenterRunnable = new Runnable() {
        @Override
        public void run() {
            centerMyLocation();
        }
    };
    private static final String NOTIFICATION_CHANNEL_ID = "conductor_cerca_channel";

    private static final long AUTO_CENTER_INTERVAL = 30000; // 10 segundos
    private View infoView;
    private TextView empresaTextView;
    private TextView rutaTextView;
    private TextView horaInicioTextView;
    private TextView horaFinTextView;
    private HashMap<String, Float> lastNotifiedDistance = new HashMap<>();
    private DatabaseReference clientAvailableRef;
    private String currentRuta;
    private String currentHoraInicio;
    private String currentHoraFin;
    private String currentEmpresa;
    private LatLng driverLocation;
    private HashSet<String> notifiedDrivers = new HashSet<>();
    private HashMap<String, Marker> driverMarkers = new HashMap<>();
    private HashSet<String> activeDrivers = new HashSet<>();
    private Map<String, String> conductorRutas = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pasajero_map);

        // Inicialización de elementos de UI
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        driversLocationRef = FirebaseDatabase.getInstance().getReference("driversAvailable"); // Reemplace con la referencia correcta de su base de datos
        geoFire = new GeoFire(driversLocationRef);
        ImageButton btnFilter = findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> showFilterDialog());

        // Resto de la inicialización

        // Inicializa el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        clientAvailableRef = FirebaseDatabase.getInstance().getReference("clientAvailable");
        geoFire = new GeoFire(clientAvailableRef);


        driversLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashSet<String> currentDrivers = new HashSet<>();
                for (DataSnapshot driverSnapshot : dataSnapshot.getChildren()) {
                    String conductorId = driverSnapshot.getKey();
                    if (conductorId == null) continue;
                    // Carga la ruta del conductor de Firebase
                    DatabaseReference conductorRef = FirebaseDatabase.getInstance().getReference("Users").child("Conductor").child(conductorId);
                    conductorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot conductorSnapshot) {
                            String ruta = conductorSnapshot.child("ruta").getValue(String.class);
                            conductorRutas.put(conductorId, ruta);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Manejar errores
                        }
                    });

                    currentDrivers.add(conductorId);
                    Double latitude = driverSnapshot.child("l/0").getValue(Double.class);
                    Double longitude = driverSnapshot.child("l/1").getValue(Double.class);
                    Float bearing = driverSnapshot.hasChild("bearing") ? driverSnapshot.child("bearing").getValue(Float.class) : 0f;

                    // Verifica si la latitud y longitud son válidas
                    if (latitude == null || longitude == null) continue;

                    LatLng driverLatLng = new LatLng(latitude, longitude);
                    conductorLatLng = new LatLng(latitude, longitude);
                    boolean invertIcon = bearing > 180;

                    Marker marker = driverMarkers.get(conductorId);
                    if (marker != null) {
                        // Actualiza la posición y orientación del marcador existente
                        marker.setPosition(driverLatLng);
                        if (invertIcon) {
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(getInvertedIcon()));
                            marker.setRotation(bearing);
                        } else {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bus2));
                            marker.setRotation(bearing);
                        }
                    } else {
                        // Crea un nuevo marcador y lo añade al mapa
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(driverLatLng)
                                .anchor(0.5f, 0.5f);
                        if (invertIcon) {
                            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getInvertedIcon()));
                            markerOptions.rotation(bearing);
                        } else {
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bus2));
                            markerOptions.rotation(bearing);
                        }
                        Marker newMarker = mMap.addMarker(markerOptions);
                        newMarker.setTag(conductorId);
                        driverMarkers.put(conductorId, newMarker);
                    }
                }

                // Elimina marcadores de conductores que ya no están en la base de datos
                for (String driverId : driverMarkers.keySet()) {
                    if (!currentDrivers.contains(driverId)) {
                        Marker markerToRemove = driverMarkers.get(driverId);
                        if (markerToRemove != null) {
                            markerToRemove.remove();
                        }
                        driverMarkers.remove(driverId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejar errores
            }
        });


        // Configurar el BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setSelectedItemId(R.id.menu_home);
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

    }

    // Método para mostrar el diálogo de filtro
    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione el bus que desea visualizar");
        View filterView = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        builder.setView(filterView);

        // Obtener referencias a los CheckBoxes
        CheckBox checkBoxRuta1 = filterView.findViewById(R.id.checkBoxRuta1);
        CheckBox checkBoxRuta2 = filterView.findViewById(R.id.checkBoxRuta2);
        CheckBox checkBoxRuta3 = filterView.findViewById(R.id.checkBoxRuta3);
        CheckBox checkBoxRuta4 = filterView.findViewById(R.id.checkBoxRuta4);
        CheckBox checkBoxRuta5 = filterView.findViewById(R.id.checkBoxRuta5);
        CheckBox checkBoxRuta6 = filterView.findViewById(R.id.checkBoxRuta6);
        CheckBox checkBoxRuta7 = filterView.findViewById(R.id.checkBoxRuta7);
        CheckBox checkBoxRuta8 = filterView.findViewById(R.id.checkBoxRuta8);
        // ... Agrega CheckBoxes adicionales según sea necesario

        // Obtener SharedPreferences para restaurar estados
        SharedPreferences sharedPreferences = getSharedPreferences("FiltroConfig", MODE_PRIVATE);
        checkBoxRuta1.setChecked(sharedPreferences.getBoolean("EstadoRuta1", false));
        checkBoxRuta2.setChecked(sharedPreferences.getBoolean("EstadoRuta2", false));
        checkBoxRuta3.setChecked(sharedPreferences.getBoolean("EstadoRuta3", false));
        checkBoxRuta4.setChecked(sharedPreferences.getBoolean("EstadoRuta4", false));
        checkBoxRuta5.setChecked(sharedPreferences.getBoolean("EstadoRuta5", false));
        checkBoxRuta6.setChecked(sharedPreferences.getBoolean("EstadoRuta6", false));
        checkBoxRuta7.setChecked(sharedPreferences.getBoolean("EstadoRuta7", false));
        checkBoxRuta8.setChecked(sharedPreferences.getBoolean("EstadoRuta8", false));
        // ... Repite para otros CheckBoxes

        builder.setPositiveButton("Aplicar", (dialog, which) -> {
            // Conjunto para almacenar las rutas seleccionadas
            Set<String> selectedRoutes = new HashSet<>();

            // Verificar qué CheckBoxes están marcados y agregar las rutas correspondientes
            if (checkBoxRuta1.isChecked()) selectedRoutes.add("301");
            if (checkBoxRuta2.isChecked()) selectedRoutes.add("302");
            if (checkBoxRuta3.isChecked()) selectedRoutes.add("Quillota");
            if (checkBoxRuta4.isChecked()) selectedRoutes.add("Valparaiso");
            if (checkBoxRuta5.isChecked()) selectedRoutes.add("Concon");
            if (checkBoxRuta6.isChecked()) selectedRoutes.add("Quintero");
            if (checkBoxRuta7.isChecked()) selectedRoutes.add("Santiago");
            if (checkBoxRuta8.isChecked()) selectedRoutes.add("La Calera");
            // ... Repite para los demás CheckBoxes

            // Aplica el filtro con las rutas seleccionadas
            applyFilter(selectedRoutes);

            // Guardar el estado de los CheckBoxes
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("EstadoRuta1", checkBoxRuta1.isChecked());
            editor.putBoolean("EstadoRuta2", checkBoxRuta2.isChecked());
            editor.putBoolean("EstadoRuta3", checkBoxRuta3.isChecked());
            editor.putBoolean("EstadoRuta4", checkBoxRuta4.isChecked());
            editor.putBoolean("EstadoRuta5", checkBoxRuta5.isChecked());
            editor.putBoolean("EstadoRuta6", checkBoxRuta6.isChecked());
            editor.putBoolean("EstadoRuta7", checkBoxRuta7.isChecked());
            editor.putBoolean("EstadoRuta8", checkBoxRuta8.isChecked());
            // ... Repite para otros CheckBoxes
            editor.apply();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dlg -> {
            // Cambiar el color del botón positivo (Aplicar)
            Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnPositive.setTextColor(getResources().getColor(R.color.colorAplicar)); // Usa el color deseado aquí

            // Cambiar el color del botón negativo (Cancelar)
            Button btnNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            btnNegative.setTextColor(getResources().getColor(R.color.colorCancelar)); // Usa el color deseado aquí
        });

        dialog.show();
    }


    private void applyFilter(Set<String> selectedRoutes) {
        for (Marker marker : driverMarkers.values()) {
            String conductorId = (String) marker.getTag();
            String rutaConductor = conductorRutas.get(conductorId);

            marker.setVisible(selectedRoutes.isEmpty() || (rutaConductor != null && selectedRoutes.contains(rutaConductor)));
        }
    }

    private Bitmap getInvertedIcon() {
        // Aquí debes cargar tu bitmap original y aplicar una reflexión horizontal
        Bitmap originalIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_bus2);
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f); // Reflexión horizontal
        return Bitmap.createBitmap(originalIcon, 0, 0, originalIcon.getWidth(), originalIcon.getHeight(), matrix, false);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Inicializa GeoFire


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            // Habilita la ubicación del usuario en el mapa
            mMap.setMyLocationEnabled(true);
            checkLocationSettings();
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.getUiSettings().setScrollGesturesEnabled(true);
            startAutoCenterTimer();
            // Configura un listener para centrar la ubicación del usuario
            mMap.setOnMyLocationChangeListener(location -> {
                if (!isMyLocationCentered) {
                    passengerLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation, 15)); // Ajusta el valor del zoom según tus necesidades
                    isMyLocationCentered = true;

                    // Guardar la ubicación en Firebase (opcional)
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String userId = user.getUid();
                        geoFire.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    }
                }
            });
        }


        mMap.setOnMarkerClickListener(marker -> {
            currentFocusedMarker = marker;
            shouldKeepCentering = true;
            startContinuousCentering();
            return false; // Para mostrar la ventana de información
        });

        mMap.setOnInfoWindowCloseListener(marker -> {
            shouldKeepCentering = false;
            currentFocusedMarker = null;
        });

        mMap.setOnMapClickListener(latLng -> shouldKeepCentering = false);

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter(){
            @Override
            public View getInfoWindow(Marker marker) {
                // Usamos null aquí si no estamos modificando toda la ventana de información
                return null;
            }
            // Listener para clics en el mapa

            @Override
            public View getInfoContents(Marker marker) {
                // Aquí inflamos la vista de la ventana de información personalizada
                if (infoView == null) {
                    infoView = getLayoutInflater().inflate(R.layout.custom_info_window, null);
                    empresaTextView = infoView.findViewById(R.id.empresaTextView);
                    rutaTextView = infoView.findViewById(R.id.rutaTextView);
                    horaInicioTextView = infoView.findViewById(R.id.horaInicioTextView);
                    horaFinTextView = infoView.findViewById(R.id.horaFinTextView);
                }

                String conductorId = (String) marker.getTag();
                if (conductorId != null) {
                    DatabaseReference conductorRef = FirebaseDatabase.getInstance().getReference("Users").child("Conductor").child(conductorId);

                    conductorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Obtener los datos del conductor
                                String ruta = dataSnapshot.child("ruta").getValue(String.class);
                                String empresa = dataSnapshot.child("company").getValue(String.class);
                                String horaInicio = dataSnapshot.child("horario_inicio").getValue(String.class);
                                String horaFin = dataSnapshot.child("horario_fin").getValue(String.class);

                                // Solo actualiza si los datos han cambiado
                                if (!ruta.equals(currentRuta) || !horaInicio.equals(currentHoraInicio) || !horaFin.equals(currentHoraFin) || !empresa.equals(currentEmpresa)) {
                                    currentRuta = ruta;
                                    currentHoraInicio = horaInicio;
                                    currentHoraFin = horaFin;
                                    currentEmpresa = empresa;

                                    runOnUiThread(() -> {
                                        empresaTextView.setText("Empresa: " + empresa);
                                        rutaTextView.setText("Ruta: " + ruta);
                                        horaInicioTextView.setText("Hora de inicio: " + horaInicio);
                                        horaFinTextView.setText("Hora de fin: " + horaFin);

                                        // Solo actualiza la ventana de información si está visible
                                        if (marker.isInfoWindowShown()) {
                                            marker.hideInfoWindow(); // Oculta y luego muestra la ventana de información para actualizar los datos
                                            marker.showInfoWindow();
                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Manejar errores
                        }
                    });
                }

                return infoView; // Devolvemos la vista con la información del conductor
            }
        });


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
                            resolvable.startResolutionForResult(PasajeroMapActivity.this, LOCATION_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                    }
                });
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
                    LatLng passengerLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    setupProximityAlert(passengerLatLng);
                    if (user != null) {
                        String userId = user.getUid();
                        passengerLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        // Guardar la ubicación en GeoFire
                        geoFire.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                if (error != null) {
                                    Log.e("GeoFireError", "Hubo un error al guardar la ubicación en GeoFire: " + error.getMessage());
                                } else {
                                    Log.d("GeoFireSuccess", "Ubicación del pasajero guardada con éxito en GeoFire para el usuario: " + key);
                                    Log.d("PasajeroLocationLog", "Latitud: " + location.getLatitude() + ", Longitud: " + location.getLongitude());
                                }
                            }
                        });

                        //LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        //mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                    } else {
                        stopLocationUpdates();
                    }
                }
            }
        };

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private void stopLocationUpdates() {
        if (mFusedLocationClient != null && mLocationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
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

    private void startContinuousCentering() {
        final Handler centeringHandler = new Handler();
        Runnable centeringRunnable = new Runnable() {
            @Override
            public void run() {
                if (shouldKeepCentering && currentFocusedMarker != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(currentFocusedMarker.getPosition()));
                    centeringHandler.postDelayed(this, 1000); // Centra cada segundo, ajusta esto según necesites
                }
            }
        };
        centeringHandler.post(centeringRunnable);
    }
    private void stopAutoCenterTimer() {
        autoCenterHandler.removeCallbacks(autoCenterRunnable);
    }

    private void setupProximityAlert(LatLng passengerLatLng) {
        if ((passengerLatLng != null)&&(conductorLatLng != null))  {
            // Define el radio en kilómetros que consideras como "cerca"
            final double PROXIMITY_RADIUS = 0.5; // 500 metros, por ejemplo

            // Crea una GeoQuery alrededor de la ubicación del pasajero
            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(conductorLatLng.latitude, conductorLatLng.longitude), PROXIMITY_RADIUS);


            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    if (conductorLatLng != null) {
                    conductorLatLng = new LatLng(location.latitude, location.longitude);
                    float[] results = new float[1];
                    Location.distanceBetween(passengerLocation.latitude, passengerLocation.longitude, conductorLatLng.latitude, conductorLatLng.longitude, results);
                    float distanceInMeters = results[0];
                    Float lastDistance = lastNotifiedDistance.get(key);
                    if (lastDistance == null || (distanceInMeters <= lastDistance - 100)) {

                        DatabaseReference conductorRef = FirebaseDatabase.getInstance().getReference("Users").child("Conductor").child(key);
                        conductorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                sendNotification("Conductor Cerca", "Un conductor está por pasar cerca de ti.");
                                Log.d("ProximityAlert", "Conductor con clave: " + key + " está cerca del pasajero.");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Manejo de errores...
                            }
                        });
                        lastNotifiedDistance.put(key, distanceInMeters);
                    }

                    Log.d("ProximityAlert", "Pasajero con clave: " + key + " está a " + distanceInMeters + " metros del conductor.");

                    if (distanceInMeters <= PROXIMITY_RADIUS * 1000) {
                        Log.d("ProximityAlert", "Pasajero con clave: " + key + " está cerca del Conductor.");
                    } else {
                        Log.d("ProximityAlert", "Pasajero con clave: " + key + " NO está cerca del Conductor.");
                    }

                    } else {
                        Log.d("ProximityAlert", "Ubicación del conductor con clave: " + key + " es nula.");
                    }
                }


                @Override
                public void onKeyExited(String key) {
                    // Un conductor ha salido del radio del pasajero
                    Log.d("ProximityAlert", "Pasajero con clave: " + key + " se ha alejado del Conductor.");
                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                    // Un conductor se ha movido dentro del radio del pasajero
                    Log.d("ProximityAlert", "Pasajero con clave:" + key + " se ha movido cerca del Conductor.");
                }

                @Override
                public void onGeoQueryReady() {
                    // La consulta inicial ha terminado, todos los conductores actuales en el radio han sido cargados
                    Log.d("ProximityAlert", "Consulta inicial de proximidad completada.");
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                    // Hubo un error con la consulta de proximidad
                    Log.e("ProximityAlert", "Error en la consulta de proximidad: " + error.getMessage());
                }
            });

        }
    }



    private void sendNotification(String title, String message) {
        Log.d("Notification", "Intentando enviar notificación: " + title + " - " + message);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notificaciones de Conductores", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Canal para notificaciones cuando un conductor está cerca");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher) // Asegúrate de tener un icono ic_launcher en tu carpeta mipmap
                .setContentTitle(title)
                .setContentText(message)
                .setContentInfo("Info");

        notificationManager.notify(new Random().nextInt(), notificationBuilder.build());
        Log.d("Notification", "Intentando enviar notificación: " + title + " - " + message);
    }


    private void removeLocationAndLogout() {
        // Borra la ubicación del pasajero de la base de datos
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (error != null) {
                        Log.e("PasajeroMapActivity", "No se pudo eliminar la ubicación: " + error);
                    } else {
                        Log.d("PasajeroMapActivity", "Ubicación eliminada con éxito.");
                    }

                    // Cierra sesión en Firebase y redirige al login
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(PasajeroMapActivity.this, PasajeroLoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }
    private void centerMyLocation() {
        if (mMap != null && mMap.isMyLocationEnabled()) {
            Location lastLocation = mMap.getMyLocation();
            if (lastLocation != null) {
                LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            }
        }
        // Reinicia el temporizador para el próximo centrado
        autoCenterHandler.postDelayed(autoCenterRunnable, AUTO_CENTER_INTERVAL);
    }



    private void logout() {
        // Construye el AlertDialog
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión") // Título del diálogo
                .setMessage("¿Estás seguro de que deseas desconectar?")
                .setPositiveButton("Sí, Salir", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // El usuario ha confirmado que quiere desconectar
                        removeLocationAndLogout();
                    }
                })
                .setNegativeButton("Volver", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
                        bottomNavigationView.setSelectedItemId(R.id.menu_home);
                        dialog.dismiss(); // Cierra el diálogo
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


