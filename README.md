# Linketinder - ZG-Hero Project

Aplicação **full stack em desenvolvimento**, criada como parte do **Acelera ZG**, composta por um backend desenvolvido em **Groovy** e um frontend desenvolvido em **TypeScript**.

O projeto consiste na implementação de uma plataforma de contratação inspirada no **LinkedIn** e no **Tinder**, aproximando empresas e candidatos com base em suas competências (*skills*) e interesses.

## Sobre o projeto

A plataforma permite que **empresas e candidatos demonstrem interesse uns nos outros através de curtidas**. Quando uma empresa e um candidato demonstram interesse mútuo, o sistema identifica um **match** entre os dois.

O desenvolvimento aconteceu em etapas:

1. **MVP executado pelo terminal** (backend em Groovy), com foco em Programação Orientada a Objetos, estruturas de dados e regras de negócio;
2. **Frontend independente em TypeScript**, com interfaces gráficas para cadastro, visualização de vagas, perfis de candidatos e empresas e visualização de dados;
3. **Evolução arquitetural do backend**, incorporando banco de dados **PostgreSQL**, persistência através de **JDBC**, camada de **DAO**, DTOs para transferência de dados e testes unitários com **Spock**.

> **Importante:** nesta etapa o frontend ainda não possui comunicação com o backend. Suas funcionalidades são executadas de forma independente, utilizando o **LocalStorage do navegador**. A integração entre as duas partes será abordada posteriormente no **KIT 2**.

---

# Backend

## Tecnologias utilizadas

* **Groovy**
* Programação Orientada a Objetos
* Estruturas de Dados
* **PostgreSQL**
* **JDBC** (puro, sem frameworks ORM como JPA ou Hibernate)
* **Spock**
* **JaCoCo**
* **PIT / Mutation Testing**
* Git e GitHub

## Arquitetura

O backend utiliza separação de responsabilidades entre camadas:

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

* `model` — classes que representam candidatos, empresas, pessoas, vagas, referências, formações e matches;
* `dto` — objetos utilizados para transferência e apresentação de dados;
* `dao` — acesso e persistência dos dados no PostgreSQL;
* `service` — regras de negócio e orquestração da aplicação;
* `validation` — validações utilizadas pela aplicação;
* `database` — gerenciamento das conexões com o PostgreSQL;
* `view` — interação com o usuário através do terminal;
* `Main` — ponto de entrada da aplicação.

A aplicação separa **regra de negócio e persistência**, o que permite testar os Services isoladamente através de mocks dos DAOs.

## Estrutura de pacotes

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

## Persistência com PostgreSQL

Uma das principais evoluções do backend foi a substituição do armazenamento exclusivamente em memória por um banco de dados relacional, que armazena candidatos, empresas, vagas, competências, formações, instituições de formação, curtidas, matches, países e estados.

A comunicação com o banco é feita via **JDBC**, utilizando `PreparedStatement`, `ResultSet`, transações e chaves geradas pelo banco.

### ConnectionFactory

As conexões são centralizadas na classe `database/ConnectionFactory`, que as fornece aos DAOs. A camada de serviço não executa consultas SQL diretamente:

```text
Service
   ↓
DAO
   ↓
ConnectionFactory
   ↓
PostgreSQL
```

### DAO

A camada DAO concentra as operações de persistência. Entre os principais DAOs estão:

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

Eles executam `INSERT`, `SELECT`, `UPDATE` e `DELETE`, associações entre entidades, consultas envolvendo múltiplas tabelas e a conversão dos resultados do banco para objetos do domínio ou DTOs.

### Services

A camada de Service concentra a orquestração das operações da aplicação. Os Services recebem os DAOs necessários através do construtor.

* `CandidatoService`
* `EmpresaService`
* `VagaService`
* `CompetenciaService`
* `ReferenciaService`
* `MatchService`

Exemplo — fluxo de cadastro de um candidato:

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

### DTOs

Os DTOs retornam somente as informações necessárias para cada operação e mantêm separadas as entidades de domínio das estruturas usadas em consulta e apresentação:

* `CandidatoMatchDTO`
* `CandidatoAnonimoDTO`
* `EmpresaMatchDTO`
* `VagaEmpresaDTO`
* `VagaAnonimaDTO`
* `VagaMatchDTO`
* `MatchEncontradoDTO`
* `RemoverCompetenciaDTO`

