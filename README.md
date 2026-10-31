# Linketinder - ZG-Hero Project

Aplicação **full stack em desenvolvimento**, criada como parte do **Acelera ZG**, composta por um backend desenvolvido em **Groovy** e um frontend desenvolvido em **TypeScript**, que se comunicam por meio de uma **API REST**.

O projeto consiste na implementação de uma plataforma de contratação inspirada no **LinkedIn** e no **Tinder**, aproximando empresas e candidatos com base em suas competências (*skills*) e interesses.

## Sobre o projeto

A plataforma permite que **empresas e candidatos demonstrem interesse uns nos outros através de curtidas**. Quando uma empresa e um candidato demonstram interesse mútuo, o sistema identifica um **match** entre os dois.

O desenvolvimento aconteceu em etapas:

1. **MVP executado pelo terminal** (backend em Groovy), com foco em Programação Orientada a Objetos, estruturas de dados e regras de negócio;
2. **Frontend em TypeScript**, com interfaces gráficas para cadastro, login, visualização de vagas, perfis de candidatos e empresas e visualização de dados;
3. **Evolução arquitetural do backend**, incorporando banco de dados **PostgreSQL**, persistência através de **JDBC**, camada de **DAO**, DTOs para transferência de dados e testes unitários com **Spock**;
4. **Integração entre frontend e backend**, com uma **API REST implementada sem framework** (Groovy, Jakarta Servlet e Tomcat embarcado), consumida pelo frontend através da `fetch` API.

> **Importante:** o frontend agora se comunica com o backend. Os dados de candidatos, empresas, vagas, curtidas e matches são lidos e gravados no **PostgreSQL** através da API, que deve estar em execução em `http://localhost:8080`. O navegador guarda localmente (LocalStorage) apenas a **sessão** do usuário logado.

---

# Backend

## Tecnologias utilizadas

* **Groovy**
* Programação Orientada a Objetos
* Estruturas de Dados
* **PostgreSQL**
* **JDBC** (puro, sem frameworks ORM como JPA ou Hibernate)
* **Jakarta Servlet** + **Tomcat embarcado** (API REST sem framework)
* **groovy-json** (`JsonSlurper` e `JsonOutput`)
* **Spock**
* **JaCoCo**
* **PIT / Mutation Testing**
* Git e GitHub

## Arquitetura

O backend utiliza separação de responsabilidades entre camadas. O fluxo de uma requisição HTTP é:

```text
Servlet (api/servlet)
   ↓
Controller
   ↓
Service
   ↓
DAO
   ↓
ConnectionFactory
   ↓
PostgreSQL
```

O menu do terminal (`view`) também utiliza os mesmos Services e DAOs, o que permite reaproveitar as regras de negócio nos dois pontos de entrada.

* `model` — classes que representam candidatos, empresas, pessoas, vagas e referências (países, estados e competências);
* `dto` — objetos utilizados para transferência e apresentação de dados;
* `dao` — acesso e persistência dos dados no PostgreSQL, incluindo as interfaces em `dao/contratos`;
* `service` — regras de negócio e orquestração da aplicação;
* `controller` — recebem as chamadas das Servlets e as encaminham aos Services;
* `facade` — `MatchFacade`, que coordena a curtida de um candidato por uma empresa e o registro do match;
* `api` — `RestServer` (ponto de entrada da API) e as Servlets de cada recurso, além do `CorsFilter`;
* `config` — criação e injeção manual das dependências (`DaoConfig`, `ServiceConfig`, `ControllerConfig`), configuração do Tomcat (`TomcatConfig`) e registro das Servlets (`ApiServletConfig`);
* `database` — gerenciamento das conexões com o PostgreSQL;
* `exception` — exceções específicas do domínio;
* `factory` — criação dos menus;
* `view` — interação com o usuário através do terminal;
* `Main` — ponto de entrada da aplicação via terminal.

A aplicação separa **regra de negócio e persistência**, o que permite testar os Services isoladamente através de mocks dos DAOs.

## Estrutura de pacotes

