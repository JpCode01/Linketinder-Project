# Linketinder - ZG-Hero Project

Aplicação backend em **Groovy**, desenvolvida como ZG-Hero Project do **Acelera ZG**. O projeto consiste na implementação via terminal de uma plataforma de contratação inspirada no **LinkedIn** e no **Tinder**, utilizando conceitos de **Programação Orientada a Objetos (POO)** e **Estruturas de Dados**.

## Sobre o projeto

O **Linketinder** tem como objetivo aproximar empresas e candidatos com base em suas competências (*skills*).

A plataforma permite que **empresas e candidatos demonstrem interesse uns nos outros através de curtidas**. Quando uma empresa e um candidato demonstram interesse mútuo, o sistema identifica um **match** entre os dois.

A aplicação funciona como um **MVP executado pelo terminal**, tendo como foco a aplicação prática de conceitos de POO, estruturas de dados, regras de negócio e organização de código.

## Funcionalidades

O sistema possui:

* Cadastro de candidatos pré-definidos;
* Cadastro de empresas pré-definidas;
* Listagem de todos os candidatos;
* Listagem de todas as empresas;
* Armazenamento das competências dos candidatos;
* Armazenamento das competências esperadas pelas empresas;
* Curtidas entre candidatos e empresas;
* Exibição do *match* após a correspondência entre candidato e empresa;
* Menu de interação pelo terminal.

O projeto possui, inicialmente, no mínimo **5 candidatos e 5 empresas pré-cadastrados**.

## Curtidas e Match

Uma das principais funcionalidades do sistema é o mecanismo de **curtidas e match**, inspirado na dinâmica do Tinder.

### Curtidas

Empresas e candidatos podem demonstrar interesse por meio de uma curtida.

As curtidas são armazenadas para que o sistema consiga verificar posteriormente se existe interesse mútuo entre as partes.

Antes de ocorrer um *match*, as informações que devem permanecer privadas entre candidato e empresa não são reveladas.

### Match

Um **match** ocorre quando existe uma curtida mútua entre uma empresa e um candidato.

O fluxo pode ser representado da seguinte forma:

Empresa curte Candidato
          +
Candidato curte Empresa
        =
      MATCH.

Interesse mútuo identificado


O *match* é representado por uma entidade própria, relacionando o candidato e a empresa envolvidos na correspondência.

## Dados dos candidatos

Cada candidato possui:

* Nome;
* E-mail;
* CPF;
* Idade;
* Estado;
* CEP;
* Descrição pessoal;
* Competências (*skills*);
* Curtidas realizadas.

As competências são representadas por valores previamente definidos, como:

* Python;
* Java;
* Spring Framework;
* Angular;
* Entre outras.

## Dados das empresas

Cada empresa possui:

* Nome;
* E-mail corporativo;
* CNPJ;
* País;
* Estado;
* CEP;
* Descrição da empresa;
* Competências esperadas dos candidatos;
* Curtidas realizadas.

## Conceitos utilizados

O projeto foi desenvolvido com foco na aplicação prática de conceitos fundamentais de programação, principalmente:

### Programação Orientada a Objetos

O sistema utiliza conceitos como:

* Classes e objetos;
* Herança;
* Encapsulamento;
* Polimorfismo;
* Interfaces;
* Classes abstratas;
* Enums;
* Composição e relacionamento entre objetos.

A estrutura do domínio utiliza uma classe Pessoa como base para representar os atributos e comportamentos comuns entre candidatos e empresas, enquanto Candidato e Empresa representam os diferentes tipos de perfis existentes no sistema.

A funcionalidade de *match* também utiliza uma classe específica para representar o relacionamento entre uma empresa e um candidato que demonstraram interesse mútuo.

### Estruturas de dados

As informações dos candidatos, empresas, competências e curtidas são armazenadas utilizando estruturas de dados adequadas à proposta do MVP, principalmente:

* Listas;
* Arrays;
* Estruturas de associação entre objetos.

Essas estruturas permitem armazenar os interesses dos usuários e realizar as verificações necessárias para identificar *matches*.

## Organização do projeto

O projeto está organizado em pacotes para separar as responsabilidades:

* model - contém as classes responsáveis pela representação dos candidatos, empresas, pessoas, vagas e *matches*;
* enums - contém as enumerações utilizadas pelo sistema, como as competências;
* service - contém as regras e operações relacionadas aos candidatos, empresas, vagas, curtidas e *matches*;
* validation - contém as validações utilizadas pela aplicação;
* view - responsável pela interação com o usuário através do terminal;
* Main - ponto de entrada da aplicação.

## Menu

A aplicação possui um menu executado diretamente pelo terminal.

As opções disponíveis permitem consultar os dados cadastrados e executar as funcionalidades da plataforma, incluindo:

1 - Listar candidatos
2 - Listar empresas
3 - Curtir candidato
4 - Curtir empresa
5 - Visualizar matches
0 - Sair


O menu pode variar conforme o perfil que está utilizando a aplicação, apresentando apenas as operações disponíveis para cada tipo de usuário.