## Funcionalidades

* Cadastro, listagem e busca por ID de candidatos, empresas e vagas;
* Listagem de vagas por empresa e de todas as vagas;
* Atualização de candidatos, empresas e vagas;
* Login de candidatos e de empresas;
* Desativação lógica de candidatos e empresas;
* Exclusão de vagas;
* Armazenamento, adição e remoção das competências de candidatos e vagas;
* Curtidas entre candidatos e vagas e entre empresas e candidatos;
* Identificação e armazenamento de *matches*;
* Consultas de candidatos interessados em vagas, candidatos curtidos por empresas e vagas curtidas por candidatos;
* Menu de interação pelo terminal.

O projeto possui, inicialmente, no mínimo **5 candidatos e 5 empresas pré-cadastrados**.

### Menu do terminal

```text
1 - Listar candidatos
2 - Listar empresas
3 - Curtir candidato
4 - Curtir empresa
5 - Visualizar matches
0 - Sair
```

O menu pode variar conforme o perfil que está utilizando a aplicação, apresentando apenas as operações disponíveis para cada tipo de usuário.

## Regras de negócio

### Competências

As competências são dados persistidos na tabela `competencias` (ID e nome) e representadas pela classe `Competencia`. Diferentemente da primeira versão, não dependem exclusivamente de um enum.

As associações com candidatos e vagas são feitas pelas tabelas intermediárias `candidatos_competencias` e `vagas_competencias`. O sistema consulta o ID da competência no banco antes de criar a associação.

### Cadastro de candidatos e empresas

O cadastro é feito pelo `CandidatoService` e pelo `EmpresaService`. Primeiro, país e estado informados são convertidos em seus respectivos IDs através dos DAOs de referência. Depois, o registro é salvo: o banco gera o ID e o DAO o recupera com `RETURN_GENERATED_KEYS`, atribuindo-o ao objeto salvo.

No caso do candidato, as competências informadas são associadas em seguida pela tabela de relacionamento.

### Cadastro de vagas

Uma vaga possui nome, descrição, local e empresa responsável. Após a criação, suas competências são associadas pela tabela `vagas_competencias`:

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

### Curtidas

* **Candidato → Vaga:** tabela `vagas_curtidas_candidato`, registra o interesse de um candidato em uma vaga;
* **Empresa → Candidato:** tabela `candidatos_curtidos_empresa`, registra o interesse de uma empresa em um candidato.

### Matches

Quando há interesse mútuo, o relacionamento é registrado na tabela `match`, com ID, ID do candidato, ID da empresa e ID da vaga.

O `MatchDAO` possui consultas para visualizar matches por empresa e por candidato, e os resultados podem ser convertidos para `MatchEncontradoDTO`, contendo informações do candidato, empresa, vaga e competências relacionadas.

### Anonimato

O backend utiliza DTOs específicos (`CandidatoAnonimoDTO` e `VagaAnonimaDTO`) para retornar somente as informações necessárias antes de um relacionamento que permita a identificação completa.

### Exclusão e desativação

Candidatos e empresas utilizam **exclusão lógica**: em vez de remover o registro, o campo `ativo` é alterado para `false`, preservando os dados históricos.

As vagas, por outro lado, podem ser removidas fisicamente. Nesse caso, os relacionamentos que dependem diretamente da vaga são removidos automaticamente por `ON DELETE CASCADE`:

* `vagas_competencias`;
* `vagas_curtidas_candidato`;
* `match`.

A exclusão de uma empresa **não remove automaticamente suas vagas**.

### Regras de unicidade

As tabelas de relacionamento possuem restrições para impedir duplicações:

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

As operações de associação também utilizam `ON CONFLICT DO NOTHING` quando apropriado, evitando erros ao tentar inserir uma relação já existente.

## Testes

O backend possui testes unitários com **Spock Framework**. Os testes dos Services verificam exclusivamente a lógica de negócio e a comunicação entre Service e DAO, e **não acessam o PostgreSQL**: os DAOs são substituídos por mocks.

```groovy
EmpresaDAO empresaDAO = Mock()
```

```text
EmpresaService
      ↓
EmpresaDAO Mock
```

