package it.units.placesapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import it.units.placesapp.databinding.ActivityMapsBinding;
import utils.DatabaseAccess;
import utils.Monument;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private ActivityMapsBinding binding;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    float mZoom;
    GoogleMap gMap;
    LatLng latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        fetchLastLocation();
        binding.profile.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(MapsActivity.this, AccountActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.google_map);
                mapFragment.getMapAsync(MapsActivity.this);
                currentLocation = location;
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.setMinZoomPreference(7);
        gMap.setMaxZoomPreference(20);
        latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mZoom = 13.2f;

        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, mZoom));
        showAllNearPlace(gMap, currentLocation.getLatitude(), currentLocation.getLongitude(), mZoom);

        gMap.setOnMarkerClickListener(marker -> {
            Intent intent = new Intent(MapsActivity.this, MonumentActivity.class);
            intent.putExtra("monumentName", marker.getTitle());
            intent.putExtra("latitude", marker.getPosition().latitude);
            intent.putExtra("longitude", marker.getPosition().longitude);
            startActivity(intent);
            return false;
        });

        gMap.setOnCameraIdleListener(() -> {
            if (gMap != null) {
                gMap.clear();
            }
            latLng = gMap.getCameraPosition().target;
            mZoom = gMap.getCameraPosition().zoom;


            showAllNearPlace(gMap, latLng.latitude, latLng.longitude, mZoom);
        });
    }

    private void showAllNearPlace(GoogleMap gmap, double latitude, double longitude, float zoom) {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
        databaseAccess.open();
        List<Monument> monuments = databaseAccess.getName(longitude, latitude, zoom);
        for (Monument m : monuments) {
            LatLng latLng = new LatLng(m.getLatitude(), m.getLongitude());
            gmap.addMarker(new MarkerOptions().title(m.getName()).position(latLng).icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_baseline_place_24)));
        }
        databaseAccess.close();
    }

    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLastLocation();
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                Toast.makeText(MapsActivity.this, R.string.noLocationAllowed, Toast.LENGTH_LONG).show();
            }
        }
    }
}