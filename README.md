# Linketinder - ZG-Hero Project

Aplicação **full stack em desenvolvimento**, criada como parte do **Acelera ZG**, composta por um backend desenvolvido em **Groovy** e um frontend desenvolvido em **TypeScript**.

O projeto consiste na implementação de uma plataforma de contratação inspirada no **LinkedIn** e no **Tinder**, aproximando empresas e candidatos com base em suas competências (*skills*) e interesses.

A primeira versão do projeto foi desenvolvida como um **MVP executado pelo terminal**, com foco em Programação Orientada a Objetos, estruturas de dados e regras de negócio.

Na segunda etapa, foi desenvolvido um **frontend independente em TypeScript**, adicionando interfaces gráficas para cadastro, visualização de vagas, perfis de candidatos e empresas e visualização de dados.

Posteriormente, o backend passou por uma evolução arquitetural, incorporando um **banco de dados PostgreSQL**, persistência através de **JDBC**, uma camada de **DAO**, DTOs para transferência de dados e testes unitários utilizando **Spock**.

> **Importante:** nesta etapa o frontend ainda não possui comunicação com o backend. As funcionalidades do frontend são executadas de forma independente utilizando o **LocalStorage do navegador**. A integração entre frontend e backend será abordada posteriormente no **KIT 2**.

---

## Sobre o projeto

O **Linketinder** tem como objetivo aproximar empresas e candidatos com base em suas competências (*skills*).

A plataforma permite que **empresas e candidatos demonstrem interesse uns nos outros através de curtidas**. Quando uma empresa e um candidato demonstram interesse mútuo, o sistema identifica um **match** entre os dois.

O projeto atualmente possui duas partes principais:

* **Backend:** desenvolvido em Groovy, inicialmente como MVP executado pelo terminal e posteriormente integrado a um banco de dados PostgreSQL;
* **Frontend:** interface web desenvolvida em TypeScript.

O frontend possui armazenamento próprio utilizando o **LocalStorage**, permitindo executar as funcionalidades da interface sem depender do servidor.

---

# Backend

## Tecnologias utilizadas

O backend foi desenvolvido utilizando:

* **Groovy**
* Programação Orientada a Objetos
* Estruturas de Dados
* **PostgreSQL**
* **JDBC**
* **Spock**
* **JaCoCo**
* **PIT / Mutation Testing**
* Git e GitHub

A persistência do backend é realizada utilizando **JDBC puro**, sem utilização de frameworks ORM como JPA ou Hibernate.

A comunicação com o banco é centralizada através de uma `ConnectionFactory`, responsável pela criação das conexões com o PostgreSQL.

---

## Arquitetura do backend

O backend foi reorganizado utilizando separação de responsabilidades entre diferentes camadas:

```text
model
   ↓
dto
   ↓
dao
   ↓
service
   ↓
view
```

Além dessas camadas, o projeto possui estruturas específicas para:

* `validation` — validações;
* `database` — conexão com o banco de dados;
* `model` — entidades do domínio;
* `dto` — objetos utilizados para transferência de dados;
* `dao` — acesso e persistência dos dados;
* `service` — regras e orquestração da aplicação.

A aplicação segue uma abordagem baseada na separação entre **regra de negócio e persistência**, permitindo testar os Services isoladamente através de mocks dos DAOs.

---

## Estrutura do backend

O backend está organizado aproximadamente da seguinte maneira:

```text
com.jpcode
├── model
│   ├── core
│   │   ├── Candidato
│   │   ├── Empresa
│   │   ├── Pessoa
│   │   ├── PessoaInterface
│   │   └── Vaga
│   │
│   ├── referencia
│   │   ├── Pais
│   │   ├── Estado
│   │   └── Competencia
│   │
│   ├── formacao
│   │   ├── Formacao
│   │   └── InstituicaoFormacao
│   │
│   └── relacionamento
│       └── Match
│
├── dto
│   ├── candidato
│   ├── empresa
│   ├── vaga
│   ├── match
│   └── competencia
│
├── dao
│   ├── candidato
│   ├── empresa
│   ├── vaga
│   ├── referencia
│   ├── relacionamento
│   ├── match
│   └── formacao
│
├── service
├── validation
├── database
├── view
└── Main
```

---

# Persistência com PostgreSQL

Uma das principais evoluções do backend foi a substituição do armazenamento exclusivamente em memória por persistência em um banco de dados relacional.

O projeto utiliza **PostgreSQL** para armazenar os dados de:

* Candidatos;
* Empresas;
* Vagas;
* Competências;
* Formações;
* Instituições de formação;
* Curtidas;
* Matches;
* Países;
* Estados.

A comunicação com o banco é realizada através de **JDBC**, utilizando `PreparedStatement`, `ResultSet`, transações e chaves geradas pelo banco.

---

## ConnectionFactory

A conexão com o banco é centralizada através da classe:

```text
database/ConnectionFactory
```

Essa classe é responsável por fornecer as conexões utilizadas pelos DAOs.

A camada de serviço não realiza consultas SQL diretamente.

O fluxo utilizado é:

```text
Service
   ↓
DAO
   ↓
ConnectionFactory
   ↓
PostgreSQL
```

---

# DAO