Os testes dos DAOs são responsáveis pela camada de persistência, enquanto os testes dos Services verificam a lógica e a orquestração. Essa separação ajuda a identificar se um problema está na regra de negócio ou na persistência.

Services testados: `CandidatoService`, `EmpresaService`, `VagaService`, `CompetenciaService`, `ReferenciaService` e `MatchService`.

Operações verificadas: cadastro, login, atualização, desativação, exclusão, busca, listagem, associação e remoção de competências, curtidas, busca de candidatos interessados, de vagas curtidas e de matches.

Os mocks permitem verificar se as operações esperadas foram chamadas:

```groovy
1 * empresaDAO.desativar(1L)
```

Também são utilizados testes parametrizados com `@Unroll` quando uma mesma regra precisa ser validada com diferentes entradas — por exemplo, login inválido com e-mail vazio, senha vazia ou ambos vazios.

## Como implementei uma API REST sem framework

Para implementar a API REST sem utilizar frameworks como Spring Boot, utilizei Groovy, Jakarta Servlet e Tomcat embarcado. A ideia foi entender como funciona o processamento de requisições HTTP sem depender das abstrações de um framework.

### 1. Dependências

No `build.gradle`, adicionei as seguintes dependências:

```groovy
implementation 'org.apache.groovy:groovy-json:5.0.8'
compileOnly 'jakarta.servlet:jakarta.servlet-api:6.1.0'
implementation 'org.apache.tomcat.embed:tomcat-embed-core:11.0.26'
```

Utilizei o `groovy-json` para converter JSON em estruturas manipuláveis e gerar respostas JSON. O `jakarta.servlet-api` fornece as classes necessárias para trabalhar com requisições e respostas HTTP, enquanto o Tomcat embarcado permite executar o servidor dentro da própria aplicação.

### 2. Organização do código

Separei as responsabilidades em camadas: as Servlets recebem as requisições HTTP, os Controllers encaminham as operações, os Services executam as regras de negócio e os DAOs acessam o banco de dados.

Essa organização evita concentrar toda a lógica em uma única classe e permite reutilizar a estrutura que já existia no projeto.

### 3. Recebendo as requisições

Utilizei a API Servlet para tratar os métodos HTTP. Por exemplo, `doGet()` processa consultas e `doPost()` recebe solicitações de cadastro.

```groovy
@Override
protected void doGet(
    HttpServletRequest request,
    HttpServletResponse response
) {
    // Encaminha a consulta ao Controller.
}
```

Também configurei os caminhos dos endpoints e o registro das Servlets no Tomcat para direcionar cada requisição ao componente correspondente.

### 4. Processando JSON e respondendo ao cliente

Como não utilizei um framework que realizasse a conversão automaticamente, utilizei `JsonSlurper` para interpretar o JSON recebido e `JsonOutput` para gerar o JSON de resposta.

```groovy
def dados = new JsonSlurper().parse(request.inputStream)
```

Após encaminhar os dados ao Controller e executar a operação, configurei o status HTTP e o conteúdo da resposta:

```groovy
response.setContentType("application/json")
response.setCharacterEncoding("UTF-8")
response.setStatus(HttpServletResponse.SC_CREATED)
response.writer.write(JsonOutput.toJson(resultado))
```

O código `201 Created`, por exemplo, indica que um recurso foi criado com sucesso. Para outras situações, utilizei os status HTTP correspondentes ao resultado da operação.

### 5. Testes

Por fim, testei os endpoints com requisições HTTP, verificando os códigos de resposta, o conteúdo JSON e a persistência dos dados no banco.

Com essa implementação, consegui construir uma API REST sem depender de um framework web de alto nível, compreendendo melhor como o servidor HTTP, as Servlets e as camadas da aplicação trabalham em conjunto.

## Qualidade e Mutation Testing

O projeto utiliza **JaCoCo** para acompanhamento da cobertura dos testes e **PIT Mutation Testing** para verificar sua efetividade. O Mutation Testing modifica temporariamente partes do código para checar se os testes detectam as alterações. Durante o desenvolvimento foram realizados exercícios de eliminação de mutantes, buscando aumentar a quantidade de mutantes detectados.

---

# Frontend

