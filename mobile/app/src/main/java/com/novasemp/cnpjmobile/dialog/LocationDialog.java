package com.novasemp.cnpjmobile.dialog;

import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import com.novasemp.cnpjmobile.R;
import com.novasemp.cnpjmobile.util.AppLocationManager;

public class LocationDialog extends Dialog {

    private Context context;
    private LocationDialogListener listener;
    private TextView textStatus;
    private Button btnConfirmar;
    private Button btnCancelar;
    private AppLocationManager appLocationManager;
    private String municipioSugerido;
    private String municipioNome;

    public interface LocationDialogListener {
        void onLocationConfirmed(String municipio);
        void onLocationCancelled();
    }

    public LocationDialog(Context context, LocationDialogListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        this.appLocationManager = new AppLocationManager(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_location);

        initViews();
        setupClickListeners();
        startLocationDetection();
    }

    private void initViews() {
        textStatus = findViewById(R.id.textStatus);
        btnConfirmar = findViewById(R.id.btnConfirmar);
        btnCancelar = findViewById(R.id.btnCancelar);

        btnConfirmar.setEnabled(false);
    }

    private void setupClickListeners() {
        btnConfirmar.setOnClickListener(v -> {
            if (municipioSugerido != null) {
                listener.onLocationConfirmed(municipioSugerido);
                dismiss();
            }
        });

        btnCancelar.setOnClickListener(v -> {
            listener.onLocationCancelled();
            dismiss();
        });
    }

    private void startLocationDetection() {
        textStatus.setText("🛰️ Detectando sua localização...");

        appLocationManager.getCurrentLocation(new AppLocationManager.LocationCallback() {
            @Override
            public void onLocationSuccess(Location location) {
                // Usar simulação baseada em coordenadas
                String[] resultado = getMunicipioFromLocationSimulation(location);
                municipioSugerido = resultado[0];
                municipioNome = resultado[1];

                runOnUiThread(() -> {
                    textStatus.setText("📍 Localização detectada!\nMunicípio: " + municipioNome + " (" + municipioSugerido + ")");
                    btnConfirmar.setEnabled(true);
                    btnConfirmar.setText("Usar " + municipioNome);
                });
            }

            @Override
            public void onLocationError(String error) {
                runOnUiThread(() -> {
                    textStatus.setText("❌ " + error + "\n\nPor favor, digite o município manualmente.");
                    btnConfirmar.setEnabled(false);
                });
            }
        });
    }

    private String[] getMunicipioFromLocationSimulation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        System.out.println("DEBUG: Coordenadas detectadas - Lat: " + latitude + ", Long: " + longitude);

        // Simulação baseada em coordenadas aproximadas de grandes cidades
        if (latitude >= -23.8 && latitude <= -23.3 && longitude >= -46.8 && longitude <= -46.3) {
            return new String[]{"3550308", "São Paulo"};
        } else if (latitude >= -22.9 && latitude <= -22.8 && longitude >= -43.4 && longitude <= -43.1) {
            return new String[]{"3304557", "Rio de Janeiro"};
        } else if (latitude >= -19.9 && latitude <= -19.8 && longitude >= -44.0 && longitude <= -43.9) {
            return new String[]{"3106200", "Belo Horizonte"};
        } else if (latitude >= -30.1 && latitude <= -29.9 && longitude >= -51.3 && longitude <= -51.1) {
            return new String[]{"4314902", "Porto Alegre"};
        } else if (latitude >= -25.5 && latitude <= -25.3 && longitude >= -49.4 && longitude <= -49.1) {
            return new String[]{"4106902", "Curitiba"};
        } else if (latitude >= -15.8 && latitude <= -15.7 && longitude >= -47.9 && longitude <= -47.8) {
            return new String[]{"5300108", "Brasília"};
        } else if (latitude >= -12.9 && latitude <= -12.8 && longitude >= -38.5 && longitude <= -38.4) {
            return new String[]{"2927408", "Salvador"};
        } else if (latitude >= -3.7 && latitude <= -3.6 && longitude >= -38.5 && longitude <= -38.4) {
            return new String[]{"2304400", "Fortaleza"};
        } else if (latitude >= -8.1 && latitude <= -8.0 && longitude >= -34.9 && longitude <= -34.8) {
            return new String[]{"2611606", "Recife"};
        } else if (latitude >= -3.1 && latitude <= -3.0 && longitude >= -60.0 && longitude <= -59.9) {
            return new String[]{"1302603", "Manaus"};
        } else {
            // Fallback para um município padrão
            return new String[]{"3550308", "São Paulo"};
        }
    }

    private void runOnUiThread(Runnable action) {
        if (getContext() != null) {
            android.os.Handler handler = new android.os.Handler(getContext().getMainLooper());
            handler.post(action);
        }
    }

    @Override
    public void dismiss() {
        appLocationManager.cleanup();
        super.dismiss();
    }
}