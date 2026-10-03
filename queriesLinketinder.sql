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

ALTER TABLE candidatos_competencias
    ADD CONSTRAINT uk_candidato_competencia
        UNIQUE (id_candidato, id_competencia);

ALTER TABLE vagas_competencias
    ADD CONSTRAINT uk_vaga_competencia
        UNIQUE (id_vaga, id_competencia);

ALTER TABLE vagas_curtidas_candidato
    ADD CONSTRAINT uk_candidato_vaga
        UNIQUE (id_candidato, id_vaga);

ALTER TABLE candidatos_curtidos_empresa
    ADD CONSTRAINT uk_empresa_candidato
        UNIQUE (id_empresa, id_candidato);

ALTER TABLE candidato_formacoes
    ADD CONSTRAINT uk_candidato_formacao
        UNIQUE (id_candidato, id_formacao);

ALTER TABLE "match"
    ADD CONSTRAINT uk_match
        UNIQUE (id_candidato, id_empresa, id_vaga);

ALTER TABLE vagas_competencias
DROP CONSTRAINT vagas_competencias_id_vaga_fkey;

ALTER TABLE vagas_competencias
    ADD CONSTRAINT vagas_competencias_id_vaga_fkey
        FOREIGN KEY (id_vaga)
            REFERENCES vagas(id)
            ON DELETE CASCADE;

ALTER TABLE vagas_curtidas_candidato
DROP CONSTRAINT vagas_curtidas_candidato_id_vaga_fkey;

ALTER TABLE vagas_curtidas_candidato
    ADD CONSTRAINT vagas_curtidas_candidato_id_vaga_fkey
        FOREIGN KEY (id_vaga)
            REFERENCES vagas(id)
            ON DELETE CASCADE;

ALTER TABLE "match"
DROP CONSTRAINT match_id_vaga_fkey;

ALTER TABLE "match"
    ADD CONSTRAINT match_id_vaga_fkey
        FOREIGN KEY (id_vaga)
            REFERENCES vagas(id)
            ON DELETE CASCADE;

-- Versão Atualizada para Criação do projeto

CREATE TABLE pais (
                      id SERIAL PRIMARY KEY,
                      nome VARCHAR NOT NULL
);

CREATE TABLE estados (
                         id SERIAL PRIMARY KEY,
                         sigla VARCHAR(2) NOT NULL
);

CREATE TABLE instituicao_formacao (
                                      id SERIAL PRIMARY KEY,
                                      nome_instituicao VARCHAR NOT NULL
);

CREATE TABLE competencias (
                              id SERIAL PRIMARY KEY,
                              nome_competencia VARCHAR NOT NULL
);

CREATE TABLE candidatos (
                            id SERIAL PRIMARY KEY,
                            nome VARCHAR NOT NULL,
                            sobrenome VARCHAR NOT NULL,
                            email VARCHAR NOT NULL UNIQUE,
                            senha VARCHAR NOT NULL,
                            data_nascimento DATE NOT NULL,
                            cpf VARCHAR NOT NULL UNIQUE,
                            id_pais INTEGER NOT NULL REFERENCES pais(id),
                            cep VARCHAR NOT NULL,
                            descricao TEXT,
                            ativo BOOLEAN,
                            idade INTEGER,
                            id_estado INTEGER REFERENCES estados(id)
);

