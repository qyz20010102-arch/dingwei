package com.example.locationlab;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public final class MainActivity extends Activity implements LocationListener {
    private static final int REQUEST_LOCATION = 1001;

    private LocationManager locationManager;
    private TextView permissionView;
    private TextView providerView;
    private TextView locationView;
    private TextView accuracyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        permissionView = findViewById(R.id.permission_status);
        providerView = findViewById(R.id.provider_status);
        locationView = findViewById(R.id.location_value);
        accuracyView = findViewById(R.id.accuracy_value);

        findViewById(R.id.refresh_button).setOnClickListener(v -> refreshLocation());
        findViewById(R.id.settings_button).setOnClickListener(v -> openLocationSettings());
        findViewById(R.id.copy_button).setOnClickListener(v -> copyLocation());

        refreshLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLocation();
    }

    private boolean hasLocationPermission() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void refreshLocation() {
        if (!hasLocationPermission()) {
            permissionView.setText(R.string.permission_missing);
            providerView.setText(R.string.provider_unknown);
            locationView.setText(R.string.location_unknown);
            accuracyView.setText(R.string.accuracy_unknown);
            requestPermissions(
                    new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQUEST_LOCATION
            );
            return;
        }

        permissionView.setText(R.string.permission_ok);

        boolean gps = isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        providerView.setText(String.format(
                Locale.CHINA,
                getString(R.string.provider_format),
                gps ? "开启" : "关闭",
                network ? "开启" : "关闭"
        ));

        Location best = getLastKnownLocation();
        if (best != null) {
            showLocation(best);
        } else {
            locationView.setText(R.string.location_waiting);
            accuracyView.setText(R.string.accuracy_unknown);
        }

        requestUpdates();
    }

    private boolean isProviderEnabled(String provider) {
        try {
            return locationManager.isProviderEnabled(provider);
        } catch (Exception ignored) {
            return false;
        }
    }

    private Location getLastKnownLocation() {
        Location best = null;
        String[] providers = {
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
        };

        for (String provider : providers) {
            try {
                Location candidate = locationManager.getLastKnownLocation(provider);
                if (candidate == null) {
                    continue;
                }
                if (best == null || candidate.getTime() > best.getTime()) {
                    best = candidate;
                }
            } catch (SecurityException ignored) {
                return best;
            } catch (Exception ignored) {
                // Provider may not exist on this device.
            }
        }
        return best;
    }

    private void requestUpdates() {
        try {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setPowerRequirement(Criteria.POWER_MEDIUM);

            locationManager.requestLocationUpdates(
                    1000L,
                    1.0f,
                    criteria,
                    this,
                    getMainLooper()
            );
        } catch (SecurityException e) {
            permissionView.setText(R.string.permission_missing);
        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "无法启动定位监听：" + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void showLocation(Location location) {
        locationView.setText(String.format(
                Locale.US,
                "%.6f, %.6f",
                location.getLatitude(),
                location.getLongitude()
        ));

        if (location.hasAccuracy()) {
            accuracyView.setText(String.format(
                    Locale.US,
                    "%.1f m",
                    location.getAccuracy()
            ));
        } else {
            accuracyView.setText(R.string.accuracy_unknown);
        }
    }

    private void copyLocation() {
        String value = locationView.getText().toString();
        if (value.equals(getString(R.string.location_unknown))
                || value.equals(getString(R.string.location_waiting))) {
            Toast.makeText(this, "当前还没有可复制的位置", Toast.LENGTH_SHORT).show();
            return;
        }

        android.content.ClipboardManager clipboard =
                (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(
                android.content.ClipData.newPlainText("location", value)
        );
        Toast.makeText(this, "坐标已复制", Toast.LENGTH_SHORT).show();
    }

    private void openLocationSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        } catch (Exception e) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        showLocation(location);
    }

    @Override
    protected void onDestroy() {
        try {
            locationManager.removeUpdates(this);
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }

    @Override
    public void onProviderEnabled(String provider) {
        refreshLocation();
    }

    @Override
    public void onProviderDisabled(String provider) {
        refreshLocation();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Deprecated callback retained for API compatibility.
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION) {
            if (hasLocationPermission()) {
                refreshLocation();
            } else {
                permissionView.setText(R.string.permission_missing);
                Toast.makeText(
                        this,
                        "未授予定位权限，无法读取当前位置",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }
}