A camada DAO foi criada para concentrar as operações de persistência.

Entre os principais DAOs estão:

* `CandidatoDAO`
* `EmpresaDAO`
* `VagaDAO`
* `CompetenciaDAO`
* `PaisDAO`
* `EstadoDAO`
* `FormacaoDAO`
* `InstituicaoFormacaoDAO`
* `CompetenciasCandidatoDAO`
* `CompetenciasVagaDAO`
* `CandidatoCurtirDAO`
* `EmpresaCurtirDAO`
* `MatchDAO`

Os DAOs são responsáveis por executar operações como:

* `INSERT`;
* `SELECT`;
* `UPDATE`;
* `DELETE`;
* associação entre entidades;
* consultas envolvendo múltiplas tabelas;
* conversão dos resultados do banco para objetos do domínio ou DTOs.

---

# Services

A camada de Service concentra a orquestração das operações da aplicação.

Entre os principais Services estão:

* `CandidatoService`
* `EmpresaService`
* `VagaService`
* `CompetenciaService`
* `ReferenciaService`
* `MatchService`

Os Services recebem os DAOs necessários através do construtor e coordenam as operações realizadas pela aplicação.

Por exemplo, o cadastro de um candidato pode seguir o fluxo:

```text
CandidatoService
       ↓
PaisDAO / EstadoDAO
       ↓
CandidatoDAO
       ↓
CompetenciasCandidatoDAO
       ↓
PostgreSQL
```

---

# DTOs

Foram adicionados DTOs para representar dados específicos utilizados nas consultas e na apresentação das informações.

Entre eles estão:

* `CandidatoMatchDTO`
* `CandidatoAnonimoDTO`
* `EmpresaMatchDTO`
* `VagaEmpresaDTO`
* `VagaAnonimaDTO`
* `VagaMatchDTO`
* `MatchEncontradoDTO`
* `RemoverCompetenciaDTO`

Os DTOs permitem retornar somente as informações necessárias para determinada operação, além de ajudar a manter separadas as entidades de domínio das estruturas utilizadas para apresentação e consulta.

---

# Funcionalidades

O backend possui:

* Cadastro de candidatos;
* Cadastro de empresas;
* Cadastro de vagas;
* Listagem de candidatos;
* Listagem de empresas;
* Busca de candidatos por ID;
* Busca de empresas por ID;
* Busca de vagas por ID;
* Listagem de vagas por empresa;
* Listagem de todas as vagas;
* Armazenamento das competências dos candidatos;
* Armazenamento das competências exigidas pelas vagas;
* Adição e remoção de competências;
* Curtidas entre candidatos e vagas;
* Curtidas entre empresas e candidatos;
* Identificação e armazenamento de *matches*;
* Login de candidatos;
* Login de empresas;
* Desativação lógica de candidatos;
* Desativação lógica de empresas;
* Atualização de candidatos;
* Atualização de empresas;
* Atualização de vagas;
* Exclusão de vagas;
* Consultas de candidatos interessados em vagas;
* Consultas de candidatos curtidos por empresas;
* Consultas de vagas curtidas por candidatos;
* Menu de interação pelo terminal.

O projeto possui, inicialmente, no mínimo **5 candidatos e 5 empresas pré-cadastrados**.

---

# Competências

As competências passaram a ser tratadas como dados persistidos no banco.

A classe `Competencia` representa uma competência armazenada na tabela `competencias`, contendo:

* ID;
* Nome.

Diferentemente da primeira versão, as competências não dependem exclusivamente de um enum para sua persistência.

As associações entre competências e candidatos ou vagas são realizadas através das tabelas intermediárias:

```text
candidatos_competencias
vagas_competencias
```

O sistema consulta o ID da competência no banco antes de criar a associação.

---

# Cadastro de candidatos

O cadastro de candidatos é realizado através do `CandidatoService`.

O processo consulta previamente os IDs correspondentes ao país e estado informados e então realiza o cadastro.

As competências informadas são posteriormente associadas ao candidato através da tabela de relacionamento.

O banco é responsável por gerar o ID do candidato.

O DAO recupera o ID gerado utilizando `RETURN_GENERATED_KEYS` e atribui o valor ao objeto salvo.

---

# Cadastro de empresas

O cadastro de empresas é realizado através do `EmpresaService`.

O país e o estado são convertidos para seus respectivos IDs através dos DAOs de referência.

Após a criação, o ID da empresa é gerado pelo PostgreSQL e retornado ao objeto.

Empresas possuem um campo:

```text
ativo
```

Esse campo permite realizar **exclusão lógica**.

---

# Cadastro de vagas

As vagas são cadastradas através do `VagaService`.

Uma vaga possui:

* Nome;
* Descrição;
* Local;
* Empresa responsável.

Após a criação da vaga, suas competências são associadas através da tabela `vagas_competencias`.

O fluxo é:

```text
VagaService
     ↓
VagaDAO.salvar()
     ↓
PostgreSQL gera o ID
     ↓
CompetenciasVagaDAO
     ↓
Associação das competências
```

---

# Curtidas

O sistema possui diferentes relacionamentos de curtidas persistidos no banco.

## Candidato → Vaga

A tabela:

```text
vagas_curtidas_candidato
```