CREATE TABLE empresas (
                          id SERIAL PRIMARY KEY,
                          nome VARCHAR NOT NULL,
                          cnpj VARCHAR NOT NULL UNIQUE,
                          email_corporativo VARCHAR NOT NULL UNIQUE,
                          descricao TEXT,
                          id_pais INTEGER NOT NULL REFERENCES pais(id),
                          cep VARCHAR NOT NULL,
                          id_estado INTEGER REFERENCES estados(id),
                          senha VARCHAR,
                          ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE formacao (
                          id SERIAL PRIMARY KEY,
                          curso VARCHAR NOT NULL,
                          id_candidato INTEGER NOT NULL REFERENCES candidatos(id),
                          id_instituicao INTEGER NOT NULL REFERENCES instituicao_formacao(id),
                          inicio DATE NOT NULL,
                          termino_ou_possivel DATE NOT NULL
);

CREATE TABLE candidato_formacoes (
                                     id SERIAL PRIMARY KEY,
                                     id_formacao INTEGER NOT NULL REFERENCES formacao(id),
                                     id_candidato INTEGER NOT NULL REFERENCES candidatos(id),
                                     CONSTRAINT uk_candidato_formacao UNIQUE (id_candidato, id_formacao)
);

CREATE TABLE candidatos_competencias (
                                         id SERIAL PRIMARY KEY,
                                         id_candidato INTEGER NOT NULL REFERENCES candidatos(id),
                                         id_competencia INTEGER NOT NULL REFERENCES competencias(id),
                                         CONSTRAINT uk_candidato_competencia UNIQUE (id_candidato, id_competencia)
);

CREATE TABLE vagas (
                       id SERIAL PRIMARY KEY,
                       nome VARCHAR NOT NULL,
                       descricao VARCHAR NOT NULL,
                       local VARCHAR NOT NULL,
                       id_empresa INTEGER NOT NULL REFERENCES empresas(id)
);

CREATE TABLE vagas_competencias (
                                    id SERIAL PRIMARY KEY,
                                    id_vaga INTEGER NOT NULL REFERENCES vagas(id) ON DELETE CASCADE,
                                    id_competencia INTEGER NOT NULL REFERENCES competencias(id),
                                    CONSTRAINT uk_vaga_competencia UNIQUE (id_vaga, id_competencia)
);

CREATE TABLE candidatos_curtidos_empresa (
                                             id SERIAL PRIMARY KEY,
                                             id_empresa INTEGER NOT NULL REFERENCES empresas(id),
                                             id_candidato INTEGER NOT NULL REFERENCES candidatos(id),
                                             CONSTRAINT uk_empresa_candidato UNIQUE (id_empresa, id_candidato)
);

CREATE TABLE candidatos_que_curtiram_vaga (
                                              id SERIAL PRIMARY KEY,
                                              id_candidato INTEGER NOT NULL REFERENCES candidatos(id),
                                              id_vaga INTEGER NOT NULL REFERENCES vagas(id)
);

CREATE TABLE vagas_curtidas_candidato (
                                          id SERIAL PRIMARY KEY,
                                          id_candidato INTEGER NOT NULL REFERENCES candidatos(id),
                                          id_vaga INTEGER NOT NULL REFERENCES vagas(id) ON DELETE CASCADE,
                                          CONSTRAINT uk_candidato_vaga UNIQUE (id_candidato, id_vaga)
);

CREATE TABLE match (
                       id SERIAL PRIMARY KEY,
                       id_candidato INTEGER NOT NULL REFERENCES candidatos(id),
                       id_empresa INTEGER NOT NULL REFERENCES empresas(id),
                       id_vaga INTEGER NOT NULL REFERENCES vagas(id) ON DELETE CASCADE,
                       CONSTRAINT uk_match UNIQUE (id_candidato, id_empresa, id_vaga)
);

-- Inserções

INSERT INTO candidatos (
    nome, sobrenome, email, senha, data_nascimento,
    cpf, id_pais, cep, descricao, ativo, idade, id_estado
) VALUES
      (
          'Lucas', 'Oliveira', 'lucas.oliveira@example.com',
          'senha123', '2002-04-15', '111.222.333-44',
          1, '12900000', 'Desenvolvedor Java Júnior',
          TRUE, 24, 1
      ),
      (
          'Mariana', 'Santos', 'mariana.santos@example.com',
          'senha456', '2001-08-20', '222.333.444-55',
          1, '12900000', 'Desenvolvedora Backend',
          TRUE, 25, 1
      ),
      (
          'Rafael', 'Costa', 'rafael.costa@example.com',
          'senha789', '2003-01-10', '333.444.555-66',
          1, '12900000', 'Estudante de Engenharia de Software',
          TRUE, 23, 1
      );

INSERT INTO empresas (
    nome, cnpj, email_corporativo, descricao,
    id_pais, cep, id_estado, senha, ativo
) VALUES
      (
          'Tech Solutions', '11.222.333/0001-44',
          'contato@techsolutions.example',
          'Empresa especializada em desenvolvimento de sistemas.',
          1, '12900000', 1, 'empresa123', TRUE
      ),
      (
          'DevWorks', '22.333.444/0001-55',
          'contato@devworks.example',
          'Consultoria de tecnologia e desenvolvimento de software.',
          1, '12900000', 1, 'empresa456', TRUE
      );

INSERT INTO vagas (nome, descricao, local, id_empresa)
VALUES
    (
        'Desenvolvedor Java Júnior',
        'Desenvolvimento de APIs REST utilizando Java e Spring.',
        'Bragança Paulista',
        (
            SELECT id FROM empresas
            WHERE email_corporativo = 'contato@techsolutions.example'
        )
    ),
    (
        'Desenvolvedor Backend',
        'Desenvolvimento de aplicações backend e integração com bancos de dados.',
        'São Paulo',
        (
            SELECT id FROM empresas
            WHERE email_corporativo = 'contato@devworks.example'
        )
    );

INSERT INTO candidatos_competencias (id_candidato, id_competencia)
VALUES
    (
        (SELECT id FROM candidatos
         WHERE email = 'lucas.oliveira@example.com'),
        1
    ),
    (
        (SELECT id FROM candidatos
         WHERE email = 'lucas.oliveira@example.com'),
        6
    ),
    (
        (SELECT id FROM candidatos
         WHERE email = 'mariana.santos@example.com'),
        1
    ),
    (
        (SELECT id FROM candidatos
         WHERE email = 'rafael.costa@example.com'),
        6
    );

INSERT INTO vagas_competencias (id_vaga, id_competencia)
VALUES
    (
        (SELECT id FROM vagas
         WHERE nome = 'Desenvolvedor Java Júnior'
           AND id_empresa = (
             SELECT id FROM empresas
             WHERE email_corporativo = 'contato@techsolutions.example'
         )),
        1
    ),
    (
        (SELECT id FROM vagas
         WHERE nome = 'Desenvolvedor Java Júnior'
           AND id_empresa = (
             SELECT id FROM empresas
             WHERE email_corporativo = 'contato@techsolutions.example'
         )),
        6
    ),
    (
        (SELECT id FROM vagas
         WHERE nome = 'Desenvolvedor Backend'
           AND id_empresa = (
             SELECT id FROM empresas
             WHERE email_corporativo = 'contato@devworks.example'
         )),
        1
    );

INSERT INTO candidatos_curtidos_empresa (id_empresa, id_candidato)
VALUES (
           (SELECT id FROM empresas
            WHERE email_corporativo = 'contato@techsolutions.example'),
           (SELECT id FROM candidatos
            WHERE email = 'lucas.oliveira@example.com')
       );

INSERT INTO vagas_curtidas_candidato (id_candidato, id_vaga)
VALUES (
           (SELECT id FROM candidatos
            WHERE email = 'lucas.oliveira@example.com'),
           (
               SELECT v.id
               FROM vagas v
                        JOIN empresas e ON e.id = v.id_empresa
               WHERE v.nome = 'Desenvolvedor Java Júnior'
                 AND e.email_corporativo = 'contato@techsolutions.example'
           )
       );

INSERT INTO match (id_candidato, id_empresa, id_vaga)
VALUES (
           (SELECT id FROM candidatos
            WHERE email = 'lucas.oliveira@example.com'),
           (SELECT id FROM empresas
            WHERE email_corporativo = 'contato@techsolutions.example'),
           (
               SELECT v.id
               FROM vagas v
                        JOIN empresas e ON e.id = v.id_empresa
               WHERE v.nome = 'Desenvolvedor Java Júnior'
                 AND e.email_corporativo = 'contato@techsolutions.example'
           )
       );

SELECT
    c.nome,
    c.sobrenome,
    comp.nome_competencia
FROM candidatos c
         JOIN candidatos_competencias cc
              ON cc.id_candidato = c.id
         JOIN competencias comp
              ON comp.id = cc.id_competencia
ORDER BY c.nome;

