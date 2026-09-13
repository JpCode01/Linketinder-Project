CREATE TABLE "pais" (
  "id" serial PRIMARY KEY,
  "nome" varchar NOT NULL
);

CREATE TABLE "instituicao_formacao" (
  "id" serial PRIMARY KEY,
  "nome_instituicao" varchar NOT NULL
);

CREATE TABLE "competencias" (
  "id" serial PRIMARY KEY,
  "nome_competencia" varchar NOT NULL
);

CREATE TABLE "candidatos" (
  "id" serial PRIMARY KEY,
  "nome" varchar NOT NULL,
  "sobrenome" varchar NOT NULL,
  "email" varchar UNIQUE NOT NULL,
  "senha" varchar NOT NULL,
  "data_nascimento" date NOT NULL,
  "cpf" varchar UNIQUE NOT NULL,
  "id_pais" int NOT NULL REFERENCES "pais" ("id"),
  "cep" varchar NOT NULL,
  "descricao" text,
  "ativo" boolean
);

CREATE TABLE "formacao" (
  "id" serial PRIMARY KEY,
  "curso" varchar NOT NULL,
  "id_candidato" int NOT NULL REFERENCES "candidatos" ("id"),
  "id_instituicao" int NOT NULL REFERENCES "instituicao_formacao" ("id"),
  "inicio" date NOT NULL,
  "termino_ou_possivel" date NOT NULL
);

CREATE TABLE "candidato_formacoes" (
  "id" serial PRIMARY KEY,
  "id_formacao" int NOT NULL REFERENCES "formacao" ("id"),
  "id_candidato" int NOT NULL REFERENCES "candidatos" ("id")
);

CREATE TABLE "candidatos_competencias" (
  "id" serial PRIMARY KEY,
  "id_candidato" int NOT NULL REFERENCES "candidatos" ("id"),
  "id_competencia" int NOT NULL REFERENCES "competencias" ("id")
);

CREATE TABLE "empresas" (
  "id" serial PRIMARY KEY,
  "nome" varchar NOT NULL,
  "cnpj" varchar UNIQUE NOT NULL,
  "email_corporativo" varchar UNIQUE NOT NULL,
  "descricao" text,
  "id_pais" int NOT NULL REFERENCES "pais" ("id"),
  "cep" varchar NOT NULL
);

CREATE TABLE "vagas" (
  "id" serial PRIMARY KEY,
  "nome" varchar NOT NULL,
  "descricao" varchar NOT NULL,
  "local" varchar NOT NULL,
  "id_empresa" int NOT NULL REFERENCES "empresas" ("id")
);

CREATE TABLE "vagas_competencias" (
  "id" serial PRIMARY KEY,
  "id_vaga" int NOT NULL REFERENCES "vagas" ("id"),
  "id_competencia" int NOT NULL REFERENCES "competencias" ("id")
);