registra quando um candidato demonstra interesse em uma vaga.

## Empresa → Candidato

A tabela:

```text
candidatos_curtidos_empresa
```

registra quando uma empresa demonstra interesse em um candidato.

As associações possuem restrições de unicidade para impedir registros duplicados.

---

# Matches

Quando existe interesse mútuo entre as partes, o sistema registra o relacionamento na tabela:

```text
match
```

A tabela possui:

* ID;
* ID do candidato;
* ID da empresa;
* ID da vaga.

Existe uma restrição de unicidade para impedir que o mesmo match seja registrado mais de uma vez.

O `MatchDAO` também possui consultas específicas para:

* visualizar matches por empresa;
* visualizar matches por candidato.

Os resultados podem ser convertidos para `MatchEncontradoDTO`, contendo informações do candidato, empresa, vaga e competências relacionadas.

---

# Anonimato

O backend também utiliza DTOs específicos para preservar o anonimato em determinadas consultas.

Entre eles:

```text
CandidatoAnonimoDTO
VagaAnonimaDTO
```

Esses DTOs permitem retornar somente as informações necessárias antes de um relacionamento que permita a identificação completa.

---

# Exclusão e desativação

Candidatos e empresas utilizam **exclusão lógica**.

Em vez de remover fisicamente o registro, o campo:

```text
ativo
```

é alterado para:

```text
false
```

Isso preserva os dados históricos relacionados à entidade.

As vagas, por outro lado, podem ser removidas fisicamente.

Quando uma vaga é excluída, os relacionamentos dependentes associados à vaga podem ser removidos automaticamente através de `ON DELETE CASCADE`.

Essa regra é aplicada aos relacionamentos que dependem diretamente da existência da vaga, como:

* `vagas_competencias`;
* `vagas_curtidas_candidato`;
* `match`.

A exclusão de uma empresa **não remove automaticamente suas vagas**, preservando a separação entre essas regras de negócio.

---

# Regras de unicidade

As tabelas de relacionamento possuem restrições para impedir duplicações.

Entre elas:

```text
candidatos_competencias
    (id_candidato, id_competencia)

vagas_competencias
    (id_vaga, id_competencia)

vagas_curtidas_candidato
    (id_candidato, id_vaga)

candidatos_curtidos_empresa
    (id_empresa, id_candidato)

candidato_formacoes
    (id_candidato, id_formacao)

match
    (id_candidato, id_empresa, id_vaga)
```

As operações de associação também utilizam `ON CONFLICT DO NOTHING` quando apropriado, evitando erros causados por tentativas de inserir uma relação já existente.

---

# Idade

A idade dos candidatos não é armazenada diretamente como uma coluna no banco.

O banco armazena a:

```text
data_nascimento
```

e as consultas calculam a idade quando necessário utilizando PostgreSQL:

```sql
EXTRACT(YEAR FROM AGE(c.data_nascimento))
```

Dessa maneira, a idade não precisa ser atualizada manualmente conforme o tempo passa.

---

# Testes

O backend possui testes unitários utilizando **Spock Framework**.

Os testes dos Services foram estruturados para testar exclusivamente a lógica de negócio e a comunicação entre Service e DAO.

Os DAOs são substituídos por mocks:

```text
Service
   ↓
DAO Mock
```

Dessa maneira, os testes dos Services **não acessam o PostgreSQL**.

---

## Testes unitários dos Services

Entre os Services testados estão:

* `CandidatoService`
* `EmpresaService`
* `VagaService`
* `CompetenciaService`
* `ReferenciaService`
* `MatchService`

Os testes verificam operações como:

* Cadastro;
* Login;
* Atualização;
* Desativação;
* Exclusão;
* Busca;
* Listagem;
* Associação de competências;
* Remoção de competências;
* Curtidas;
* Busca de candidatos interessados;
* Busca de vagas curtidas;
* Busca de matches.

Os DAOs são utilizados como mocks para verificar se as operações esperadas foram chamadas.

Exemplo:

```groovy
1 * empresaDAO.desativar(1L)
```

Também são utilizados testes parametrizados com `@Unroll` quando uma mesma regra precisa ser validada com diferentes entradas.

Por exemplo, login inválido quando:

* e-mail vazio;
* senha vazia;
* ambos vazios.

---

## Isolamento dos testes

Os testes dos Services não dependem do banco de dados.

Por exemplo:

```groovy
EmpresaDAO empresaDAO = Mock()
```

permite testar:

```text
EmpresaService
      ↓
EmpresaDAO Mock
```

sem executar SQL.

Os testes dos DAOs são responsáveis pela camada de persistência, enquanto os testes dos Services verificam a lógica e a orquestração.

Essa separação permite identificar com maior precisão se um problema está relacionado à regra de negócio ou à persistência.

---

# Qualidade e Mutation Testing

O projeto utiliza **JaCoCo** para acompanhamento da cobertura dos testes.

Também é utilizado **PIT Mutation Testing** para verificar a efetividade dos testes.

O Mutation Testing modifica temporariamente partes do código para verificar se os testes são capazes de detectar essas alterações.

Durante o desenvolvimento do projeto foram realizados exercícios de eliminação de mutantes, buscando aumentar a quantidade de mutantes detectados pelos testes.

