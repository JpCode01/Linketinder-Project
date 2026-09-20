-- Todos os comandos Utilizados ao longo do projeto

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

CREATE TABLE "vagas_competencias" (
                                      "id" serial PRIMARY KEY,
                                      "id_vaga" int NOT NULL REFERENCES "vagas" ("id"),
                                      "id_competencia" int NOT NULL REFERENCES "competencias" ("id")
);

CREATE TABLE "vagas_curtidas_candidato" (
                                            "id" serial PRIMARY KEY,
                                            "id_candidato" int NOT NULL REFERENCES "candidatos" ("id"),
                                            "id_vaga" int NOT NULL REFERENCES "vagas" ("id")
);

CREATE TABLE "candidatos_curtidos_empresa" (
                                               "id" serial PRIMARY KEY,
                                               "id_empresa" int NOT NULL REFERENCES "empresas" ("id"),
                                               "id_candidato" int NOT NULL REFERENCES "candidatos" ("id")
);

CREATE TABLE "candidatos_que_curtiram_vaga" (
                                                "id" serial PRIMARY KEY,
                                                "id_candidato" int NOT NULL REFERENCES "candidatos" ("id"),
                                                "id_vaga" int NOT NULL REFERENCES "vagas" ("id")
);

CREATE TABLE "match" (
                         "id_candidato" int NOT NULL REFERENCES "candidatos" ("id"),
                         "id_empresa" int NOT NULL REFERENCES "empresas" ("id"),
                         "id_vaga" int NOT NULL REFERENCES "vagas" ("id")
);

INSERT INTO "pais" ("nome")
VALUES
    ('Brasil'),
    ('Estados Unidos'),
    ('Reino Unido');

INSERT INTO "instituicao_formacao" ("nome_instituicao")
VALUES
    ('FATEC Bragança Paulista'),
    ('Universidade de São Paulo'),
    ('FIAP');

INSERT INTO "competencias" ("nome_competencia")
VALUES
    ('Java'),
    ('Python'),
    ('JavaScript');

INSERT INTO "candidatos"
("nome", "sobrenome", "email", "senha", "data_nascimento", "cpf", "id_pais", "cep", "descricao", "ativo")
VALUES
    ('João', 'Silva', '[joao@email.com](mailto:joao@email.com)', '123456', '2005-03-15', '11111111111', 1, '12900000', 'Desenvolvedor Java', true),
    ('Maria', 'Santos', '[maria@email.com](mailto:maria@email.com)', '123456', '2004-07-20', '22222222222', 1, '12900001', 'Desenvolvedora Python', true),
    ('Pedro', 'Oliveira', '[pedro@email.com](mailto:pedro@email.com)', '123456', '2003-01-10', '33333333333', 2, '01000000', 'Desenvolvedor JavaScript', true);

INSERT INTO "formacao"
("curso", "id_candidato", "id_instituicao", "inicio", "termino_ou_possivel")
VALUES
    ('Análise e Desenvolvimento de Sistemas', 1, 1, '2025-01-01', '2027-12-31'),
    ('Ciência da Computação', 2, 2, '2024-01-01', '2028-12-31'),
    ('Engenharia de Software', 3, 3, '2025-01-01', '2029-12-31');

INSERT INTO "candidato_formacoes"
("id_formacao", "id_candidato")
VALUES
    (1, 1),
    (2, 2),
    (3, 3);

INSERT INTO "candidatos_competencias"
("id_candidato", "id_competencia")
VALUES
    (1, 1),
    (2, 2),
    (3, 3);

INSERT INTO "empresas"
("nome", "cnpj", "email_corporativo", "descricao", "id_pais", "cep")
VALUES
    ('Pastelsoft', '11111111000111', '[contato@pastelsoft.com](mailto:contato@pastelsoft.com)', 'Empresa de tecnologia', 1, '12900010'),
    ('ZG Soluções', '22222222000122', '[contato@zgs.com](mailto:contato@zgs.com)', 'Soluções em tecnologia', 1, '12900011'),
    ('Tech Corp', '33333333000133', '[contato@techcorp.com](mailto:contato@techcorp.com)', 'Desenvolvimento de software', 2, '01000001');

INSERT INTO "vagas"
("nome", "descricao", "local", "id_empresa")
VALUES
    ('Desenvolvedor Java Junior', 'Desenvolvimento de aplicações utilizando Java', 'Bragança Paulista', 1),
    ('Desenvolvedor Python', 'Desenvolvimento de aplicações utilizando Python', 'São Paulo', 2),
    ('Desenvolvedor JavaScript', 'Desenvolvimento de aplicações web utilizando JavaScript', 'Campinas', 3);

INSERT INTO "vagas_competencias"
("id_vaga", "id_competencia")
VALUES
    (1, 1),
    (2, 2),
    (3, 3);

INSERT INTO "vagas_curtidas_candidato"
("id_candidato", "id_vaga")
VALUES
    (1, 1),
    (2, 2),
    (3, 3);

INSERT INTO "candidatos_curtidos_empresa"
("id_empresa", "id_candidato")
VALUES
    (1, 1),
    (2, 2),
    (3, 3);

INSERT INTO "candidatos_que_curtiram_vaga"
("id_candidato", "id_vaga")
VALUES
    (1, 1),
    (2, 2),
    (3, 3);

INSERT INTO "match"
("id_candidato", "id_empresa", "id_vaga")
VALUES
    (1, 1, 1),
    (2, 2, 2),
    (3, 3, 3);

-- Alterações necessárias ao longo do desenvolvimento

ALTER TABLE candidatos
    ADD COLUMN idade int;

CREATE TABLE "estados" (
                           "id" serial PRIMARY KEY,
                           "sigla" varchar(2) NOT NULL
);

ALTER TABLE "empresas"
    ADD COLUMN "id_estado" int REFERENCES "estados" ("id");

ALTER TABLE "candidatos"
    ADD COLUMN "id_estado" int REFERENCES "estados" ("id");

ALTER TABLE "empresas"
    ADD COLUMN "senha" varchar;

select * from match;

INSERT INTO pais (nome) VALUES
                            ('BRASIL'),
                            ('ESTADOS UNIDOS'),
                            ('CANADA'),
                            ('REINO UNIDO'),
                            ('ALEMANHA'),
                            ('FRANCA'),
                            ('JAPAO'),
                            ('CHINA'),
                            ('AUSTRALIA'),
                            ('PORTUGAL');

INSERT INTO estados (sigla) VALUES
                                ('SP'),
                                ('RJ'),
                                ('MG'),
                                ('PR'),
                                ('SC'),
                                ('RS'),
                                ('BA'),
                                ('PE'),
                                ('GO'),
                                ('DF');

INSERT INTO competencias (nome_competencia) VALUES
                                                ('JAVA'),
                                                ('GROOVY'),
                                                ('PYTHON'),
                                                ('JAVASCRIPT'),
                                                ('TYPESCRIPT'),
                                                ('SPRING'),
                                                ('SPRING BOOT'),
                                                ('DJANGO'),
                                                ('NODE.JS'),
                                                ('REACT'),
                                                ('ANGULAR'),
                                                ('POSTGRESQL'),
                                                ('DOCKER'),
                                                ('GIT'),
                                                ('AWS');

ALTER TABLE empresas
    ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT true;