```text
com.jpcode
├── model
│   ├── core
│   │   ├── Candidato
│   │   ├── Empresa
│   │   ├── Pessoa
│   │   └── Vaga
│   │
│   └── referencia
│       ├── Pais
│       ├── Estado
│       └── Competencia
│
├── dto
│   ├── candidato
│   ├── empresa
│   ├── vaga
│   ├── Match
│   └── competencia
│
├── dao
│   ├── candidato
│   ├── empresa
│   ├── vaga
│   ├── referencia
│   ├── relacionamento
│   ├── match
│   └── contratos
│
├── api
│   ├── RestServer
│   └── servlet
│       └── filter
│
├── controller
│   ├── candidato
│   ├── empresa
│   ├── vaga
│   ├── competencia
│   ├── referencia
│   └── match
│
├── facade
├── config
├── service
├── exception
├── factory
├── database
├── view
└── Main
```

## Persistência com PostgreSQL

Uma das principais evoluções do backend foi a substituição do armazenamento exclusivamente em memória por um banco de dados relacional, que armazena candidatos, empresas, vagas, competências, curtidas, matches, países e estados.

A comunicação com o banco é feita via **JDBC**, utilizando `PreparedStatement`, `ResultSet`, transações e chaves geradas pelo banco.

### ConnectionFactory

As conexões são fornecidas pela interface `database/ConnectionFactory`, implementada por `PostgresConnectionFactory`, que se conecta ao banco `linketinder` em `localhost:5432`. A camada de serviço não executa consultas SQL diretamente:

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

* Candidato: `CadastrarCandidatoDTO`, `AtualizarCandidatoDTO`, `CandidatoViewDTO`, `CandidatoViewListDTO`, `CandidatoComReferenciasDTO`, `CandidatoAnonimoDTO`;
* Empresa: `CadastrarEmpresaDTO`, `AtualizarEmpresaDTO`, `EmpresaViewDTO`, `EmpresaViewListDTO`, `EmpresaComReferenciasDTO`;
* Vaga: `CadastrarVagaDTO`, `AtualizarVagaDTO`, `VagaEmpresaDTO`, `VagaAnonimaDTO`;
* Match: `MatchEncontradoDTO`;
* Competência: `RemoverCompetenciaDTO`.

## Funcionalidades

* Cadastro, listagem e busca por ID de candidatos e empresas, e cadastro, listagem e busca por ID de vagas;
* Listagem de vagas por empresa e de todas as vagas;
* Atualização de candidatos, empresas e vagas;
* Login de candidatos e de empresas;
* Desativação lógica de candidatos e empresas;
* Exclusão de vagas;
* Armazenamento, adição e remoção das competências de candidatos e vagas;
* Curtidas entre candidatos e vagas e entre empresas e candidatos;
* Identificação e armazenamento de *matches*;
* Consultas de candidatos interessados em vagas, candidatos curtidos por empresas e vagas curtidas por candidatos;
* Menu de interação pelo terminal;
* API REST para consumo pelo frontend.

O projeto possui, inicialmente, no mínimo **5 candidatos e 5 empresas pré-cadastrados**.

### Menu do terminal

O menu inicial permite escolher o perfil de acesso:

```text
1 - ENTRAR COMO EMPRESA
2 - ENTRAR COMO CANDIDATO
3 - SAIR
```

Em cada perfil é possível cadastrar-se ou fazer login. Após o login, o menu apresenta as operações disponíveis para cada tipo de usuário:

**Candidato**

```text
1 - Ver vagas curtidas
2 - Ver vagas disponiveis
3 - Curtir Vaga
4 - Desativar Conta
5 - Atualizar competencias
6 - Atualizar conta
7 - Ver matches
8 - Remover Competencia
9 - Sair
```

**Empresa**

```text
1 - Ver Vagas
2 - Curtir Candidatos em Vaga
3 - Criar Vaga
4 - Ver Candidatos Curtidos
5 - Desativar Conta
6 - Apagar vaga
7 - Atualizar conta
8 - Atualizar vaga
9 - Adicionar competencias em Vaga
10 - Ver Matches
11 - Remover competencia
12 - Sair
```

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