---

# Frontend

A segunda etapa do projeto consiste na criação de uma interface web para o Linketinder utilizando **TypeScript**.

O objetivo é transformar as funcionalidades principais do MVP em uma interface gráfica, permitindo que candidatos e empresas interajam com a plataforma através do navegador.

## Tecnologias utilizadas

O frontend utiliza:

* **HTML5**
* **CSS3**
* **TypeScript 5.9.3**
* **Webpack**
* **Gulp**
* **ts-loader**
* **LocalStorage**
* **Chart.js** para visualização de dados

O projeto utiliza **Webpack + Gulp** para compilação e organização dos arquivos TypeScript.


---

## Estrutura do frontend

### Models

Contém as classes responsáveis pela representação dos objetos do sistema, como:

* Pessoa
* Candidato
* Empresa
* Vaga
* Competencia

### Services

Contém as operações relacionadas às regras do frontend, como:

* Cadastro de candidatos;
* Cadastro de empresas;
* Cadastro de vagas;
* Busca de candidatos e empresas;
* Curtidas;
* Conversão dos dados armazenados no LocalStorage;
* Validação de competências.

Entre os principais serviços estão:

* CandidatoService
* EmpresaService
* VagaService

### Pages

Contém a lógica responsável por preencher e manipular as páginas HTML.

Entre as páginas estão:

* LoginPage
* CadastroPage
* CandidatoPage
* EmpresaPage

---

# Funcionalidades do Frontend

## Cadastro de candidatos

O frontend possui uma tela específica para cadastro de candidatos.

O cadastro permite informar dados como:

* Nome;
* E-mail;
* CPF;
* Idade;
* Estado;
* CEP;
* Formação;
* Descrição;
* Competências.

Após o cadastro, os dados são armazenados no **LocalStorage** do navegador.

---

## Cadastro de empresas

Também existe uma tela específica para cadastro de empresas.

Os dados da empresa incluem:

* Nome;
* E-mail;
* CNPJ;
* País;
* Estado;
* CEP;
* Descrição.

As empresas cadastradas também são armazenadas no LocalStorage.

---

## Login

O frontend possui uma tela inicial que permite selecionar o tipo de usuário e localizar um candidato ou empresa cadastrado.

Após a identificação, o usuário é direcionado para seu respectivo perfil.

Os usuários atualmente são identificados através dos dados armazenados no LocalStorage.

---

# Perfil do candidato

A página do candidato apresenta seus dados principais e permite visualizar as vagas cadastradas no sistema.

O candidato pode:

* Visualizar todas as vagas disponíveis;
* Visualizar título, descrição, tipo e localização da vaga;
* Visualizar as competências associadas à vaga;
* Curtir uma vaga;
* Visualizar quais vagas já foram curtidas.

As vagas curtidas são armazenadas no perfil do candidato no LocalStorage.

---

# Perfil da empresa

A página da empresa funciona como um dashboard.

Ela apresenta:

* Dados da empresa;
* Formulário para criação de vagas;
* Lista de vagas cadastradas pela empresa;
* Quantidade de candidatos interessados em cada vaga;
* Lista de candidatos que demonstraram interesse nas vagas;
* Competências dos candidatos;
* Formação dos candidatos;
* Gráfico de candidatos por competência.

---

# Cadastro de vagas

Empresas podem cadastrar novas vagas diretamente através do dashboard.

Uma vaga possui informações como:

* Nome;
* Tipo;
* Localização;
* Descrição;
* Competências exigidas.

As competências são validadas antes da criação da vaga.

Após o cadastro, a vaga é armazenada no LocalStorage e associada à empresa responsável.

---

# Curtidas

O mecanismo de curtidas foi implementado também no frontend.

### Candidato

O candidato pode curtir uma vaga.

Quando uma vaga é curtida:

* A vaga é adicionada à lista de vagas curtidas do candidato;
* A vaga passa a registrar o candidato interessado;
* O botão é alterado visualmente para indicar que a vaga já foi curtida.

### Empresa

A empresa pode curtir candidatos que demonstraram interesse em suas vagas.

Quando uma empresa curte um candidato:

* O candidato é adicionado à lista de candidatos curtidos pela empresa;
* O botão é alterado para indicar que o candidato já foi curtido.

---

# Anonimato

O frontend respeita a regra de anonimato definida no desafio.

Antes de existir um *match*, as informações de identificação devem permanecer restritas.

Na visão da empresa, os candidatos são apresentados com informações como:

* Formação;
* Competências;
* Vaga de interesse;
* Descrição.

Na visão do candidato, as vagas apresentam as informações necessárias para avaliar a oportunidade.

A lógica completa de *match* não é o foco desta etapa, conforme definido no enunciado.

---

# Gráfico de candidatos por competência

O perfil da empresa possui um **gráfico de barras** que apresenta a quantidade de candidatos que possuem cada competência.

O gráfico é gerado dinamicamente a partir dos candidatos disponíveis e suas respectivas competências.

A visualização utiliza a biblioteca **Chart.js**.

---

# Armazenamento no LocalStorage

Como o frontend ainda não possui comunicação com o backend, os dados são armazenados temporariamente no **LocalStorage** do navegador.