Interface web para o Linketinder desenvolvida em **TypeScript**, que transforma as funcionalidades principais do MVP em uma interface gráfica, permitindo que candidatos e empresas interajam com a plataforma pelo navegador.

O frontend possui armazenamento próprio no **LocalStorage**, sem depender do servidor. A arquitetura é baseada em **Models, Repositories, Services e Pages**, com configurações centralizadas para a criação de repositórios e serviços.

## Tecnologias utilizadas

* **HTML5**
* **CSS3**
* **TypeScript 5.9.3**
* **Webpack** + **Gulp** (compilação e organização dos arquivos TypeScript)
* **ts-loader**
* **LocalStorage**
* **Chart.js** para visualização de dados

## Estrutura

### Models

Classes que representam os objetos do sistema: `Pessoa`, `Candidato`, `Empresa`, `Vaga` e `Competencia`.

### Repositories

Acesso e persistência dos dados no **LocalStorage**, separados das regras de negócio:

* `CandidatoRepository`
* `EmpresaRepository`
* `VagaRepository`
* `CurtidaVagaRepository`
* `CurtidaCandidatoRepository`

### Services

Regras de negócio e orquestração das operações, utilizando os repositórios para acessar os dados:

* `CandidatoService`
* `EmpresaService`
* `VagaService`
* `CurtidaVagaService`
* `CurtidaCandidatoService`

Os serviços validam operações como a existência de candidatos, empresas e vagas e impedem operações duplicadas, lançando exceções específicas quando necessário.

### Configuração de dependências

`RepositoryConfig` e `ServiceConfig` centralizam a criação dos repositórios e serviços e a injeção manual de suas dependências através dos construtores.

### Pages

Lógica responsável por preencher e manipular as páginas HTML: `LoginPage`, `CadastroPage`, `CandidatoPage` e `EmpresaPage`.

### Demais diretórios

* `charts` — visualizações gráficas com Chart.js;
* `data` — dados iniciais;
* `app` — inicialização e controle da aplicação;
* `html` — páginas da aplicação;
* `css` — estilos da interface;
* `ts` — código TypeScript.

## Funcionalidades

### Cadastro de candidatos

Tela específica para cadastro de candidatos, com: nome, e-mail, CPF, idade, estado, CEP, formação, descrição e competências.

### Cadastro de empresas

Tela específica para cadastro de empresas, com: nome, e-mail, CNPJ, país, estado, CEP e descrição.

Os dados de candidatos e empresas são armazenados no **LocalStorage** do navegador.

### Login

A tela inicial permite selecionar o tipo de usuário e localizar um candidato ou empresa cadastrado, com base nos dados do LocalStorage. Após a identificação, o usuário é direcionado ao seu perfil.

### Perfil do candidato

O candidato pode:

* Visualizar todas as vagas disponíveis, com título, descrição, tipo, localização e competências associadas;
* Curtir uma vaga;
* Visualizar quais vagas já foram curtidas (armazenadas no perfil do candidato no LocalStorage).

### Perfil da empresa

A página da empresa funciona como um dashboard e apresenta:

* Dados da empresa;
* Formulário para criação de vagas;
* Lista de vagas cadastradas pela empresa;
* Quantidade de candidatos interessados em cada vaga;
* Lista de candidatos que demonstraram interesse em cada vaga, com suas competências e formação.

### Cadastro de vagas

Empresas cadastram vagas diretamente pelo dashboard, informando nome, tipo, localização, descrição e competências exigidas. As competências são validadas antes da criação. Após o cadastro, a vaga é armazenada no LocalStorage e associada à empresa responsável.

### Curtidas

**Candidato:** ao curtir uma vaga,

* a vaga é adicionada à lista de vagas curtidas do candidato;
* a vaga passa a registrar o candidato interessado;
* o botão é alterado visualmente para indicar que a vaga já foi curtida.

**Empresa:** a empresa demonstra interesse em um candidato **para uma vaga específica**. Ao curtir,

* a curtida registra o ID da empresa, o ID do candidato e o ID da vaga;
* o botão daquela combinação de candidato e vaga indica que ele já foi curtido para essa oportunidade;
* o mesmo candidato pode ser considerado para outra vaga da mesma empresa de forma independente.

### Matches