O match é registrado na tabela `match`, com ID, ID do candidato, ID da empresa e ID da vaga.

Quando uma empresa curte um candidato para uma vaga, o `MatchFacade` registra a curtida da empresa e salva o match daquele candidato com aquela vaga. O fluxo do frontend garante o interesse mútuo: a empresa só visualiza, para curtir, os candidatos que **já curtiram a vaga** em questão.

O `MatchDAO` possui consultas para visualizar matches por empresa e por candidato, e os resultados são convertidos para `MatchEncontradoDTO`, contendo informações do candidato, empresa, vaga e competências relacionadas.

### Anonimato

O backend utiliza DTOs específicos (`CandidatoAnonimoDTO` e `VagaAnonimaDTO`) para retornar somente as informações necessárias antes de um relacionamento que permita a identificação completa:

* `CandidatoAnonimoDTO`: ID, descrição e competências do candidato;
* `VagaAnonimaDTO`: ID, descrição e competências da vaga.

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

Essa separação ajuda a identificar se um problema está na regra de negócio ou na persistência.

Services testados: `CandidatoService`, `EmpresaService`, `VagaService`, `CompetenciaService`, `ReferenciaService` e `MatchService`.

Operações verificadas: cadastro, login, atualização, desativação, exclusão, busca, listagem, associação e remoção de competências, curtidas, busca de candidatos interessados, de vagas curtidas e de matches.

Os mocks permitem verificar se as operações esperadas foram chamadas:

```groovy
1 * empresaDAO.desativar(1L)
```

Também são utilizados testes parametrizados com `@Unroll` quando uma mesma regra precisa ser validada com diferentes entradas — por exemplo, login inválido com e-mail vazio, senha vazia ou ambos vazios.

## Qualidade e Mutation Testing

O projeto utiliza **JaCoCo** para acompanhamento da cobertura dos testes e **PIT Mutation Testing** para verificar sua efetividade. O Mutation Testing modifica temporariamente partes do código para checar se os testes detectam as alterações. Durante o desenvolvimento foram realizados exercícios de eliminação de mutantes, buscando aumentar a quantidade de mutantes detectados.

---

# API REST

Para implementar a API REST sem utilizar frameworks como Spring Boot, utilizei Groovy, Jakarta Servlet e Tomcat embarcado. A ideia foi entender como funciona o processamento de requisições HTTP sem depender das abstrações de um framework.

O ponto de entrada da API é a classe `com.jpcode.api.RestServer`, que monta as dependências (`DaoConfig` → `ServiceConfig` → `ControllerConfig`), inicia o Tomcat na porta **8080** e registra as Servlets e o filtro de CORS.

## Como foi implementada

### 1. Dependências

No `build.gradle`, foram adicionadas as seguintes dependências:

```groovy
implementation 'org.apache.groovy:groovy-json:5.0.8'
compileOnly 'jakarta.servlet:jakarta.servlet-api:6.1.0'
implementation 'org.apache.tomcat.embed:tomcat-embed-core:11.0.26'
```

Utilizei o `groovy-json` para converter JSON em estruturas manipuláveis e gerar respostas JSON. O `jakarta.servlet-api` fornece as classes necessárias para trabalhar com requisições e respostas HTTP, enquanto o Tomcat embarcado permite executar o servidor dentro da própria aplicação.

### 2. Organização do código

As responsabilidades foram separadas em camadas: as Servlets recebem as requisições HTTP, os Controllers encaminham as operações, os Services executam as regras de negócio e os DAOs acessam o banco de dados.

Essa organização evita concentrar toda a lógica em uma única classe e permite reutilizar a estrutura que já existia no projeto.

### 3. Recebendo as requisições

Utilizei a API Servlet para tratar os métodos HTTP. Por exemplo, `doGet()` processa consultas, `doPost()` recebe solicitações de cadastro e `doPut()` recebe atualizações.

```groovy
@Override
protected void doGet(
    HttpServletRequest request,
    HttpServletResponse response
) {
    // Encaminha a consulta ao Controller.
}
```

