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
    private String status = "NÃO INICIALIZADO";
    private int totalAmostras = 0;
    
    public MLModelService(Connection connection) {
        try {
            System.out.println("🎯 Inicializando serviço de ML...");
            
            // Tentar carregar modelo salvo
            try {
                classifier = (Classifier) SerializationHelper.read("modelo_empresas.model");
                dataStructure = (Instances) SerializationHelper.read("modelo_estrutura.model");
                modeloTreinado = true;
                totalAmostras = dataStructure.numInstances();
                status = "MODELO CARREGADO - " + totalAmostras + " amostras";
                System.out.println("✅ Modelo de ML carregado com sucesso! Amostras: " + totalAmostras);
            } catch (Exception e) {
                System.out.println("⚠️ Modelo não encontrado, treinando novo modelo...");
                treinarModelo(connection);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erro crítico na inicialização do ML: " + e.getMessage());
            status = "ERRO: " + e.getMessage();
            e.printStackTrace();
        }
    }
    
    private void treinarModelo(Connection connection) {
        try {
            TrainingDataRepository repo = new TrainingDataRepository(connection);
            List<EmpresaFeatures> dadosTreinamento = repo.obterDadosTreinamento();
            
            System.out.println("📊 Dados de treinamento obtidos: " + dadosTreinamento.size() + " registros");
            
            if (dadosTreinamento.isEmpty()) {
                System.out.println("⚠️ Nenhum dado real encontrado, gerando dados simulados...");
                dadosTreinamento = gerarDadosTreinamentoSimulados();
                status = "MODELO SIMULADO - " + dadosTreinamento.size() + " amostras";
            } else {
                System.out.println("✅ Usando dados reais do banco para treinamento");
                status = "MODELO REAL - " + dadosTreinamento.size() + " amostras";
                
                // Logar alguns exemplos
                for (int i = 0; i < Math.min(3, dadosTreinamento.size()); i++) {
                    EmpresaFeatures f = dadosTreinamento.get(i);
                    System.out.println("   📍 Exemplo " + i + ": CNAE=" + f.getCnaePrincipal() + 
                                     ", Capital=" + f.getCapitalSocial() + 
                                     ", Empresas=" + f.getQuantidadeEmpresasRegiao());
                }
            }
            
            if (dadosTreinamento.isEmpty()) {
                status = "SEM DADOS PARA TREINAMENTO";
                System.out.println("❌ Dados insuficientes para treinamento");
                return;
            }
            
            System.out.println("📈 Treinando modelo com " + dadosTreinamento.size() + " amostras...");
            
            // Criar estrutura de dados do WEKA
            ArrayList<Attribute> attributes = new ArrayList<>();
            attributes.add(new Attribute("cnae_principal"));
            attributes.add(new Attribute("municipio"));
            attributes.add(new Attribute("capital_social"));
            attributes.add(new Attribute("quantidade_empresas_regiao"));
            attributes.add(new Attribute("capital_medio_regiao"));
            attributes.add(new Attribute("densidade_empresarial"));
            attributes.add(new Attribute("faixa_capital"));
            
            // Classe (suposta probabilidade de sucesso)
            ArrayList<String> classValues = new ArrayList<>();
            classValues.add("BAIXA");
            classValues.add("MEDIA");
            classValues.add("ALTA");
            attributes.add(new Attribute("sucesso", classValues));
            
            dataStructure = new Instances("EmpresasTrainingData", attributes, 0);
            dataStructure.setClassIndex(dataStructure.numAttributes() - 1);
            
            // Adicionar instâncias
            Random rand = new Random(42);
            int altaCount = 0, mediaCount = 0, baixaCount = 0;
            
            for (EmpresaFeatures features : dadosTreinamento) {
                double[] values = features.toFeatureArray();
                double[] instanceValues = new double[dataStructure.numAttributes()];
                
                for (int i = 0; i < values.length; i++) {
                    instanceValues[i] = values[i];
                }
                
                // Simular classe baseada nas features
                double probabilidadeBase = calcularProbabilidadeBase(features);
                String classe;
                if (probabilidadeBase > 0.7) {
                    classe = "ALTA";
                    altaCount++;
                } else if (probabilidadeBase > 0.4) {
                    classe = "MEDIA";
                    mediaCount++;
                } else {
                    classe = "BAIXA";
                    baixaCount++;
                }
                
                instanceValues[values.length] = dataStructure.attribute("sucesso").indexOfValue(classe);
                dataStructure.add(new DenseInstance(1.0, instanceValues));
            }
            
            totalAmostras = dataStructure.numInstances();
            System.out.println("📊 Distribuição das classes:");
            System.out.println("   ALTA: " + altaCount + " (" + String.format("%.1f", (altaCount * 100.0 / totalAmostras)) + "%)");
            System.out.println("   MÉDIA: " + mediaCount + " (" + String.format("%.1f", (mediaCount * 100.0 / totalAmostras)) + "%)");
            System.out.println("   BAIXA: " + baixaCount + " (" + String.format("%.1f", (baixaCount * 100.0 / totalAmostras)) + "%)");
            
            // Treinar classificador
            classifier = new RandomForest();
            classifier.buildClassifier(dataStructure);
            
            // Salvar modelo
            try {
                SerializationHelper.write("modelo_empresas.model", classifier);
                SerializationHelper.write("modelo_estrutura.model", dataStructure);
                System.out.println("💾 Modelo salvo com sucesso!");
            } catch (Exception e) {
                System.out.println("⚠️ Modelo treinado mas não salvo: " + e.getMessage());
            }
            
            modeloTreinado = true;
            status = "MODELO TREINADO - " + totalAmostras + " amostras";
            System.out.println("✅ Modelo de ML treinado com sucesso! " + totalAmostras + " instâncias.");
            
        } catch (Exception e) {
            System.err.println("❌ Erro no treinamento do modelo: " + e.getMessage());
            status = "ERRO NO TREINAMENTO: " + e.getMessage();
            e.printStackTrace();
        }
    }
    
    private List<EmpresaFeatures> gerarDadosTreinamentoSimulados() {
        List<EmpresaFeatures> dados = new ArrayList<>();
        Random rand = new Random(42);
        
        // Gerar dados simulados realistas para treinamento
        String[] cnaes = {"4721102", "4711301", "5611201", "6201501", "7820800", "4771701", "9602501", "4312808"};
        String[] municipios = {"3550308", "3509502", "3304557", "3106200", "5300108", "4106902", "4314902"};
        
        for (int i = 0; i < 2000; i++) {
            String cnae = cnaes[rand.nextInt(cnaes.length)];
            String municipio = municipios[rand.nextInt(municipios.length)];
            double capitalSocial = 1000 + rand.nextDouble() * 200000;
            int quantidadeEmpresas = 5 + rand.nextInt(150);
            double capitalMedio = 30000 + rand.nextDouble() * 70000;
            double densidade = 0.1 + rand.nextDouble() * 0.8;
            int faixaCapital = capitalSocial < 10000 ? 0 : (capitalSocial < 50000 ? 1 : 2);
            
            EmpresaFeatures features = new EmpresaFeatures(
                cnae, municipio, capitalSocial, 
                quantidadeEmpresas, capitalMedio, densidade, faixaCapital
            );
            dados.add(features);
        }
        
        System.out.println("🎲 Gerados " + dados.size() + " dados simulados para treinamento");
        return dados;
    }
    
    private double calcularProbabilidadeBase(EmpresaFeatures features) {
        // Simulação de probabilidade baseada nas features (mais realista)
        double score = 0.0;
        
        // Mais empresas na região -> maior probabilidade (até 30%)
        double densidadeEmpresas = Math.min(features.getQuantidadeEmpresasRegiao() / 80.0, 1.0);
        score += densidadeEmpresas * 0.3;
        
        // Capital social próximo da média -> maior probabilidade (até 30%)
        double diffCapital = Math.abs(features.getCapitalSocial() - features.getCapitalMedioRegiao());
        double capitalScore = 1.0 - Math.min(diffCapital / (features.getCapitalMedioRegiao() + 1), 1.0);
        score += capitalScore * 0.3;
        
        // Densidade empresarial moderada é melhor (até 20%)
        double densidadeOtimizada = 1.0 - Math.abs(features.getDensidadeEmpresarial() - 0.5) * 2.0;
        score += Math.max(densidadeOtimizada, 0) * 0.2;
        
        // Faixa de capital média tende a ser melhor (20%)
        if (features.getFaixaCapitalSocial() == 1) {
            score += 0.2; // Capital médio é ideal
        } else if (features.getFaixaCapitalSocial() == 2) {
            score += 0.1; // Capital alto é bom
        }
        
        // Adicionar variação baseada no CNAE
        double hashCnae = Math.abs(features.getCnaePrincipal().hashCode() % 100) / 100.0;
        score += (hashCnae - 0.5) * 0.1;
        
        return Math.max(0.1, Math.min(score, 0.95));
    }
    
    public PredictionResult preverSucesso(String cnae, String municipio, double capitalSocial, Connection connection) {
        System.out.println("🎯 Iniciando predição para CNAE: " + cnae + ", Município: " + municipio + ", Capital: " + capitalSocial);
        
        if (!modeloTreinado) {
            System.out.println("⚠️ Usando predição default - modelo não treinado");
            return criarPredicaoDefault(cnae, municipio, capitalSocial);
        }
        
        try {
            TrainingDataRepository repo = new TrainingDataRepository(connection);
            List<EmpresaFeatures> featuresList = repo.obterDadosParaPredicao(cnae, municipio, capitalSocial);
            
            EmpresaFeatures features;
            boolean dadosReais = true;
            
            if (featuresList.isEmpty()) {
                System.out.println("⚠️ Sem dados específicos, usando análise genérica");
                // Criar features básicas para análise
                features = new EmpresaFeatures(
                    cnae, municipio, capitalSocial,
                    50, // quantidade estimada
                    50000, // capital médio estimado
                    0.5, // densidade média
                    capitalSocial < 10000 ? 0 : (capitalSocial < 50000 ? 1 : 2)
                );
                dadosReais = false;
            } else {
                features = featuresList.get(0);
                System.out.println("✅ Dados reais encontrados: " + features.getQuantidadeEmpresasRegiao() + " empresas na região");
            }
            
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
            
            System.out.println("✅ Predição ML realizada: " + classePredita + " (" + String.format("%.1f", probabilidade * 100) + "%)");
            System.out.println("📊 Distribuição: ALTA=" + String.format("%.2f", distribution[2]) + 
                             ", MÉDIA=" + String.format("%.2f", distribution[1]) + 
                             ", BAIXA=" + String.format("%.2f", distribution[0]));
            
            return criarResultadoPredicaoAprimorado(probabilidade, classePredita, features, dadosReais);
            
        } catch (Exception e) {
            System.err.println("❌ Erro na predição ML: " + e.getMessage());
            return criarPredicaoComAnalise(cnae, municipio, capitalSocial);
        }
    }
    
    private PredictionResult criarResultadoPredicaoAprimorado(double probabilidade, String classe, EmpresaFeatures features, boolean dadosReais) {
        String[] fatoresCriticos;
        String recomendacao;
        
        // Cálculo de métricas detalhadas
        double capitalVsMedia = features.getCapitalSocial() / (features.getCapitalMedioRegiao() + 0.001);
        double saturacao = Math.min(features.getQuantidadeEmpresasRegiao() / 50.0, 1.0);
        
        if (classe.equals("ALTA")) {
            fatoresCriticos = new String[]{
                "Mercado consolidado na região",
                "Capital social adequado ao segmento", 
                "Baixa saturação do segmento",
                "Condições favoráveis para entrada",
                dadosReais ? "Baseado em dados reais da região" : "Análise com dados estimados"
            };
            recomendacao = "Ótimas condições para abertura - mercado com potencial de crescimento acima de 70%";
        } else if (classe.equals("MEDIA")) {
            fatoresCriticos = new String[]{
                "Concorrência estabelecida",
                "Capital social dentro da média regional",
                "Necessidade de diferencial competitivo",
                "Rentabilidade moderada esperada",
                dadosReais ? "Baseado em dados reais da região" : "Análise com dados estimados"
            };
            recomendacao = "Analise oportunidades de diferenciação - mercado competitivo mas viável (40-70% de sucesso)";
        } else {
            fatoresCriticos = new String[]{
                "Alta concorrência na região",
                "Capital social pode estar abaixo do ideal",
                "Possível saturação do segmento",
                "Rentabilidade potencialmente baixa",
                dadosReais ? "Baseado em dados reais da região" : "Análise com dados estimados"
            };
            recomendacao = "Considere: 1) Localização alternativa 2) Segmento diferente 3) Maior capital inicial (menos de 40% de sucesso)";
        }
        
        PredictionResult result = new PredictionResult();
        result.setProbabilidadeSucesso(probabilidade);
        result.setClassificacao(classe);
        result.setFatoresCriticos(fatoresCriticos);
        result.setRecomendacao(recomendacao);
        
        return result;
    }
    
    private PredictionResult criarPredicaoComAnalise(String cnae, String municipio, double capitalSocial) {
        // Análise inteligente baseada em regras quando ML não está disponível
        double probabilidade = 0.5;
        String classificacao = "MEDIA";
        String[] fatoresCriticos;
        String recomendacao;
        
        // Simular análise baseada em características conhecidas
        if (capitalSocial > 100000) {
            probabilidade = 0.75;
            classificacao = "ALTA";
            fatoresCriticos = new String[]{
                "Capital social acima da média",
                "Condições financeiras favoráveis",
                "Maior capacidade de investimento",
                "Análise baseada em regras (ML offline)"
            };
            recomendacao = "Bom potencial - capital adequado para investimentos iniciais";
        } else if (capitalSocial < 5000) {
            probabilidade = 0.25;
            classificacao = "BAIXA";
            fatoresCriticos = new String[]{
                "Capital social limitado",
                "Necessidade de planejamento financeiro cuidadoso",
                "Risco de capital de giro insuficiente",
                "Análise baseada em regras (ML offline)"
            };
            recomendacao = "Considere aumentar o capital ou buscar financiamento";
        } else {
            probabilidade = 0.55;
            classificacao = "MEDIA";
            fatoresCriticos = new String[]{
                "Capital social dentro da faixa média",
                "Mercado competitivo",
                "Necessidade de plano de negócios detalhado",
                "Análise baseada em regras (ML offline)"
            };
            recomendacao = "Realize uma análise de viabilidade detalhada";
        }
        
        System.out.println("🔍 Usando análise inteligente: " + classificacao + " (" + String.format("%.1f", probabilidade * 100) + "%)");
        return new PredictionResult(probabilidade, classificacao, fatoresCriticos, recomendacao);
    }
    
    private PredictionResult criarPredicaoDefault(String cnae, String municipio, double capitalSocial) {
        // Fallback básico quando não há dados suficientes
        return new PredictionResult(
            0.5, 
            "MEDIA", 
            new String[]{"Análise básica - dados limitados", "Serviço ML em inicialização"},
            "Considere realizar uma pesquisa de mercado mais detalhada"
        );
    }
    
    // Método para verificar status do serviço
    public String getStatus() {
        return status;
    }
    
    public boolean isModeloTreinado() {
        return modeloTreinado;
    }
    
    public int getTotalAmostras() {
        return totalAmostras;
    }
}