Entre as informações armazenadas estão:

* candidatos
* empresas
* vagas
* candidatoLogado
* empresaLogada

O fluxo utilizado é baseado em:

```text
Objeto TypeScript
       ↓
JSON.stringify()
       ↓
LocalStorage
       ↓
JSON.parse()
       ↓
Objeto JSON
       ↓
Novo objeto da classe correspondente
```

A conversão dos dados armazenados para instâncias das classes do domínio permite que o frontend continue trabalhando com os métodos e comportamentos definidos nos modelos, mesmo depois que os dados são recuperados do LocalStorage.

---

# Conceitos utilizados no Frontend

Além dos conceitos utilizados no backend, a implementação do frontend utiliza conceitos de desenvolvimento web e TypeScript.

Entre eles:

* TypeScript;
* Tipagem estática;
* Classes;
* Interfaces;
* Herança;
* Encapsulamento;
* Enums;
* Generics;
* Manipulação do DOM;
* Eventos;
* Arrow Functions;
* Array methods, como map, find e some;
* JSON;
* LocalStorage;
* Modularização através de import e export;
* Separação de responsabilidades;
* Compilação de TypeScript para JavaScript.

---

# Organização do projeto

## Backend

O backend está organizado em pacotes para separar as responsabilidades:

* `model` - classes responsáveis pela representação dos candidatos, empresas, pessoas, vagas, referências, formações e matches;
* `dto` - objetos utilizados para transferência e apresentação de dados;
* `dao` - acesso e persistência dos dados no PostgreSQL;
* `service` - regras e operações relacionadas aos candidatos, empresas, vagas, curtidas, competências e matches;
* `validation` - validações utilizadas pela aplicação;
* `database` - gerenciamento das conexões com o PostgreSQL;
* `view` - interação com o usuário através do terminal;
* `Main` - ponto de entrada da aplicação.

## Frontend

O frontend utiliza uma organização baseada em responsabilidades:

* models - classes e interfaces que representam os dados;
* services - operações e regras do frontend;
* pages - lógica de cada página;
* data - dados iniciais;
* app - inicialização e controle da aplicação;
* html - páginas da aplicação;
* css - estilos da interface;
* ts - código TypeScript.

---

# Banco de Dados

O projeto possui uma estrutura de banco de dados relacional desenvolvida para armazenar os dados de candidatos, empresas, vagas, competências e formações.

O banco foi modelado utilizando o **dbdiagram.io**, seguindo os relacionamentos necessários para representar as regras de negócio do Linketinder.

O backend utiliza **PostgreSQL** como banco de dados e **JDBC** para realizar a comunicação com o banco.

## Estrutura do banco de dados

O banco é composto pelas seguintes tabelas:

### Candidatos

Armazena os dados pessoais e de cadastro dos candidatos, incluindo:

* Nome;
* Sobrenome;
* E-mail;
* Senha;
* Data de nascimento;
* CPF;
* País;
* Estado;
* CEP;
* Descrição pessoal;
* Status de atividade.

Um candidato pode possuir diversas competências e formações.

A idade não é armazenada diretamente. Ela é calculada a partir da data de nascimento nas consultas.

### Competências

Armazena as competências disponíveis no sistema.

As competências podem ser utilizadas tanto pelos candidatos quanto pelas vagas.

Exemplos:

* Java;
* Python;
* Groovy;
* Angular;
* Spring;
* Entre outras.

### Candidatos_Competencias

Tabela responsável por representar o relacionamento entre candidatos e competências.

Um candidato pode possuir várias competências e uma mesma competência pode pertencer a vários candidatos, caracterizando um relacionamento **N:N**.

### Formação

Armazena as formações acadêmicas ou técnicas dos candidatos, contendo informações como:

* Curso;
* Candidato;
* Instituição;
* Data de início;
* Data de término ou previsão de término.

### Candidato_Formacoes

Tabela responsável por relacionar candidatos às suas formações.

### Instituicao_Formacao

Armazena as instituições relacionadas às formações dos candidatos.

### País

Armazena os países disponíveis para associação aos candidatos e empresas.

### Estados

Armazena os estados disponíveis para associação aos candidatos e empresas.

### Empresas

Armazena os dados das empresas cadastradas no sistema, incluindo:

* Nome;
* CNPJ;
* E-mail corporativo;
* Senha;
* Descrição;
* País;
* Estado;
* CEP;
* Status de atividade.

Uma empresa pode possuir diversas vagas.

### Vagas

Armazena as vagas cadastradas pelas empresas, contendo:

* Nome;
* Descrição;
* Local;
* Empresa responsável.

O relacionamento entre empresas e vagas é **1:N**, pois uma empresa pode possuir várias vagas, enquanto cada vaga pertence a uma única empresa.

### Vagas_Competencias

Tabela responsável por representar o relacionamento entre vagas e competências.

Uma vaga pode exigir várias competências e uma mesma competência pode ser exigida por várias vagas, caracterizando um relacionamento **N:N**.

### Vagas_Curtidas_Candidato

Tabela responsável por registrar quais vagas foram curtidas pelos candidatos.

### Candidatos_Curtidos_Empresa

Tabela responsável por registrar quais candidatos foram curtidos pelas empresas.

