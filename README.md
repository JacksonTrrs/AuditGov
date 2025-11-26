# AuditGov: Monitoramento de Gastos em Viagens a Serviço

## 📋 Descrição

O AuditGov é uma solução de Engenharia de Dados desenvolvida para processar, sanar e estruturar os dados abertos de "Viagens a Serviço" do Governo Federal. A aplicação resolve o problema da baixa qualidade dos dados brutos (arquivos CSV desnormalizados, com redundâncias e erros de formatação) através de uma rotina de ETL (Extract, Transform, Load) desenvolvida em Java. O sistema normaliza as informações no banco de dados MariaDB, permitindo auditorias precisas sobre gastos públicos e destinos de viagens.

## 🛠️ Tecnologias Utilizadas

- **Java 21** - Linguagem de programação
- **MariaDB** - Banco de dados relacional
- **OpenCSV** - Biblioteca para processamento de arquivos CSV
- **Java Swing** - Interface gráfica do usuário
- **Maven** - Gerenciamento de dependências e build

## ✨ Funcionalidades

### 1. Importação de Dados (ETL)
- Importação de arquivos CSV com dados de viagens
- Processamento e normalização automática de dados
- Tratamento de erros e dados inconsistentes
- Normalização de nomes de órgãos e cidades (evita duplicatas)
- Cálculo automático de valores totais (diárias + passagens + outros - devoluções)

### 2. Visualização de Dados
- Tabela interativa com todas as viagens cadastradas
- Paginação para grandes volumes de dados
- Filtros e ordenação por data, valor, órgão e destino
- Formatação de valores monetários em Real (R$)
- Estatísticas gerais em tempo real

### 3. Relatórios e Análises

#### Relatório de Maiores Gastadores
- Lista os 5 órgãos com maior soma de valor total
- Identifica pontos de maior despesa
- Valores formatados em moeda brasileira

#### Relatório de Destinos Frequentes
- Lista as 10 cidades mais visitadas
- Agrupamento correto por cidade e UF
- Identifica fluxo de servidores públicos
- Quantidade de viagens por destino

## 🗂️ Estrutura das Entidades

### Modelo de Dados

#### Tabela: `viagem` (Fato)
Armazena as transações de viagens realizadas.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id_processo` | VARCHAR | Identificador único do processo de viagem |
| `data_inicio` | DATE | Data de início da viagem |
| `valor_total` | DECIMAL | Valor total da viagem (diárias + passagens + outros - devoluções) |
| `fk_orgao` | INT | Chave estrangeira para a tabela `orgao` |
| `fk_destino` | INT | Chave estrangeira para a tabela `cidade` |

#### Tabela: `orgao` (Dimensão)
Armazena os órgãos governamentais.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | INT | Chave primária (auto-incremento) |
| `nome` | VARCHAR | Nome do órgão (normalizado) |

#### Tabela: `cidade` (Dimensão)
Armazena as cidades de destino das viagens.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | INT | Chave primária (auto-incremento) |
| `nome` | VARCHAR | Nome da cidade |
| `uf` | VARCHAR(2) | Unidade Federativa (estado) |

### DTOs (Data Transfer Objects)

#### `OrgaoGastador`
Usado para relatórios de maiores gastadores.

```java
- nomeOrgao: String
- valorTotal: double
```

#### `DestinoFrequente`
Usado para relatórios de destinos frequentes.

```java
- nomeCidade: String
- uf: String
- quantidadeViagens: int
```

## 🏗️ Arquitetura do Projeto

```
src/main/java/org/example/
├── database/          # Camada de acesso a dados
│   ├── ConexaoFactory.java    # Factory para conexões com banco
│   └── DadosDAO.java          # Data Access Object (operações SQL)
├── model/             # Entidades de domínio
│   ├── Viagem.java
│   ├── Orgao.java
│   └── Cidade.java
├── dto/               # Data Transfer Objects
│   ├── OrgaoGastador.java
│   └── DestinoFrequente.java
├── service/           # Camada de serviços (lógica de negócio)
│   ├── ServicoImportacao.java      # ETL de importação
│   ├── ServicoConsulta.java        # Consultas e relatórios
│   └── GestaoEntidadesUnicas.java  # Normalização de entidades
├── util/              # Utilitários
│   └── TratamentoDados.java        # Limpeza e formatação de dados
└── ui/                # Interface gráfica
    ├── TelaPrincipal.java          # Janela principal
    └── controller/
        └── ControllerPrincipal.java # Controlador da interface
