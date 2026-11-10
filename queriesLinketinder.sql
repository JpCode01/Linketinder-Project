-- Versão Atualizada para Criação do projeto

-- Instalação nova: executar somente o DDL desta seção, até -- Inserções.
-- Não executar o arquivo inteiro nem os exemplos legados automaticamente.
-- Após o DDL, cadastrar manualmente pais, estados, competencias e
-- areas_recrutamento (id gerado, nome informado), antes das contas e vínculos.
-- Áreas são referências próprias; não substituem competências.

CREATE TABLE pais (
                      id SERIAL PRIMARY KEY,
                      nome VARCHAR NOT NULL
);

CREATE TABLE estados (
                         id SERIAL PRIMARY KEY,
                         sigla VARCHAR(2) NOT NULL
);

CREATE TABLE competencias (
                              id SERIAL PRIMARY KEY,
                              nome_competencia VARCHAR NOT NULL
);

CREATE TABLE candidatos (
                            id SERIAL PRIMARY KEY,
                            nome VARCHAR NOT NULL,
                            sobrenome VARCHAR NOT NULL,
                            email VARCHAR NOT NULL,
                            senha VARCHAR NOT NULL,
                            data_nascimento DATE NOT NULL,
                            cpf VARCHAR NOT NULL,
                            id_pais INTEGER NOT NULL REFERENCES pais(id),
                            cep VARCHAR NOT NULL,
                            descricao TEXT,
                            ativo BOOLEAN NOT NULL DEFAULT TRUE,
                            idade INTEGER,
                            id_estado INTEGER REFERENCES estados(id)
);

CREATE TABLE empresas (
                          id SERIAL PRIMARY KEY,
                          nome VARCHAR NOT NULL,
                          cnpj VARCHAR NOT NULL,
                          email_corporativo VARCHAR NOT NULL,
                          descricao TEXT,
                          id_pais INTEGER NOT NULL REFERENCES pais(id),
                          cep VARCHAR NOT NULL,
                          id_estado INTEGER REFERENCES estados(id),
                          senha VARCHAR,
                          ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE areas_recrutamento (
                                      id SERIAL PRIMARY KEY,
                                      nome VARCHAR NOT NULL
);

CREATE TABLE recrutadores (
                              id SERIAL PRIMARY KEY,
                              nome VARCHAR NOT NULL,
                              id_area_recrutamento INTEGER NOT NULL REFERENCES areas_recrutamento(id),
                              descricao TEXT,
                              email VARCHAR NOT NULL,
                              cpf VARCHAR NOT NULL,
                              data_nascimento DATE NOT NULL,
                              senha VARCHAR NOT NULL,
                              id_empresa INTEGER NOT NULL REFERENCES empresas(id),
                              ativo BOOLEAN NOT NULL DEFAULT TRUE,
                              CONSTRAINT uk_recrutador_empresa UNIQUE (id, id_empresa)
);

-- Unicidade por perfil somente entre contas ativas; reutilização não altera IDs/FKs.
-- A garantia de e-mail entre os três perfis depende do protocolo transacional de T06.
CREATE UNIQUE INDEX uk_candidato_cpf_ativo ON candidatos (cpf) WHERE ativo = true;
CREATE UNIQUE INDEX uk_candidato_email_ativo ON candidatos (email) WHERE ativo = true;
CREATE UNIQUE INDEX uk_empresa_cnpj_ativo ON empresas (cnpj) WHERE ativo = true;
CREATE UNIQUE INDEX uk_empresa_email_ativo ON empresas (email_corporativo) WHERE ativo = true;
CREATE UNIQUE INDEX uk_recrutador_cpf_ativo ON recrutadores (cpf) WHERE ativo = true;
CREATE UNIQUE INDEX uk_recrutador_email_ativo ON recrutadores (email) WHERE ativo = true;

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
                       id_empresa INTEGER NOT NULL REFERENCES empresas(id),
                       id_recrutador INTEGER,
                       CONSTRAINT uk_vaga_empresa UNIQUE (id, id_empresa),
                       CONSTRAINT fk_vaga_recrutador_empresa FOREIGN KEY (id_recrutador, id_empresa)
                           REFERENCES recrutadores(id, id_empresa)
);

CREATE TABLE vagas_competencias (
                                    id SERIAL PRIMARY KEY,
                                    id_vaga INTEGER NOT NULL REFERENCES vagas(id) ON DELETE CASCADE,
                                    id_competencia INTEGER NOT NULL REFERENCES competencias(id),
                                    CONSTRAINT uk_vaga_competencia UNIQUE (id_vaga, id_competencia)
);

CREATE TABLE vagas_curtidas_candidato (
                                          id SERIAL PRIMARY KEY,
                                          id_candidato INTEGER NOT NULL REFERENCES candidatos(id),
                                          id_vaga INTEGER NOT NULL REFERENCES vagas(id) ON DELETE CASCADE,
                                          CONSTRAINT uk_candidato_vaga UNIQUE (id_candidato, id_vaga)
);

-- Retribuição contextualizada; ator não se confunde com o autor da vaga.
-- Atividade e autoria do ator serão validadas na transação de T09.
CREATE TABLE candidatos_curtidos_empresa (
                                             id SERIAL PRIMARY KEY,
                                             id_empresa INTEGER NOT NULL REFERENCES empresas(id),
                                             id_candidato INTEGER NOT NULL REFERENCES candidatos(id),
                                             id_vaga INTEGER NOT NULL,
                                             id_recrutador_ator INTEGER,
                                             CONSTRAINT uk_retribuicao_candidato_vaga UNIQUE (id_candidato, id_vaga),
                                             CONSTRAINT uk_retribuicao_contexto UNIQUE (id_candidato, id_empresa, id_vaga),
                                             CONSTRAINT fk_retribuicao_interesse FOREIGN KEY (id_candidato, id_vaga)
                                                 REFERENCES vagas_curtidas_candidato(id_candidato, id_vaga),
                                             CONSTRAINT fk_retribuicao_vaga_empresa FOREIGN KEY (id_vaga, id_empresa)
                                                 REFERENCES vagas(id, id_empresa),
                                             CONSTRAINT fk_retribuicao_recrutador_empresa FOREIGN KEY (id_recrutador_ator, id_empresa)
                                                 REFERENCES recrutadores(id, id_empresa)
);

CREATE TABLE match (
                       id SERIAL PRIMARY KEY,
                       id_candidato INTEGER NOT NULL REFERENCES candidatos(id),
                       id_empresa INTEGER NOT NULL REFERENCES empresas(id),
                       id_vaga INTEGER NOT NULL REFERENCES vagas(id) ON DELETE CASCADE,
                       CONSTRAINT uk_match UNIQUE (id_candidato, id_vaga),
                       CONSTRAINT fk_match_vaga_empresa FOREIGN KEY (id_vaga, id_empresa)
                           REFERENCES vagas(id, id_empresa),
                       CONSTRAINT fk_match_retribuicao FOREIGN KEY (id_candidato, id_empresa, id_vaga)
                           REFERENCES candidatos_curtidos_empresa(id_candidato, id_empresa, id_vaga)
);

-- Inserções

-- Exemplos legados abaixo: não integram a preparação de referências ou o DDL.
-- Não executar automaticamente nem usar como fixtures dos testes de integração.

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