### Match

Tabela responsável por armazenar os matches encontrados entre:

* Candidato;
* Empresa;
* Vaga.

---

## Relacionamentos principais

A estrutura do banco possui os seguintes relacionamentos:

* **País → Candidatos:** 1:N;
* **País → Empresas:** 1:N;
* **Estado → Candidatos:** 1:N;
* **Estado → Empresas:** 1:N;
* **Candidatos → Formações:** relacionamento através das tabelas de formação;
* **Instituição → Formações:** 1:N;
* **Candidatos ↔ Competências:** N:N;
* **Empresa → Vagas:** 1:N;
* **Vagas ↔ Competências:** N:N;
* **Candidatos ↔ Vagas:** relacionamento através de curtidas;
* **Empresas ↔ Candidatos:** relacionamento através de curtidas;
* **Candidatos ↔ Empresas ↔ Vagas:** relacionamento através de matches.

As tabelas `candidatos_competencias` e `vagas_competencias` são utilizadas como tabelas intermediárias para representar os relacionamentos muitos-para-muitos.

---

## Integridade e restrições

O banco utiliza:

* Chaves primárias;
* Chaves estrangeiras;
* Restrições `UNIQUE`;
* `NOT NULL`;
* Valores padrão;
* `ON DELETE CASCADE` em relacionamentos dependentes de vagas.

As restrições de unicidade impedem duplicações nos relacionamentos.

O banco também utiliza IDs gerados pelo PostgreSQL para as entidades principais.

---

## MER / DER

O modelo entidade-relacionamento foi desenvolvido utilizando o **dbdiagram.io**.

A representação visual da estrutura do banco pode ser adicionada abaixo:

**Modelo Entidade-Relacionamento (MER/DER)**

<img width="1293" height="1081" alt="linketind_bd1" src="https://github.com/user-attachments/assets/51feb32e-5ac6-4395-8134-0a08f6282f30" />

---

## SQL

O arquivo SQL do projeto contém:

* Criação das tabelas;
* Definição das chaves primárias;
* Definição das chaves estrangeiras;
* Restrições de unicidade;
* Valores padrão;
* Regras de `ON DELETE CASCADE`;
* Dados iniciais para testes;
* Inserts das competências;
* Inserts dos países e estados;
* Inserts dos candidatos e empresas fictícios utilizados no projeto;
* Estrutura das tabelas de relacionamento.

O script foi desenvolvido para **PostgreSQL**.

---

# Execução

## Backend

Execute o projeto utilizando a configuração correspondente ao backend.

A aplicação será iniciada através do ponto de entrada `Main` e poderá ser utilizada pelo terminal.

Para executar o backend com persistência, é necessário possuir uma instância do **PostgreSQL** disponível e configurar os dados de conexão utilizados pela `ConnectionFactory`.

---

## Frontend

Entre no diretório do frontend:

```bash
cd frontend
```

Instale as dependências:

```bash
npm install
```

Compile o projeto utilizando o processo configurado com Webpack/Gulp.

O resultado compilado será disponibilizado no diretório de saída configurado pelo projeto.

Depois disso, abra a página inicial do frontend no navegador.

> O frontend não depende do backend para funcionar nesta etapa. Os dados são armazenados no LocalStorage do navegador.

---

# Próximos passos

A próxima etapa do projeto será a integração entre frontend e backend.

Essa integração permitirá substituir o armazenamento local por uma comunicação real com o servidor, utilizando uma API.

A comunicação entre as duas partes será explorada posteriormente no **KIT 2**.

---

# Menu do Backend

A aplicação backend possui um menu executado diretamente pelo terminal.

As opções disponíveis permitem consultar os dados cadastrados e executar funcionalidades da plataforma, incluindo:

```text
1 - Listar candidatos
2 - Listar empresas
3 - Curtir candidato
4 - Curtir empresa
5 - Visualizar matches
0 - Sair
```

O menu pode variar conforme o perfil que está utilizando a aplicação, apresentando apenas as operações disponíveis para cada tipo de usuário.

---

# Autor

Desenvolvido por **João Pedro Vaz** durante o **Acelera ZG**.


### Candidato

O candidato pode curtir uma vaga.

Quando uma vaga é curtida:

* A vaga é adicionada à lista de vagas curtidas do candidato;
* A vaga passa a registrar o candidato interessado;
* O botão é alterado visualmente para indicar que a vaga já foi curtida.

### Empresa

A empresa pode curtir candidatos que demonstraram interesse em suas vagas.

Quando uma empresa curte um candidato:

* O candidato é adicionado à lista de candidatos curtidos pela empresa;
* O botão é alterado para indicar que o candidato já foi curtido.

---

# Anonimato

O frontend respeita a regra de anonimato definida no desafio.

Antes de existir um *match*, as informações de identificação devem permanecer restritas.

Na visão da empresa, os candidatos são apresentados com informações como:

* Formação;
* Competências;
* Vaga de interesse;
* Descrição.

Na visão do candidato, as vagas apresentam as informações necessárias para avaliar a oportunidade.

A lógica completa de *match* não é o foco desta etapa, conforme definido no enunciado.

---

# Gráfico de candidatos por competência