Os caminhos dos endpoints e o registro das Servlets no Tomcat são configurados em `ApiServletConfig`, que direciona cada requisição ao componente correspondente.

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

O código `201 Created`, por exemplo, indica que um recurso foi criado com sucesso. Para outras situações, são utilizados os status HTTP correspondentes ao resultado da operação, como `400 Bad Request`, `401 Unauthorized`, `404 Not Found` e `500 Internal Server Error`. As mensagens de erro são devolvidas em JSON no formato `{ "erro": "..." }`.

### 5. CORS

Como o frontend e a API são executados em origens diferentes, a API registra o `CorsFilter`, aplicado a todas as rotas (`/*`). Ele libera a origem `http://127.0.0.1:5500` (porta padrão do Live Server), permite os métodos `GET`, `POST`, `PUT`, `DELETE` e `OPTIONS` e o cabeçalho `Content-Type`, e responde às requisições *preflight* (`OPTIONS`) com `204 No Content`.

### 6. Testes

Por fim, os endpoints foram testados com requisições HTTP, verificando os códigos de resposta, o conteúdo JSON e a persistência dos dados no banco.

## Endpoints

| Método | Rota | Descrição |
| ------ | ---- | --------- |
| `POST` | `/login/candidato` e `/login/empresa` | Login (corpo com `email` e `senha`); retorna `401` para credenciais inválidas |
| `GET` | `/candidatos` | Lista candidatos |
| `GET` | `/candidatos/{id}` | Busca um candidato por ID |
| `POST` | `/candidatos` | Cadastra um candidato |
| `PUT` | `/candidatos/{id}` | Atualiza um candidato |
| `GET` | `/empresas` | Lista empresas |
| `GET` | `/empresas/{id}` | Busca uma empresa por ID |
| `POST` | `/empresas` | Cadastra uma empresa |
| `PUT` | `/empresas/{id}` | Atualiza uma empresa |
| `GET` | `/vagas` | Lista todas as vagas |
| `GET` | `/vagas/{id}` | Busca uma vaga por ID |
| `GET` | `/vagas/empresa/{idEmpresa}` | Lista as vagas de uma empresa |
| `POST` | `/vagas` | Cadastra uma vaga |
| `PUT` | `/vagas/{id}` | Atualiza uma vaga |
| `GET` | `/competencias` | Lista as competências |
| `GET` | `/referencias/paises` | Lista os países |
| `GET` | `/referencias/estados` | Lista os estados |
| `POST` | `/curtidas/vagas` | Candidato curte uma vaga (`idCandidato`, `idVaga`) |
| `POST` | `/curtidas/candidatos` | Empresa curte um candidato para uma vaga (`idEmpresa`, `idCandidato`, `idVaga`) |
| `GET` | `/curtidas/vagas/candidato/{idCandidato}` | Vagas curtidas por um candidato |
| `GET` | `/curtidas/candidatos/vaga/{idVaga}` | Candidatos (anônimos) que curtiram uma vaga |
| `GET` | `/curtidas/candidatos/empresa/{idEmpresa}` | Candidatos curtidos por uma empresa |
| `GET` | `/curtidas/vagas/verificar?candidatoId=&vagaId=` | Verifica se o candidato já curtiu a vaga |
| `GET` | `/curtidas/candidatos/verificar?empresaId=&candidatoId=` | Verifica se a empresa já curtiu o candidato |
| `GET` | `/matches/empresa/{idEmpresa}` | Matches de uma empresa |
| `GET` | `/matches/candidato/{idCandidato}` | Matches de um candidato |

---

# Frontend

Interface web para o Linketinder desenvolvida em **TypeScript**, que transforma as funcionalidades principais do MVP em uma interface gráfica, permitindo que candidatos e empresas interajam com a plataforma pelo navegador.

O frontend **consome a API REST do backend** (`http://localhost:8080`) através da `fetch` API. A arquitetura é baseada em **API Clients, DTOs, Models, Services e Pages**, com a criação dos serviços centralizada em uma classe de configuração.

## Tecnologias utilizadas