O frontend verifica o interesse mútuo por **vaga específica**: para uma combinação ser exibida como match, o candidato precisa ter curtido a vaga e a empresa precisa ter curtido aquele candidato para a mesma vaga. Assim, a curtida da empresa em uma vaga não gera match automático nas outras vagas da mesma empresa que o candidato tenha curtido.

### Anonimato

O frontend respeita a regra de anonimato definida no desafio: antes de existir um *match*, as informações de identificação permanecem restritas.

* **Visão da empresa:** os candidatos são apresentados com formação, competências, vaga de interesse e descrição;
* **Visão do candidato:** as vagas apresentam as informações necessárias para avaliar a oportunidade.

### Gráfico de competências

O projeto utiliza **Chart.js** para a visualização de dados. O gráfico de competências da empresa está em processo de refatoração e ainda não está integrado à nova `EmpresaPage`.

A refatoração planejada separa as responsabilidades em dois arquivos:

* `services/RelatorioCompetenciasService.ts` — consulta as vagas da empresa, recupera as curtidas de cada vaga, busca os candidatos e contabiliza suas competências;
* `charts/CompetenciaCharts.ts` — recebe as quantidades calculadas e renderiza o gráfico de barras.

A contagem proposta mantém o comportamento legado: as competências de um candidato são contabilizadas novamente quando ele curte outra vaga da mesma empresa. A implementação só deve ser considerada concluída depois que os arquivos forem criados e a chamada for integrada à `EmpresaPage`.

## Armazenamento no LocalStorage

Como o frontend ainda não se comunica com o backend, os dados são armazenados temporariamente no **LocalStorage** do navegador, nas chaves:

* `candidatos`;
* `empresas`;
* `vagas`;
* `curtidasVagas`;
* `curtidasCandidatos`;
* `sessao`, contendo o identificador e o tipo de usuário autenticado.

> Estes nomes correspondem às chaves descritas nesta documentação; se forem alterados no código, esta seção também deve ser atualizada.

Fluxo utilizado:

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

A conversão para instâncias das classes do domínio permite que o frontend continue usando os métodos e comportamentos definidos nos modelos, mesmo depois que os dados são recuperados do LocalStorage.

## Conceitos utilizados

Além dos conceitos do backend, o frontend utiliza:

* TypeScript e tipagem estática;
* Classes, interfaces, herança e encapsulamento;
* Enums e Generics;
* Manipulação do DOM e eventos;
* Arrow Functions;
* Array methods, como `map`, `find` e `some`;
* JSON e LocalStorage;
* Modularização através de `import` e `export`;
* Separação de responsabilidades;
* Compilação de TypeScript para JavaScript.

---

# Banco de Dados

O projeto utiliza um banco relacional em **PostgreSQL** (acessado via **JDBC**) para armazenar e relacionar os dados de candidatos, empresas, vagas, competências, formações e interações entre candidatos e empresas. O modelo foi desenvolvido no **dbdiagram.io**, seguindo as regras de negócio do Linketinder.

## Tabelas

### Candidatos

Dados pessoais e de cadastro:

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

Um candidato pode possuir diversas competências, formações e curtidas em vagas. A idade não é armazenada diretamente: é calculada a partir da data de nascimento nas consultas.

### Competências

Competências disponíveis no sistema, compartilhadas entre candidatos e vagas (por exemplo: Java, Python, Groovy, Angular, Spring), o que permite identificar as habilidades que um candidato possui e as exigidas por uma vaga.

### Candidatos_Competencias

Relacionamento **N:N** entre candidatos e competências.

### Formação

Formações acadêmicas ou técnicas dos candidatos:

* Curso;
* Candidato;
* Instituição;
* Data de início;
* Data de término ou previsão de término.

### Candidato_Formacoes

Relaciona os candidatos às suas formações.

### Instituicao_Formacao

Instituições de ensino relacionadas às formações.

### País e Estados

Países e estados disponíveis para associação a candidatos e empresas.

### Empresas

Dados das empresas cadastradas:

* Nome;
* CNPJ;
* E-mail corporativo;
* Senha;
* Descrição;
* País;
* Estado;
* CEP;
* Status de atividade.

Uma empresa pode cadastrar diversas vagas e curtir candidatos.

### Vagas