O perfil da empresa possui um **gráfico de barras** que apresenta a quantidade de candidatos que possuem cada competência.

O gráfico é gerado dinamicamente a partir dos candidatos disponíveis e suas respectivas competências.

A visualização utiliza a biblioteca **Chart.js**.

---

# Armazenamento no LocalStorage

Como o frontend ainda não possui comunicação com o backend, os dados são armazenados temporariamente no **LocalStorage** do navegador.

Entre as informações armazenadas estão:

* candidatos
* empresas
* vagas
* candidatoLogado
* empresaLogada

O fluxo utilizado é baseado em:

Objeto TypeScript
       ↓
JSON.stringify()
       ↓
LocalStorage
       ↓
JSON.parse()
       ↓
Objeto JSON
       ↓
Novo objeto da classe correspondente


A conversão dos dados armazenados para instâncias das classes do domínio permite que o frontend continue trabalhando com os métodos e comportamentos definidos nos modelos, mesmo depois que os dados são recuperados do LocalStorage.

---

# Conceitos utilizados no Frontend

Além dos conceitos utilizados no backend, a implementação do frontend utiliza conceitos de desenvolvimento web e TypeScript.

Entre eles:

* TypeScript;
* Tipagem estática;
* Classes;
* Interfaces;
* Herança;
* Encapsulamento;
* Enums;
* Generics;
* Manipulação do DOM;
* Eventos;
* Arrow Functions;
* Array methods, como map, find e some;
* JSON;
* LocalStorage;
* Modularização através de import e export;
* Separação de responsabilidades;
* Compilação de TypeScript para JavaScript.

---

# Organização do projeto

## Backend

O backend está organizado em pacotes para separar as responsabilidades:

* model - classes responsáveis pela representação dos candidatos, empresas, pessoas, vagas e *matches*;
* enums - enumerações utilizadas pelo sistema;
* service - regras e operações relacionadas aos candidatos, empresas, vagas, curtidas e *matches*;
* validation - validações utilizadas pela aplicação;
* view - interação com o usuário através do terminal;
* Main - ponto de entrada da aplicação.

## Frontend

O frontend utiliza uma organização baseada em responsabilidades:

* models - classes e interfaces que representam os dados;
* services - operações e regras do frontend;
* pages - lógica de cada página;
* data - dados iniciais;
* app - inicialização e controle da aplicação;
* html - páginas da aplicação;
* css - estilos da interface;
* ts - código TypeScript.

---

# Execução

## Backend

Execute o projeto utilizando a configuração correspondente ao backend.

A aplicação será iniciada através do ponto de entrada `Main` e poderá ser utilizada pelo terminal.

---

# Banco de Dados

O projeto utiliza um banco de dados relacional desenvolvido em **PostgreSQL** para armazenar e relacionar os dados de candidatos, empresas, vagas, competências, formações e interações entre candidatos e empresas.

O modelo foi desenvolvido utilizando o **dbdiagram.io**, considerando as regras de negócio do Linketinder e os relacionamentos entre as entidades.

## Estrutura do banco de dados

O banco é composto pelas seguintes tabelas:

### Candidatos

Armazena os dados pessoais e de cadastro dos candidatos:

* Nome;
* Sobrenome;
* E-mail;
* Senha;
* Data de nascimento;
* CPF;
* País;
* CEP;
* Descrição;
* Status de atividade.

Cada candidato pode possuir diversas competências, formações e curtidas em vagas.

### Competências

Armazena as competências disponíveis no sistema.

As competências são compartilhadas entre candidatos e vagas, permitindo identificar quais habilidades um candidato possui e quais são exigidas por uma vaga.

Exemplos:

* Java;
* Python;
* Groovy;
* JavaScript;
* Entre outras.

### Candidatos_Competencias

Representa o relacionamento entre candidatos e competências.

Um candidato pode possuir diversas competências e uma mesma competência pode pertencer a diversos candidatos, caracterizando um relacionamento **N:N**.

### Formação

Armazena as formações acadêmicas ou técnicas dos candidatos:

* Curso;
* Candidato;
* Instituição;
* Data de início;
* Data de término ou previsão de término.

### Candidato_Formacoes

Relaciona os candidatos às suas formações.

Essa tabela permite associar um candidato às formações cadastradas no sistema.

### Instituicao_Formacao

Armazena as instituições de ensino relacionadas às formações dos candidatos.

### País

Armazena os países disponíveis para associação aos candidatos e empresas.

Um país pode estar relacionado a diversos candidatos e empresas.

### Empresas

Armazena os dados das empresas cadastradas:

* Nome;
* CNPJ;
* E-mail corporativo;
* Descrição;
* País;
* CEP.

Uma empresa pode cadastrar diversas vagas e também pode curtir candidatos.

### Vagas

Armazena as vagas cadastradas pelas empresas:

* Nome;
* Descrição;
* Local;
* Empresa responsável.

Cada vaga pertence a uma única empresa, enquanto uma empresa pode possuir diversas vagas, caracterizando um relacionamento **1:N**.

### Vagas_Competencias

Representa o relacionamento entre vagas e competências.

Uma vaga pode exigir diversas competências e uma mesma competência pode ser exigida por diversas vagas, caracterizando um relacionamento **N:N**.

