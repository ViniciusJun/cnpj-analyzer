package com.novasemp.cnpjmobile.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
public class HistoricoLocalManager {
    private static final String PREF_NAME = "historico_local_prefs";
    private static final String KEY_TIPOS_ANALISE = "tipos_analise";
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public HistoricoLocalManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    public void salvarTipoAnalise(String chave, String tipoAnalise) {
        Map<String, String> map = getTodosTiposAnalise();
        map.put(chave, tipoAnalise);
        String json = gson.toJson(map);
        sharedPreferences.edit().putString(KEY_TIPOS_ANALISE, json).apply();
        System.out.println("DEBUG: HistoricoLocalManager - Tipo salvo: " + chave + " -> " + tipoAnalise);
    }

    public String getTipoAnalise(String chave) {
        Map<String, String> map = getTodosTiposAnalise();
        String tipo = map.get(chave);
        System.out.println("DEBUG: HistoricoLocalManager - Tipo recuperado: " + chave + " -> " + tipo);
        return tipo;
    }

    private Map<String, String> getTodosTiposAnalise() {
        String json = sharedPreferences.getString(KEY_TIPOS_ANALISE, "{}");
        Type type = new TypeToken<HashMap<String, String>>(){}.getType();
        Map<String, String> map = gson.fromJson(json, type);
        return map != null ? map : new HashMap<>();
    }

    public void limparTodosTiposAnalise() {
        sharedPreferences.edit().remove(KEY_TIPOS_ANALISE).apply();
        System.out.println("DEBUG: HistoricoLocalManager - Todos os tipos de análise foram limpos");
    }

    // Gerar chave única baseada nos dados da busca
    public static String gerarChave(String sessionId, String cnae, String municipio, String dataBusca) {
        // Simplificar a chave - remover espaços e caracteres especiais
        String dataSimplificada = dataBusca != null ?
                dataBusca.replace(" ", "").replace(":", "").replace("-", "").replace("T", "").replace("Z", "") :
                "semdata";

        String chave = sessionId + "_" + cnae + "_" + municipio + "_" + dataSimplificada;

        System.out.println("DEBUG: HistoricoLocalManager - Gerando chave:");
        System.out.println("DEBUG:   SessionId: " + sessionId);
        System.out.println("DEBUG:   CNAE: " + cnae);
        System.out.println("DEBUG:   Municipio: " + municipio);
        System.out.println("DEBUG:   DataBusca original: " + dataBusca);
        System.out.println("DEBUG:   DataBusca simplificada: " + dataSimplificada);
        System.out.println("DEBUG:   Chave final: " + chave);

        return chave;
    }

    public void debugTodosTiposAnalise() {
        Map<String, String> map = getTodosTiposAnalise();
        System.out.println("DEBUG: HistoricoLocalManager - Todos os tipos armazenados:");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.println("DEBUG:   " + entry.getKey() + " -> " + entry.getValue());
        }
        System.out.println("DEBUG: Total de tipos armazenados: " + map.size());
    }
}
