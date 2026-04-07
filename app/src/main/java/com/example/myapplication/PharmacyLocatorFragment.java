package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PharmacyLocatorFragment extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private MapView mapView;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_pharmacy_locator);

        // Apply edge-to-edge padding
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Initialize the MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // 1. Set Custom Info Window (The "Box")
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Nullable
            @Override
            public View getInfoContents(@NonNull Marker marker) {
                // Inflate your custom layout
                View view = getLayoutInflater().inflate(R.layout.item_pharmacy_info, null);

                // Find views within the custom box
                TextView name = view.findViewById(R.id.Title);
                TextView address = view.findViewById(R.id.Address);
                TextView phone = view.findViewById(R.id.PhoneNumber);

                // Set content based on the marker clicked
                name.setText(marker.getTitle());
                address.setText("123 Pharmacy St, New York, NY"); // Placeholder
                phone.setText("(555) 012-3456"); // Placeholder

                return view;
            }

            @Nullable
            @Override
            public View getInfoWindow(@NonNull Marker marker) {
                return null;
            }
        });

        // 2. Handle clicks on the info window
        googleMap.setOnInfoWindowClickListener(this);

        // 3. Add default markers
        LatLng nyc = new LatLng(40.7128, -74.0060);
        googleMap.addMarker(new MarkerOptions()
                .position(nyc)
                .title("CVS Pharmacy"));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nyc, 12));
    }

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        Toast.makeText(this, "Opening directions to " + marker.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}