### Vagas_Curtidas_Candidato

Armazena as vagas que foram curtidas pelos candidatos.

Cada registro representa uma interação em que um candidato demonstrou interesse em uma determinada vaga.

### Candidatos_Curtidos_Empresa

Armazena os candidatos que foram curtidos pelas empresas.

Cada registro representa uma interação em que uma empresa demonstrou interesse em um determinado candidato.

### Candidatos_Que_Curtiram_Vaga

Armazena os candidatos que demonstraram interesse em determinadas vagas.

Essa tabela mantém o registro dos candidatos que curtiram cada vaga para utilização na lógica de relacionamento entre candidatos, empresas e vagas.

### Match

Armazena os matches realizados entre candidatos, empresas e vagas.

Um match ocorre quando existe interesse de ambas as partes, relacionando:

* Um candidato;
* Uma empresa;
* Uma vaga.

Dessa forma, o registro do match identifica qual candidato e qual empresa demonstraram interesse na mesma oportunidade.

## Lógica aplicada

O funcionamento do banco segue a lógica de interação entre candidatos, empresas e vagas.

Primeiramente, um candidato é cadastrado no sistema e pode adicionar suas competências e formações.

As empresas também são cadastradas e podem criar uma ou mais vagas. Cada vaga possui suas próprias competências exigidas.

Depois do cadastro:

1. O candidato visualiza as vagas disponíveis.
2. O candidato pode curtir uma vaga de seu interesse.
3. A empresa pode visualizar candidatos de acordo com as regras de recrutamento do sistema e demonstrar interesse em um candidato.
4. As interações são armazenadas nas respectivas tabelas de curtidas.
5. Quando ocorre interesse de ambas as partes para uma determinada oportunidade, é registrado um **match**.
6. O match relaciona o candidato, a empresa e a vaga correspondente.

A estrutura permite manter separadas as informações de candidatos, empresas, vagas, competências e interações, evitando armazenar essas relações diretamente dentro das entidades principais.

## Relacionamentos principais

A estrutura do banco possui os seguintes relacionamentos:

* **País → Candidatos:** 1:N;
* **País → Empresas:** 1:N;
* **Candidatos → Formações:** 1:N;
* **Instituição → Formações:** 1:N;
* **Candidatos ↔ Competências:** N:N;
* **Empresa → Vagas:** 1:N;
* **Vagas ↔ Competências:** N:N;
* **Candidatos ↔ Vagas:** relacionamento através das curtidas;
* **Empresas ↔ Candidatos:** relacionamento através das curtidas;
* **Candidato + Empresa + Vaga → Match:** relacionamento que representa o interesse mútuo.

As tabelas `candidatos_competencias` e `vagas_competencias` são utilizadas para representar os relacionamentos **N:N** entre candidatos, vagas e competências.

As tabelas `vagas_curtidas_candidato`, `candidatos_curtidos_empresa` e `candidatos_que_curtiram_vaga` armazenam as interações realizadas durante o processo de interesse por vagas e candidatos.

## MER / DER

O modelo entidade-relacionamento foi desenvolvido utilizando o **dbdiagram.io**.

A representação visual da estrutura do banco pode ser adicionada abaixo:

**Modelo Entidade-Relacionamento (MER/DER)**

<img width="1148" height="1263" alt="linketind_bd2" src="https://github.com/user-attachments/assets/8bc91627-5b9b-4601-a5b4-831bd6152c0b" />


## SQL

O arquivo SQL do projeto contém:

* Criação das tabelas;
* Definição das chaves primárias;
* Definição das chaves estrangeiras;
* Restrições de unicidade;
* Relacionamentos entre as entidades;
* Dados iniciais para testes;
* Candidatos fictícios;
* Empresas fictícias;
* Vagas fictícias;
* Competências e formações;
* Registros de curtidas;
* Registros de matches.

O script foi desenvolvido para **PostgreSQL**.

## Frontend

Entre no diretório do frontend:

bash
cd frontend


Instale as dependências:

bash
npm install

Compile o projeto utilizando o processo configurado com Webpack/Gulp.

O resultado compilado será disponibilizado no diretório de saída configurado pelo projeto.

Depois disso, abra a página inicial do frontend no navegador.

> O frontend não depende do backend para funcionar nesta etapa. Os dados são armazenados no LocalStorage do navegador.

---

# Próximos passos

A próxima etapa do projeto será a integração entre frontend e backend.

Essa integração permitirá substituir o armazenamento local por uma comunicação real com o servidor, utilizando uma API.

A comunicação entre as duas partes será explorada posteriormente no **KIT 2**.

---

# Menu do Backend

A aplicação backend possui um menu executado diretamente pelo terminal.

As opções disponíveis permitem consultar os dados cadastrados e executar funcionalidades da plataforma, incluindo:


1 - Listar candidatos
2 - Listar empresas
3 - Curtir candidato
4 - Curtir empresa
5 - Visualizar matches
0 - Sair


O menu pode variar conforme o perfil que está utilizando a aplicação, apresentando apenas as operações disponíveis para cada tipo de usuário.

---

# Autor

Desenvolvido por **João Pedro Vaz** durante o **Acelera ZG**.
