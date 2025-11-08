package com.novasemp.cnpjmobile.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.core.content.ContextCompat;

public class AppLocationManager {
    private Context context;
    private android.location.LocationManager androidLocationManager;
    private LocationListener locationListener;
    private LocationCallback callback;
    private Handler handler;
    private static final int LOCATION_TIMEOUT = 15000; // 15 segundos

    public interface LocationCallback {
        void onLocationSuccess(Location location);
        void onLocationError(String error);
    }

    public AppLocationManager(Context context) {
        this.context = context;
        this.androidLocationManager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void getCurrentLocation(LocationCallback callback) {
        this.callback = callback;

        // Verificar permissões
        if (!hasLocationPermission()) {
            callback.onLocationError("Permissão de localização negada");
            return;
        }

        // Verificar se GPS está habilitado
        if (!isGPSEnabled()) {
            callback.onLocationError("GPS desabilitado. Por favor, habilite o GPS.");
            return;
        }

        try {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        stopLocationUpdates();
                        callback.onLocationSuccess(location);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {
                    stopLocationUpdates();
                    callback.onLocationError("Provedor de localização desabilitado");
                }
            };

            // Solicitar atualizações de localização
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                androidLocationManager.requestLocationUpdates(
                        android.location.LocationManager.GPS_PROVIDER,
                        1000, // 1 segundo
                        1,    // 1 metro
                        locationListener
                );

                // Timeout para evitar espera infinita
                handler.postDelayed(() -> {
                    stopLocationUpdates();
                    // Tentar usar última localização conhecida
                    Location lastLocation = getLastKnownLocation();
                    if (lastLocation != null) {
                        callback.onLocationSuccess(lastLocation);
                    } else {
                        callback.onLocationError("Timeout ao obter localização");
                    }
                }, LOCATION_TIMEOUT);

            } else {
                callback.onLocationError("Permissão de localização não concedida");
            }

        } catch (SecurityException e) {
            callback.onLocationError("Erro de segurança: " + e.getMessage());
        } catch (Exception e) {
            callback.onLocationError("Erro ao obter localização: " + e.getMessage());
        }
    }

    private Location getLastKnownLocation() {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                Location gpsLocation = androidLocationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
                Location networkLocation = androidLocationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);

                // Retornar a localização mais recente
                if (gpsLocation != null && networkLocation != null) {
                    return gpsLocation.getTime() > networkLocation.getTime() ? gpsLocation : networkLocation;
                } else if (gpsLocation != null) {
                    return gpsLocation;
                } else if (networkLocation != null) {
                    return networkLocation;
                }
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Erro ao obter última localização conhecida: " + e.getMessage());
        }
        return null;
    }

    private void stopLocationUpdates() {
        if (locationListener != null) {
            androidLocationManager.removeUpdates(locationListener);
            locationListener = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isGPSEnabled() {
        return androidLocationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                androidLocationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);
    }

    public void cleanup() {
        stopLocationUpdates();
    }
}