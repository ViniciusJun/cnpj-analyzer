package com.novasemp.cnpj.ml.service;

import com.novasemp.cnpj.ml.model.EmpresaFeatures;
import com.novasemp.cnpj.ml.model.PredictionResult;
import com.novasemp.cnpj.ml.repository.TrainingDataRepository;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MLModelService {
    private Classifier classifier;
    private Instances dataStructure;
    private boolean modeloTreinado = false;
    
    public MLModelService(Connection connection) {
        try {
            // Tentar carregar modelo salvo
            classifier = (Classifier) SerializationHelper.read("modelo_empresas.model");
            dataStructure = (Instances) SerializationHelper.read("modelo_estrutura.model");
            modeloTreinado = true;
            System.out.println("Modelo de ML carregado com sucesso!");
        } catch (Exception e) {
            System.out.println("Modelo não encontrado, treinando novo modelo...");
            treinarModelo(connection);
        }
    }
    
    private void treinarModelo(Connection connection) {
        try {
            TrainingDataRepository repo = new TrainingDataRepository(connection);
            List<EmpresaFeatures> dadosTreinamento = repo.obterDadosTreinamento();
            
            if (dadosTreinamento.isEmpty()) {
                System.out.println("Dados insuficientes para treinamento");
                return;
            }
            
            // Criar estrutura de dados do WEKA
            ArrayList<Attribute> attributes = new ArrayList<>();
            attributes.add(new Attribute("cnae_principal"));
            attributes.add(new Attribute("municipio"));
            attributes.add(new Attribute("capital_social"));
            attributes.add(new Attribute("quantidade_empresas_regiao"));
            attributes.add(new Attribute("capital_medio_regiao"));
            attributes.add(new Attribute("densidade_empresarial"));
            attributes.add(new Attribute("faixa_capital"));
            
            // Classe (suposta probabilidade de sucesso - para POC usaremos dados simulados)
            ArrayList<String> classValues = new ArrayList<>();
            classValues.add("BAIXA");
            classValues.add("MEDIA");
            classValues.add("ALTA");
            attributes.add(new Attribute("sucesso", classValues));
            
            dataStructure = new Instances("EmpresasTrainingData", attributes, 0);
            dataStructure.setClassIndex(dataStructure.numAttributes() - 1);
            
            // Adicionar instâncias (para POC, geraremos dados simulados)
            Random rand = new Random(42);
            for (EmpresaFeatures features : dadosTreinamento) {
                double[] values = features.toFeatureArray();
                double[] instanceValues = new double[dataStructure.numAttributes()];
                
                for (int i = 0; i < values.length; i++) {
                    instanceValues[i] = values[i];
                }
                
                // Simular classe baseada nas features (para POC)
                double probabilidadeBase = calcularProbabilidadeBase(features);
                String classe = probabilidadeBase > 0.7 ? "ALTA" : 
                               probabilidadeBase > 0.4 ? "MEDIA" : "BAIXA";
                instanceValues[values.length] = dataStructure.attribute("sucesso").indexOfValue(classe);
                
                dataStructure.add(new DenseInstance(1.0, instanceValues));
            }
            
            // Treinar classificador
            classifier = new RandomForest();
            classifier.buildClassifier(dataStructure);
            
            // Salvar modelo
            SerializationHelper.write("modelo_empresas.model", classifier);
            SerializationHelper.write("modelo_estrutura.model", dataStructure);
            
            modeloTreinado = true;
            System.out.println("Modelo de ML treinado e salvo com sucesso!");
            
        } catch (Exception e) {
            System.err.println("Erro no treinamento do modelo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private double calcularProbabilidadeBase(EmpresaFeatures features) {
        // Simulação de probabilidade baseada nas features
        double score = 0.0;
        
        // Mais empresas na região -> maior probabilidade
        score += Math.min(features.getQuantidadeEmpresasRegiao() / 100.0, 0.3);
        
        // Capital social próximo da média -> maior probabilidade
        double diffCapital = Math.abs(features.getCapitalSocial() - features.getCapitalMedioRegiao());
        double capitalScore = diffCapital / (features.getCapitalMedioRegiao() + 1);
        score += (1 - Math.min(capitalScore, 1.0)) * 0.3;
        
        // Maior densidade empresarial -> maior probabilidade
        score += features.getDensidadeEmpresarial() * 0.2;
        
        // Faixa de capital média tende a ser melhor
        if (features.getFaixaCapitalSocial() == 1) score += 0.2;
        
        return Math.min(score, 1.0);
    }
    
    public PredictionResult preverSucesso(String cnae, String municipio, double capitalSocial, Connection connection) {
        if (!modeloTreinado) {
            return criarPredicaoDefault(cnae, municipio, capitalSocial);
        }
        
        try {
            TrainingDataRepository repo = new TrainingDataRepository(connection);
            List<EmpresaFeatures> featuresList = repo.obterDadosParaPredicao(cnae, municipio, capitalSocial);
            
            if (featuresList.isEmpty()) {
                return criarPredicaoDefault(cnae, municipio, capitalSocial);
            }
            
            EmpresaFeatures features = featuresList.get(0);
            double[] featureArray = features.toFeatureArray();
            
            // Criar instância para predição
            double[] instanceValues = new double[dataStructure.numAttributes()];
            for (int i = 0; i < featureArray.length; i++) {
                instanceValues[i] = featureArray[i];
            }
            instanceValues[featureArray.length] = Double.NaN; // Classe desconhecida
            
            DenseInstance instance = new DenseInstance(1.0, instanceValues);
            instance.setDataset(dataStructure);
            
            // Fazer predição
            double[] distribution = classifier.distributionForInstance(instance);
            
            // Encontrar classe com maior probabilidade
            int maxIndex = 0;
            for (int i = 1; i < distribution.length; i++) {
                if (distribution[i] > distribution[maxIndex]) {
                    maxIndex = i;
                }
            }
            
            String classePredita = dataStructure.classAttribute().value(maxIndex);
            double probabilidade = distribution[maxIndex];
            
            return criarResultadoPredicao(probabilidade, classePredita, features);
            
        } catch (Exception e) {
            System.err.println("Erro na predição: " + e.getMessage());
            return criarPredicaoDefault(cnae, municipio, capitalSocial);
        }
    }
    
    private PredictionResult criarResultadoPredicao(double probabilidade, String classe, EmpresaFeatures features) {
        String[] fatoresCriticos;
        String recomendacao;
        
        if (classe.equals("ALTA")) {
            fatoresCriticos = new String[]{
                "Mercado consolidado na região",
                "Capital social adequado",
                "Baixa saturação do segmento"
            };
            recomendacao = "Condições favoráveis para abertura do negócio";
        } else if (classe.equals("MEDIA")) {
            fatoresCriticos = new String[]{
                "Concorrência moderada",
                "Capital social dentro da média",
                "Necessidade de diferencial competitivo"
            };
            recomendacao = "Analisar oportunidades de diferenciação no mercado";
        } else {
            fatoresCriticos = new String[]{
                "Alta concorrência na região",
                "Capital social abaixo do ideal",
                "Saturação do segmento"
            };
            recomendacao = "Considerar localização alternativa ou segmento diferente";
        }
        
        return new PredictionResult(probabilidade, classe, fatoresCriticos, recomendacao);
    }
    
    private PredictionResult criarPredicaoDefault(String cnae, String municipio, double capitalSocial) {
        // Fallback quando não há dados suficientes
        return new PredictionResult(
            0.5, 
            "MEDIA", 
            new String[]{"Dados insuficientes para análise precisa"},
            "Considere realizar uma pesquisa de mercado mais detalhada"
        );
    }
}