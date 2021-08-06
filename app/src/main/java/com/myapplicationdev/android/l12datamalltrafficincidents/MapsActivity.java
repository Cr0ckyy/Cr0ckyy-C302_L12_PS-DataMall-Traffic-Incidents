package com.myapplicationdev.android.l12datamalltrafficincidents;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.myapplicationdev.android.l12datamalltrafficincidents.databinding.ActivityMapsBinding;

import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap map;
    ActivityMapsBinding mapsBinding;
    FirebaseFirestore fireStore;
    SupportMapFragment mapFragment;
    LatLng Singapore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mapsBinding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(mapsBinding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        fireStore = FirebaseFirestore.getInstance();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        // Add a marker in Sydney and move the camera
        Singapore = new LatLng(1.3, 103.7);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(Singapore, 10));


        if (getIntent().getSerializableExtra("incidentSelected") != null) {
            Incident incident = (Incident) getIntent().getSerializableExtra("incidentSelected");
            map.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(incident.getLatitude(), incident.getLongitude()))
                            .title(incident.getType())
                            .snippet(incident.getMessage())
            );
        } else {
            fireStore.collection("incidents")
                    .get()
                    .addOnCompleteListener((Task<QuerySnapshot> task) -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                                Incident incident = documentSnapshot.toObject(Incident.class);
                                map.addMarker(new MarkerOptions()
                                        .position(new LatLng(incident.getLatitude(), incident.getLongitude()))
                                        .title(incident.getType())
                                        .snippet(incident.getMessage()));
                            }
                        } else {
                            Log.d("MapsActivity", "Error getting documents: ", task.getException());
                        }
                    });
        }
    }
}