* **HTML5**
* **CSS3**
* **TypeScript 5.9.3**
* **Webpack** + **Gulp** (compilação e organização dos arquivos TypeScript)
* **ts-loader**
* **Fetch API** (comunicação com o backend)
* **LocalStorage** (apenas para a sessão do usuário)
* **Chart.js** para visualização de dados

## Estrutura

### API Clients

Classes em `api/clientes` responsáveis por toda a comunicação HTTP com o backend. Cada uma corresponde a um recurso da API e trata os códigos de resposta, convertendo erros em exceções com a mensagem devolvida pelo servidor (campo `erro`):

* `CandidatoApiClient`
* `EmpresaApiClient`
* `VagaApiClient`
* `CompetenciaApiClient`
* `ReferenciaApiClient`
* `CurtidaVagaApiClient`
* `CurtidaCandidatoApiClient`
* `MatchApiClient`

### DTOs da API

Interfaces em `api/dto` que descrevem os dados enviados e recebidos, como `CadastrarCandidatoRequestDTO`, `CadastrarEmpresaRequestDTO`, `CadastrarVagaRequestDTO`, `CandidatoViewDTO`, `EmpresaViewDTO`, `CandidatoAnonimoResponseDTO`, `VagaAnonimaResponseDTO`, `VagaEmpresaResponseDTO` e `MatchResponseDTO`.

### Models

Classes e interfaces que representam os objetos do sistema: `Pessoa`, `Candidato`, `Empresa`, `Vaga`, `Competencia`, `Pais` e `Estado`, além de `CurtidaVaga` e `CurtidaCandidato`.

### Services

Orquestram as operações do frontend, utilizando os API Clients para acessar o backend:

* `CandidatoService`
* `EmpresaService`
* `VagaService`
* `CompetenciaService`
* `ReferenciaService`
* `CurtidaVagaService`
* `CurtidaCandidatoService`
* `MatchService`
* `RelatorioCompetenciasService`

### Configuração de dependências

A classe `ServiceConfig` centraliza a criação dos serviços e a injeção manual dos API Clients através dos construtores.

### Login

O login utiliza o padrão **Strategy**: `LoginCandidatoStrategy` e `LoginEmpresaStrategy` implementam `LoginStrategy` e enviam as credenciais para `POST /login/candidato` ou `POST /login/empresa`.

### Pages

Lógica responsável por preencher e manipular as páginas HTML, organizada por perfil:

* `LoginPage`
* `pages/candidato`: `CadastroCandidatoPage`, `CandidatoPage` e seus componentes (perfil, vagas, vagas curtidas e matches);
* `pages/empresa`: `CadastroEmpresaPage`, `EmpresaPage` e seus componentes (perfil, vagas, candidatos, candidatos curtidos e matches).

### Demais diretórios

* `exceptions` — exceções específicas do frontend;
* `charts` — visualizações gráficas com Chart.js;
* `app` — inicialização e controle da aplicação;
* `repository` e `factory/storage` — código da etapa anterior, baseado em LocalStorage, que **não é mais utilizado** pelos serviços;
* `html` — páginas da aplicação;
* `css` — estilos da interface.

## Funcionalidades

### Cadastro de candidatos

Tela específica para cadastro de candidatos, com: nome, sobrenome, e-mail, senha, CPF, data de nascimento, país, estado, CEP, competências e descrição. A idade é calculada a partir da data de nascimento.

As listas de **países, estados e competências** exibidas no formulário são carregadas da API. Os dados são enviados ao backend (`POST /candidatos`) e persistidos no PostgreSQL. Em caso de erro (por exemplo, dados inválidos), a mensagem retornada pela API é exibida no formulário.

### Cadastro de empresas

Tela específica para cadastro de empresas, com: nome, e-mail, senha, CNPJ, país, estado, CEP e descrição. Os dados são enviados ao backend (`POST /empresas`) e persistidos no PostgreSQL.

### Login

A tela inicial permite selecionar o tipo de usuário e informar e-mail e senha. As credenciais são validadas pelo backend e, em caso de sucesso, o identificador e o tipo do usuário são guardados na sessão e o usuário é direcionado ao seu perfil. Credenciais inválidas resultam em erro de login.

