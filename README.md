# Linketinder - ZG-Hero Project

Aplicação backend em **Groovy**, desenvolvida como ZG-Hero Project do **Acelera ZG**. O projeto consiste na implementação via terminal de uma plataforma de contratação inspirada no **LinkedIn** e no **Tinder**, utilizando conceitos de **Programação Orientada a Objetos (POO)** e **Estruturas de Dados**.

## Sobre o projeto

O **Linketinder** tem como objetivo aproximar empresas e candidatos com base em suas competências (*skills*).

A proposta do sistema é permitir que empresas informem as competências que procuram e que candidatos apresentem suas próprias competências, criando uma base que futuramente poderá ser utilizada para implementar funcionalidades de filtragem e *match* entre empresas e candidatos.

Nesta primeira versão, o projeto funciona como um **MVP executado pelo terminal**, tendo como foco principal a implementação das estruturas fundamentais do sistema e a validação dos dados cadastrados.

## Funcionalidades

O sistema possui:

- Cadastro de candidatos pré-definidos;
- Cadastro de empresas pré-definidas;
- Listagem de todos os candidatos;
- Listagem de todas as empresas;
- Armazenamento das competências dos candidatos;
- Armazenamento das competências esperadas pelas empresas;
- Menu de interação pelo terminal.

O projeto possui, inicialmente, no mínimo **5 candidatos e 5 empresas pré-cadastrados**.

## Dados dos candidatos

Cada candidato possui:

- Nome;
- E-mail;
- CPF;
- Idade;
- Estado;
- CEP;
- Descrição pessoal;
- Competências (*skills*).

As competências são representadas por valores previamente definidos, como:

- Python;
- Java;
- Spring Framework;
- Angular;
- Entre outras.

## Dados das empresas

Cada empresa possui:

- Nome;
- E-mail corporativo;
- CNPJ;
- País;
- Estado;
- CEP;
- Descrição da empresa;
- Competências esperadas dos candidatos.

## Conceitos utilizados

O projeto foi desenvolvido com foco na aplicação prática de conceitos fundamentais de programação, principalmente:

### Programação Orientada a Objetos

O sistema utiliza conceitos como:

- Classes e objetos;
- Herança;
- Encapsulamento;
- Polimorfismo;
- Interfaces;
- Classes abstratas;
- Enums.

A estrutura do domínio utiliza uma classe `Pessoa` como base para representar os atributos e comportamentos comuns entre candidatos e empresas, enquanto `Candidato` e `Empresa` representam os diferentes tipos de perfis existentes no sistema.

### Estruturas de dados

As informações dos candidatos, empresas e competências são armazenadas utilizando estruturas de dados adequadas à proposta do MVP, principalmente listas e arrays.

## Organização do projeto

O projeto está organizado em pacotes para separar as responsabilidades:

- `model` - contém as classes responsáveis pela representação dos candidatos, empresas e pessoas;
- `enums` - contém as enumerações utilizadas pelo sistema, como as competências;
- `service` - contém as regras e operações relacionadas aos candidatos e empresas;
- `validation` - contém as validações utilizadas pela aplicação;
- `view` - responsável pela interação com o usuário através do terminal;
- `Main` - ponto de entrada da aplicação.

## Menu

A aplicação possui um menu executado diretamente pelo terminal.

As opções disponíveis permitem consultar os dados cadastrados, incluindo:

```text
1 - Listar candidatos
2 - Listar empresas
0 - Sair
