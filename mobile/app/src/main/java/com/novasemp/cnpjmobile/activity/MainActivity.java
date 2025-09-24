package com.novasemp.cnpjmobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.novasemp.cnpjmobile.R;

public class MainActivity extends AppCompatActivity {

    private EditText editTextCnae;
    private EditText editTextMunicipio;
    private Button buttonBuscar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        editTextCnae = findViewById(R.id.editTextCnae);
        editTextMunicipio = findViewById(R.id.editTextMunicipio);
        buttonBuscar = findViewById(R.id.buttonBuscar);
    }

    private void setupClickListeners() {
        buttonBuscar.setOnClickListener(v -> {
            String cnae = editTextCnae.getText().toString().trim();
            String municipio = editTextMunicipio.getText().toString().trim();

            if (cnae.isEmpty() || municipio.isEmpty()) {
                Toast.makeText(this, "Preencha ambos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Navegar para a tela de dashboard
            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            intent.putExtra("CNAE", cnae);
            intent.putExtra("MUNICIPIO", municipio);
            startActivity(intent);
        });
    }
}