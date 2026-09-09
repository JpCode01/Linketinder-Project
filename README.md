# Linketinder - ZG-Hero Project

Aplicação **full stack em desenvolvimento**, criada como parte do **Acelera ZG**, composta por um backend desenvolvido em **Groovy** e um frontend desenvolvido em **TypeScript**.

O projeto consiste na implementação de uma plataforma de contratação inspirada no **LinkedIn** e no **Tinder**, aproximando empresas e candidatos com base em suas competências (*skills*) e interesses.

A primeira versão do projeto foi desenvolvida como um **MVP executado pelo terminal**, com foco em Programação Orientada a Objetos, estruturas de dados e regras de negócio.

Na segunda etapa, foi desenvolvido um **frontend independente em TypeScript**, adicionando interfaces gráficas para cadastro, visualização de vagas, perfis de candidatos e empresas e visualização de dados.

> **Importante:** nesta etapa o frontend ainda não possui comunicação com o backend. As funcionalidades do frontend são executadas de forma independente utilizando o **LocalStorage do navegador**. A integração entre frontend e backend será abordada posteriormente no **KIT 2**.

---

## Sobre o projeto

O **Linketinder** tem como objetivo aproximar empresas e candidatos com base em suas competências (*skills*).

A plataforma permite que **empresas e candidatos demonstrem interesse uns nos outros através de curtidas**. Quando uma empresa e um candidato demonstram interesse mútuo, o sistema identifica um **match** entre os dois.

O projeto atualmente possui duas partes principais:

* **Backend:** MVP desenvolvido em Groovy e executado pelo terminal;
* **Frontend:** interface web desenvolvida em TypeScript.

O frontend possui armazenamento próprio utilizando o **LocalStorage**, permitindo executar as funcionalidades da interface sem depender do servidor.

---

# Backend

## Tecnologias utilizadas

O backend foi desenvolvido utilizando:

* **Groovy**
* Programação Orientada a Objetos
* Estruturas de Dados
* Spock para testes
* JaCoCo
* PIT / Mutation Testing
* Git e GitHub

## Funcionalidades

O backend possui:

* Cadastro de candidatos pré-definidos;
* Cadastro de empresas pré-definidas;
* Listagem de todos os candidatos;
* Listagem de todas as empresas;
* Armazenamento das competências dos candidatos;
* Armazenamento das competências esperadas pelas empresas;
* Curtidas entre candidatos e empresas;
* Identificação de *matches*;
* Menu de interação pelo terminal.

O projeto possui, inicialmente, no mínimo **5 candidatos e 5 empresas pré-cadastrados**.

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

O **Vite não é utilizado**, mantendo a implementação alinhada às tecnologias abordadas durante o treinamento.

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