Vagas cadastradas pelas empresas, com nome, descrição, local e empresa responsável. Cada vaga pertence a uma única empresa (relacionamento **1:N**).

### Vagas_Competencias

Relacionamento **N:N** entre vagas e competências.

### Vagas_Curtidas_Candidato

Vagas curtidas pelos candidatos. Cada registro representa o interesse de um candidato em uma vaga.

### Candidatos_Curtidos_Empresa

Candidatos curtidos pelas empresas. Cada registro representa o interesse de uma empresa em um candidato.

### Match

Matches entre candidato, empresa e vaga. Um match ocorre quando há interesse de ambas as partes na mesma oportunidade.

## Lógica aplicada

Um candidato é cadastrado e pode adicionar competências e formações. As empresas também são cadastradas e criam vagas, cada uma com suas competências exigidas. A partir daí:

1. O candidato visualiza as vagas disponíveis;
2. O candidato pode curtir uma vaga de seu interesse;
3. A empresa pode visualizar candidatos de acordo com as regras de recrutamento do sistema e demonstrar interesse em um candidato;
4. As interações são armazenadas nas respectivas tabelas de curtidas;
5. Quando há interesse de ambas as partes para uma determinada oportunidade, é registrado um **match**;
6. O match relaciona o candidato, a empresa e a vaga correspondente.

Essa estrutura mantém separadas as informações de candidatos, empresas, vagas, competências e interações, evitando armazenar essas relações dentro das entidades principais.

## Relacionamentos principais

* **País → Candidatos:** 1:N;
* **País → Empresas:** 1:N;
* **Estado → Candidatos:** 1:N;
* **Estado → Empresas:** 1:N;
* **Candidatos → Formações:** relacionamento através das tabelas de formação;
* **Instituição → Formações:** 1:N;
* **Candidatos ↔ Competências:** N:N;
* **Empresa → Vagas:** 1:N;
* **Vagas ↔ Competências:** N:N;
* **Candidatos ↔ Vagas:** através das curtidas;
* **Empresas ↔ Candidatos:** através das curtidas;
* **Candidato + Empresa + Vaga → Match:** representa o interesse mútuo.

As tabelas `candidatos_competencias` e `vagas_competencias` são as intermediárias dos relacionamentos muitos-para-muitos.

## Integridade e restrições

O banco utiliza chaves primárias, chaves estrangeiras, restrições `UNIQUE`, `NOT NULL`, valores padrão e `ON DELETE CASCADE` em relacionamentos dependentes de vagas. Os IDs das entidades principais são gerados pelo PostgreSQL. Os detalhes das restrições de unicidade estão na seção [Regras de unicidade](#regras-de-unicidade).

## MER / DER

O modelo entidade-relacionamento foi desenvolvido no **dbdiagram.io**.

**Modelo Entidade-Relacionamento (MER/DER)**

<img width="1148" height="1263" alt="linketinder_bd" src="https://github.com/user-attachments/assets/51feb32e-5ac6-4395-8134-0a08f6282f30" />

## SQL

O script SQL do projeto, desenvolvido para **PostgreSQL**, contém:

* Criação das tabelas;
* Chaves primárias e estrangeiras;
* Restrições de unicidade;
* Valores padrão;
* Regras de `ON DELETE CASCADE`;
* Dados iniciais para testes: competências, países e estados, e candidatos, empresas, vagas, formações, curtidas e matches fictícios.

---

# Execução

## Backend

Execute o projeto utilizando a configuração correspondente ao backend. A aplicação é iniciada pelo ponto de entrada `Main` e utilizada pelo terminal.

Para executar com persistência, é necessário ter uma instância do **PostgreSQL** disponível e configurar os dados de conexão utilizados pela `ConnectionFactory`.



## Frontend

Entre no diretório do frontend:

```bash
cd frontend
```

Instale as dependências:

```bash
npm install
```

Compile o projeto utilizando o processo configurado com Webpack/Gulp. O resultado será disponibilizado no diretório de saída configurado pelo projeto. Depois, abra a página inicial do frontend no navegador.

> O frontend não depende do backend para funcionar nesta etapa. Os dados são armazenados no LocalStorage do navegador.

---


# Autor

Desenvolvido por **João Pedro Vaz** durante o **Acelera ZG**.