### Perfil do candidato

A página do candidato apresenta seus dados principais, carregados da API, e permite ao candidato:

* Visualizar as vagas disponíveis, apresentadas de forma anônima, com descrição e competências associadas;
* Curtir uma vaga;
* Visualizar quais vagas já foram curtidas;
* Visualizar seus matches, com os dados da empresa e da vaga.

### Perfil da empresa

A página da empresa funciona como um dashboard e apresenta:

* Dados da empresa;
* Formulário para criação de vagas;
* Lista de vagas cadastradas pela empresa, com a quantidade de candidatos interessados em cada uma;
* Lista de candidatos que demonstraram interesse em cada vaga, com suas competências;
* Lista de candidatos já curtidos;
* Matches da empresa, com os dados do candidato, da vaga e as competências de ambos;
* Gráfico de competências dos candidatos interessados.

### Cadastro de vagas

Empresas cadastram vagas diretamente pelo dashboard, informando nome, tipo, localização, descrição e competências exigidas. Os dados são enviados ao backend (`POST /vagas`) e a vaga é persistida no PostgreSQL, associada à empresa responsável. As competências precisam existir no banco: caso contrário, a API retorna erro.

### Curtidas

**Candidato:** ao curtir uma vaga (`POST /curtidas/vagas`),

* a curtida é registrada no banco, associando candidato e vaga;
* a vaga passa a contar o candidato entre os interessados;
* o botão é alterado visualmente para indicar que a vaga já foi curtida, e o estado é verificado na API ao carregar a página.

**Empresa:** a empresa demonstra interesse em um candidato **para uma vaga específica** (`POST /curtidas/candidatos`). Ao curtir,

* a requisição envia o ID da empresa, o ID do candidato e o ID da vaga;
* o botão é alterado visualmente para indicar que o candidato já foi curtido;
* o match daquele candidato com a vaga é registrado.

### Matches

Os matches são **registrados pelo backend** e consultados pelo frontend (`GET /matches/empresa/{id}` e `GET /matches/candidato/{id}`). A empresa só pode curtir candidatos que já curtiram a vaga, o que garante que o match represente interesse mútuo por **vaga específica**.

### Anonimato

O frontend respeita a regra de anonimato definida no desafio: antes de existir um *match*, as informações de identificação permanecem restritas.

* **Visão da empresa:** os candidatos que curtiram uma vaga são apresentados de forma anônima, com descrição, competências e a vaga de interesse;
* **Visão do candidato:** as vagas são apresentadas sem identificação da empresa, com descrição e competências.

### Gráfico de competências

O projeto utiliza **Chart.js** para a visualização de dados. O gráfico de competências, exibido no dashboard da empresa, é um gráfico de barras com a quantidade de vezes que cada competência aparece entre os candidatos interessados nas vagas da empresa.

As responsabilidades são separadas em dois arquivos:

* `services/RelatorioCompetenciasService.ts` — busca as vagas da empresa, recupera os candidatos que curtiram cada vaga pela API e contabiliza suas competências;
* `charts/CompetenciaChart.ts` — recebe as quantidades calculadas e renderiza o gráfico de barras.

A contagem mantém o comportamento legado: as competências de um candidato são contabilizadas novamente quando ele curte outra vaga da mesma empresa.

## Sessão no LocalStorage

Os dados da aplicação ficam no backend. O **LocalStorage** do navegador é utilizado apenas para guardar a sessão, na chave:

* `sessao` — contém o identificador e o tipo (`CANDIDATO` ou `EMPRESA`) do usuário autenticado.

As páginas de perfil leem essa chave para saber qual usuário consultar na API.

## Fluxo de comunicação

```text
Page
  ↓
Service
  ↓
ApiClient
  ↓
fetch (JSON via HTTP)
  ↓
Servlet → Controller → Service → DAO
  ↓
PostgreSQL
```

## Conceitos utilizados

Além dos conceitos do backend, o frontend utiliza:

