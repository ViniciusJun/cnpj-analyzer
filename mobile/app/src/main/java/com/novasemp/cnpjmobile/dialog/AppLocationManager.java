package com.novasemp.cnpjmobile.dialog;

import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import com.novasemp.cnpjmobile.R;

public class AppLocationManager extends Dialog {

    private Context context;
    private LocationDialogListener listener;
    private TextView textStatus;
    private Button btnConfirmar;
    private Button btnCancelar;
    private com.novasemp.cnpjmobile.util.AppLocationManager appLocationManager;
    private String municipioSugerido;

    public interface LocationDialogListener {
        void onLocationConfirmed(String municipio);
        void onLocationCancelled();
    }

    public AppLocationManager(Context context, LocationDialogListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        this.appLocationManager = new com.novasemp.cnpjmobile.util.AppLocationManager(context);
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

        btnConfirmar.setEnabled(false); // Inicialmente desabilitado
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

        appLocationManager.getCurrentLocation(new com.novasemp.cnpjmobile.util.AppLocationManager.LocationCallback() {
            @Override
            public void onLocationSuccess(Location location) {
                municipioSugerido = getMunicipioFromLocation(location);

                runOnUiThread(() -> {
                    textStatus.setText("📍 Localização detectada!\nMunicípio sugerido: " + municipioSugerido);
                    btnConfirmar.setEnabled(true);
                    btnConfirmar.setText("Usar " + municipioSugerido);
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

    private String getMunicipioFromLocation(Location location) {
        // SIMULAÇÃO: Converter coordenadas para código de município
        // Na implementação real, você usaria uma API de geocoding reversa

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        System.out.println("DEBUG: Coordenadas detectadas - Lat: " + latitude + ", Long: " + longitude);

        // Simulação baseada em coordenadas aproximadas de grandes cidades
        if (latitude >= -23.8 && latitude <= -23.3 && longitude >= -46.8 && longitude <= -46.3) {
            return "3550308"; // São Paulo - SP
        } else if (latitude >= -22.9 && latitude <= -22.8 && longitude >= -43.4 && longitude <= -43.1) {
            return "3304557"; // Rio de Janeiro - RJ
        } else if (latitude >= -19.9 && latitude <= -19.8 && longitude >= -44.0 && longitude <= -43.9) {
            return "3106200"; // Belo Horizonte - MG
        } else if (latitude >= -30.1 && latitude <= -29.9 && longitude >= -51.3 && longitude <= -51.1) {
            return "4314902"; // Porto Alegre - RS
        } else if (latitude >= -25.5 && latitude <= -25.3 && longitude >= -49.4 && longitude <= -49.1) {
            return "4106902"; // Curitiba - PR
        } else {
            // Fallback para um município padrão
            return "3550308"; // São Paulo como fallback
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