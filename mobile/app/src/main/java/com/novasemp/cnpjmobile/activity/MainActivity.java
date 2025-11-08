package com.novasemp.cnpjmobile.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.novasemp.cnpjmobile.R;
import com.novasemp.cnpjmobile.dialog.AppLocationManager;
import com.novasemp.cnpjmobile.util.SessionManager;

public class MainActivity extends AppCompatActivity {

    private EditText editTextCnae;
    private EditText editTextMunicipio;
    private EditText editTextCapital;
    private Button buttonBuscar;
    private Button buttonHistorico;
    private Button buttonUsarLocalizacao;
    private SessionManager sessionManager;

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        String sessionId = sessionManager.getSessionId();
        System.out.println("DEBUG: MainActivity - SessionId gerado: " + sessionId);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        editTextCnae = findViewById(R.id.editTextCnae);
        editTextMunicipio = findViewById(R.id.editTextMunicipio);
        editTextCapital = findViewById(R.id.editTextCapital);
        buttonBuscar = findViewById(R.id.buttonBuscar);
        buttonHistorico = findViewById(R.id.btnHistorico);
        buttonUsarLocalizacao = findViewById(R.id.buttonUsarLocalizacao);
    }

    private void setupClickListeners() {
        buttonHistorico.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoricoActivity.class);
            startActivity(intent);
        });

        Button buttonAnaliseAvancada = findViewById(R.id.buttonAnaliseAvancada);
        buttonAnaliseAvancada.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DataAnalysisActivity.class);
            startActivity(intent);
        });

        // BOTÃO NOVO: Usar Localização
        buttonUsarLocalizacao.setOnClickListener(v -> {
            usarLocalizacao();
        });

        buttonBuscar.setOnClickListener(v -> {
            realizarBusca();
        });
    }

    private void usarLocalizacao() {
        // Verificar se já tem permissão
        if (hasLocationPermission()) {
            mostrarDialogoLocalizacao();
        } else {
            // Solicitar permissão
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST
            );
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }

    private void mostrarDialogoLocalizacao() {
        AppLocationManager appLocationManager = new AppLocationManager(this, new AppLocationManager.LocationDialogListener() {
            @Override
            public void onLocationConfirmed(String municipio) {
                editTextMunicipio.setText(municipio);
                Toast.makeText(MainActivity.this, "Município definido: " + municipio, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLocationCancelled() {
                Toast.makeText(MainActivity.this, "Busca por localização cancelada", Toast.LENGTH_SHORT).show();
            }
        });
        appLocationManager.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            boolean permissionGranted = false;

            // Verificar se alguma permissão foi concedida
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = true;
                    break;
                }
            }

            if (permissionGranted) {
                // Permissão concedida
                mostrarDialogoLocalizacao();
            } else {
                // Permissão negada
                Toast.makeText(this,
                        "Permissão de localização negada. Digite o município manualmente.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void realizarBusca() {
        String cnae = editTextCnae.getText().toString().trim();
        String municipio = editTextMunicipio.getText().toString().trim();
        String capitalStr = editTextCapital.getText().toString().trim();

        // Validações básicas
        if (cnae.isEmpty() || municipio.isEmpty()) {
            Toast.makeText(this, "Preencha pelo menos CNAE e Município", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cnae.matches("\\d+") || !municipio.matches("\\d+")) {
            Toast.makeText(this, "CNAE e Município devem ser códigos numéricos", Toast.LENGTH_SHORT).show();
            return;
        }

        Double capital = null;
        if (!capitalStr.isEmpty()) {
            try {
                capital = Double.parseDouble(capitalStr);
                if (capital < 0) {
                    Toast.makeText(this, "Capital social não pode ser negativo", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Capital social deve ser um número válido", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String sessionId = sessionManager.getSessionId();

        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        intent.putExtra("CNAE", cnae);
        intent.putExtra("MUNICIPIO", municipio);
        intent.putExtra("SESSION_ID", sessionId);

        if (capital != null) {
            intent.putExtra("CAPITAL", capital);
        }

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}