* TypeScript e tipagem estática;
* Classes, interfaces, herança e encapsulamento;
* Generics;
* Programação assíncrona (`async`/`await` e `Promise`);
* Comunicação HTTP com a `fetch` API e tratamento de erros;
* Padrão Strategy (login);
* Manipulação do DOM e eventos;
* Arrow Functions;
* Array methods, como `map`, `find` e `some`;
* JSON;
* Modularização através de `import` e `export`;
* Separação de responsabilidades;
* Compilação de TypeScript para JavaScript.

---

# Banco de Dados

O projeto utiliza um banco relacional em **PostgreSQL** (acessado via **JDBC**) para armazenar e relacionar os dados de candidatos, empresas, vagas, competências, formações e interações entre candidatos e empresas. O modelo foi desenvolvido no **dbdiagram.io**, seguindo as regras de negócio do Linketinder.

> As tabelas `formacao`, `candidato_formacoes` e `instituicao_formacao` existem no script SQL, mas ainda não são utilizadas pelo código do backend nem pelo frontend.

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

Um candidato pode possuir diversas competências e curtidas em vagas. A idade não é armazenada diretamente: é calculada a partir da data de nascimento nas consultas.

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

### Candidatos_Que_Curtiram_Vaga

Tabela presente no script SQL para o registro dos candidatos que curtiram cada vaga. As consultas da API utilizam a tabela `vagas_curtidas_candidato`.

### Match

Matches entre candidato, empresa e vaga. Um match ocorre quando há interesse de ambas as partes na mesma oportunidade.

## Lógica aplicada

Um candidato é cadastrado e pode adicionar competências. As empresas também são cadastradas e criam vagas, cada uma com suas competências exigidas. A partir daí:

1. O candidato visualiza as vagas disponíveis;
2. O candidato pode curtir uma vaga de seu interesse;
3. A empresa visualiza os candidatos que curtiram suas vagas e pode demonstrar interesse em um candidato para aquela vaga;
4. As interações são armazenadas nas respectivas tabelas de curtidas;
5. Quando a empresa curte um candidato que já curtiu a vaga, é registrado um **match**;
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

O script SQL do projeto (`queriesLinketinder.sql`), desenvolvido para **PostgreSQL**, contém:

* Criação das tabelas;
* Chaves primárias e estrangeiras;
* Restrições de unicidade;
* Valores padrão;
* Regras de `ON DELETE CASCADE`;
* Dados iniciais para testes: competências, países e estados, e candidatos, empresas, vagas, formações, curtidas e matches fictícios.

---

# Execução

## Pré-requisitos

* **PostgreSQL** em execução, com o banco `linketinder` criado a partir do script `queriesLinketinder.sql`;
* **Java** e **Gradle** (o projeto inclui o Gradle Wrapper);
* **Node.js** e **npm** para o frontend.

Os dados de conexão com o banco (URL, usuário e senha) estão em `database/PostgresConnectionFactory`. Ajuste-os conforme o seu ambiente.

## Backend

O backend possui dois pontos de entrada:

* **API REST** (utilizada pelo frontend): execute a classe `com.jpcode.api.RestServer`. O servidor é iniciado em `http://localhost:8080`;
* **Terminal:** execute a classe `com.jpcode.Main` para utilizar o menu interativo.

Ambos utilizam o mesmo banco de dados PostgreSQL.

## Frontend

Entre no diretório do frontend:

```bash
cd frontend
```

Instale as dependências:

```bash
npm install
```

Compile o projeto com a tarefa configurada com Webpack/Gulp:

```bash
npx gulp
```

O resultado é gerado no diretório `dist` (`dist/html`, `dist/css` e `dist/js`). Depois, abra `dist/html/index.html` no navegador.

> O frontend **depende do backend** para funcionar: a API precisa estar em execução em `http://localhost:8080`. Como o `CorsFilter` do backend libera apenas a origem `http://127.0.0.1:5500`, abra o frontend por meio do **Live Server** (ou outro servidor) nessa origem.

---

# Autor

Desenvolvido por **João Pedro Vaz** durante o **Acelera ZG**.