```

### Princípios de Design

- **Separação de Responsabilidades**: Cada camada tem uma responsabilidade única
- **Baixo Acoplamento**: Classes independentes e desacopladas
- **DRY (Don't Repeat Yourself)**: Sem duplicação de código
- **DAO Pattern**: Acesso a dados isolado em camada específica
- **Service Layer**: Lógica de negócio separada da apresentação

## 🚀 Como Executar

### Pré-requisitos

1. **Java 21** ou superior instalado
2. **Maven 3.6+** instalado
3. **MariaDB** instalado e em execução
4. Banco de dados `audit_gov` criado

### 1. Configuração do Banco de Dados

Execute os seguintes comandos SQL no MariaDB:

```sql
CREATE DATABASE IF NOT EXISTS audit_gov CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE audit_gov;

-- Tabela de órgãos
CREATE TABLE IF NOT EXISTS orgao (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    UNIQUE KEY uk_orgao_nome (nome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela de cidades
CREATE TABLE IF NOT EXISTS cidade (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    uf VARCHAR(2) NOT NULL,
    UNIQUE KEY uk_cidade_nome_uf (nome, uf)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela de viagens (fato)
CREATE TABLE IF NOT EXISTS viagem (
    id_processo VARCHAR(255) NOT NULL,
    data_inicio DATE NOT NULL,
    valor_total DECIMAL(15,2) NOT NULL,
    fk_orgao INT NOT NULL,
    fk_destino INT NOT NULL,
    PRIMARY KEY (id_processo),
    FOREIGN KEY (fk_orgao) REFERENCES orgao(id),
    FOREIGN KEY (fk_destino) REFERENCES cidade(id),
    INDEX idx_data_inicio (data_inicio),
    INDEX idx_valor_total (valor_total)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2. Configuração da Conexão

Edite o arquivo `src/main/java/org/example/database/ConexaoFactory.java` e ajuste as credenciais:

```java
private static final String URL = "jdbc:mariadb://localhost:3306/audit_gov";
private static final String USUARIO = "root";        // Seu usuário
private static final String SENHA = "";          // Sua senha
```

### 3. Compilação e Execução

#### Opção 1: Via IDE (IntelliJ IDEA / Eclipse)
1. Importe o projeto como projeto Maven
2. Configure o JDK 21
3. Execute a classe `org.example.Main`

#### Opção 2: Via Terminal

```bash
# Compilar o projeto
mvn clean compile

# Executar a aplicação
mvn exec:java -Dexec.mainClass="org.example.Main"
```

#### Opção 3: Gerar JAR Executável

```bash
# Gerar JAR com dependências
mvn clean package

# Executar o JAR
java -jar target/AuditGov-1.0-SNAPSHOT.jar
```

### 4. Uso da Aplicação

1. **Iniciar a aplicação**: Execute o método `main` da classe `Main`
2. **Importar dados**: Clique em "Importar CSV" e selecione o arquivo CSV com os dados de viagens
3. **Visualizar dados**: As viagens serão exibidas automaticamente na tabela principal
4. **Consultar relatórios**: 
   - Aba "Maiores Gastadores": Visualiza os 5 órgãos que mais gastam
   - Aba "Destinos Frequentes": Visualiza as 10 cidades mais visitadas
5. **Atualizar dados**: Use os botões de atualização para recarregar as informações

## 📝 Formato do Arquivo CSV

O arquivo CSV deve seguir o formato dos dados abertos do Portal da Transparência:

- Separador: ponto e vírgula (`;`)
- Encoding: ISO-8859-1
- Colunas esperadas:
  - Coluna 0: ID do Processo
  - Coluna 6: Nome do Órgão
  - Coluna 14: Data de Início
  - Coluna 16: Destino (formato: "Cidade/UF")
  - Últimas 4 colunas: Diárias, Passagens, Devolução, Outros

## 🔍 Exemplos de Consultas SQL

### Top 5 Maiores Gastadores
```sql
SELECT o.nome as nome_orgao, SUM(v.valor_total) as valor_total 
FROM viagem v 
INNER JOIN orgao o ON v.fk_orgao = o.id 
GROUP BY o.id, o.nome 
ORDER BY valor_total DESC 
LIMIT 5;
```

### Top 10 Destinos Frequentes
```sql
SELECT c.nome as nome_cidade, c.uf, COUNT(*) as quantidade 
FROM viagem v 
INNER JOIN cidade c ON v.fk_destino = c.id 
GROUP BY c.id, c.nome, c.uf 
ORDER BY quantidade DESC 
LIMIT 10;
```

## 📊 Estatísticas Disponíveis

A aplicação calcula automaticamente:
- Total de viagens cadastradas
- Valor total gasto em viagens
- Valor médio por viagem
- Valor mínimo e máximo de viagens

## 🐛 Tratamento de Dados

O sistema realiza automaticamente:
- Remoção de acentos e normalização de texto
- Limpeza de espaços e caracteres especiais
- Conversão de valores monetários (formato brasileiro)
- Normalização de datas (dd/MM/yyyy)
- Separação de cidade e UF
- Tratamento de valores nulos ou inválidos

## 📄 Licença

Este projeto é desenvolvido para fins educacionais e de auditoria pública.

