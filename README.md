# CNPJ Analyzer - Sistema Completo

Sistema de análise de dados do CNPJ com backend Java e aplicativo mobile.

## Estrutura do Projeto

- `backend/` - API REST Java com processamento de dados
- `mobile/` - Aplicativo Android nativo

## Como executar

### Backend
```bash
cd backend
mvn clean compile exec:java -Dexec.mainClass="com.novasemp.cnpj.api.Api"