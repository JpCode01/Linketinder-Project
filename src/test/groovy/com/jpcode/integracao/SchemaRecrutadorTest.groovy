package com.jpcode.integracao

import spock.lang.Specification

import java.sql.Connection
import java.sql.SQLException

import static org.junit.jupiter.api.Assumptions.assumeTrue

/** T01: RN-01/RN-06 e parcela de persistência de CA-01/CA-02, sem fluxo HTTP. */
class SchemaRecrutadorTest extends Specification {

    Connection conexao
    long empresa
    long area

    def setup() {
        assumeTrue(SuportePostgres.configurado(),
                'Integração NÃO EXECUTADA: configure LINKETINDER_TEST_JDBC_URL, LINKETINDER_TEST_DB_USER e LINKETINDER_TEST_DB_PASSWORD.')
        conexao = SuportePostgres.abrir()
        SuportePostgres.criarSchemaAtual(conexao)
        long pais = inserir("INSERT INTO pais (nome) VALUES (?) RETURNING id", ['País de teste'])
        empresa = inserir('''INSERT INTO empresas (nome, cnpj, email_corporativo, id_pais, cep, senha)
                             VALUES (?, ?, ?, ?, ?, ?) RETURNING id''',
                ['Empresa de teste', 'cnpj-teste', 'empresa@example.invalid', pais, '00000000', 'fixture-sem-login'])
        area = inserir('INSERT INTO areas_recrutamento (nome) VALUES (?) RETURNING id', ['Recursos Humanos'])
    }

    def cleanup() {
        SuportePostgres.fechar(conexao)
    }

    def 'persiste recrutadores ativos com uma empresa e uma area compartilhadas'() {
        given:
        long primeiro = inserirRecrutador(empresa, area, 'primeiro')
        long segundo = inserirRecrutador(empresa, area, 'segundo')

        expect:
        primeiro != segundo
        conexao.createStatement().withCloseable { comando ->
            comando.executeQuery('''SELECT r.id_empresa, r.id_area_recrutamento, r.ativo, a.nome
                                    FROM recrutadores r JOIN areas_recrutamento a ON a.id = r.id_area_recrutamento
                                    ORDER BY r.id''').withCloseable { resultado ->
                int quantidade = 0
                while (resultado.next()) {
                    assert resultado.getLong('id_empresa') == empresa
                    assert resultado.getLong('id_area_recrutamento') == area
                    assert resultado.getBoolean('ativo')
                    assert resultado.getString('nome') == 'Recursos Humanos'
                    quantidade++
                }
                quantidade == 2
            }
        }
    }

    def 'candidato e empresa tambem iniciam ativos por default'() {
        given:
        inserir('''INSERT INTO candidatos (nome, sobrenome, email, senha, data_nascimento, cpf, id_pais, cep)
                   VALUES (?, ?, ?, ?, ?, ?, (SELECT id FROM pais LIMIT 1), ?) RETURNING id''',
                ['Candidato', 'Teste', 'candidato@example.invalid', 'fixture-sem-login',
                 java.sql.Date.valueOf('1990-01-01'), 'cpf-candidato', '00000000'])

        expect:
        conexao.createStatement().withCloseable { comando ->
            comando.executeQuery('SELECT ativo FROM candidatos UNION ALL SELECT ativo FROM empresas').withCloseable { resultado ->
                assert resultado.next() && resultado.getBoolean(1)
                assert resultado.next() && resultado.getBoolean(1)
                !resultado.next()
            }
        }
    }

    def 'rejeita vinculo #vinculo com referencia #referencia'() {
        when:
        inserirRecrutador(vinculo == 'empresa' ? referencia : empresa,
                vinculo == 'area' ? referencia : area)

        then:
        SQLException erro = thrown()
        erro.SQLState == estado

        where:
        vinculo   | referencia | estado
        'empresa' | -1L        | '23503'
        'area'    | -1L        | '23503'
        'empresa' | null       | '23502'
        'area'    | null       | '23502'
    }

    def 'rejeita atividade nula no perfil #tabela'() {
        given:
        if (tabela == 'recrutadores') {
            inserirRecrutador(empresa, area)
        } else if (tabela == 'candidatos') {
            inserir('''INSERT INTO candidatos (nome, sobrenome, email, senha, data_nascimento, cpf, id_pais, cep)
                       VALUES (?, ?, ?, ?, ?, ?, (SELECT id FROM pais LIMIT 1), ?) RETURNING id''',
                    ['Candidato', 'Teste', 'candidato@example.invalid', 'fixture-sem-login',
                     java.sql.Date.valueOf('1990-01-01'), 'cpf-candidato', '00000000'])
        }

        when:
        // Nome da tabela vem somente dos casos constantes abaixo.
        conexao.createStatement().withCloseable { it.executeUpdate("UPDATE ${tabela} SET ativo = NULL".toString()) }

        then:
        SQLException erro = thrown()
        erro.SQLState == '23502'

        where:
        tabela << ['candidatos', 'empresas', 'recrutadores']
    }

    def 'fks do recrutador sao simples e nao apagam referencias em cascata'() {
        expect:
        conexao.createStatement().withCloseable { comando ->
            comando.executeQuery('''SELECT a.attname AS coluna, c.confrelid::regclass::text AS destino,
                                           cardinality(c.conkey) AS colunas, c.confdeltype
                                    FROM pg_constraint c
                                    JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = c.conkey[1]
                                    WHERE c.conrelid = 'recrutadores'::regclass AND c.contype = 'f'
                                    ORDER BY a.attname''').withCloseable { resultado ->
                assert resultado.next()
                assert resultado.getString('coluna') == 'id_area_recrutamento'
                assert resultado.getString('destino') == 'areas_recrutamento'
                assert resultado.getInt('colunas') == 1
                assert resultado.getString('confdeltype') == 'a'
                assert resultado.next()
                assert resultado.getString('coluna') == 'id_empresa'
                assert resultado.getString('destino') == 'empresas'
                assert resultado.getInt('colunas') == 1
                assert resultado.getString('confdeltype') == 'a'
                !resultado.next()
            }
        }
    }

    private long inserirRecrutador(Long idEmpresa, Long idArea, String identificador = 'unico') {
        inserir('''INSERT INTO recrutadores (nome, id_area_recrutamento, descricao, email, cpf, data_nascimento, senha, id_empresa)
                   VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id''',
                ['Recrutador de teste', idArea, 'Perfil de RH', "${identificador}@example.invalid".toString(),
                 "cpf-${identificador}".toString(), java.sql.Date.valueOf('1990-01-01'), 'fixture-sem-login', idEmpresa])
    }

    private long inserir(String sql, List parametros) {
        conexao.prepareStatement(sql).withCloseable { comando ->
            parametros.eachWithIndex { valor, indice -> comando.setObject(indice + 1, valor) }
            comando.executeQuery().withCloseable { resultado ->
                assert resultado.next()
                resultado.getLong(1)
            }
        }
    }
}

/** Valida o bloqueio antes de qualquer conexão, mesmo sem PostgreSQL configurado. */
class SegurancaSuportePostgresTest extends Specification {

    def 'bloqueia destino inseguro ou configuracao incompleta antes de conectar'() {
        when:
        SuportePostgres.abrir(ambiente)

        then:
        thrown(IllegalArgumentException)

        where:
        ambiente << [
                [:],
                [(SuportePostgres.URL): 'jdbc:postgresql://localhost:5432/linketinder_test'],
                configuracao('jdbc:postgresql://localhost:5432/linketinder'),
                configuracao('jdbc:postgresql://localhost:5432/postgres'),
                configuracao('jdbc:postgresql://localhost:5432/linketinder_test?currentSchema=public'),
                configuracao('jdbc:postgresql://localhost:5432/linketinder_test/../linketinder')
        ]
    }

    def 'aceita apenas configuracao explicita de banco dedicado'() {
        expect:
        !SuportePostgres.configurado([:])
        SuportePostgres.configurado([(SuportePostgres.USUARIO): 'usuario-de-teste'])
        SuportePostgres.validarConfiguracao(configuracao('jdbc:postgresql://127.0.0.1:5432/linketinder_test')) == 'linketinder_test'
        SuportePostgres.validarConfiguracao(configuracao('jdbc:postgresql://localhost:5432/linketinder_test_t01')) == 'linketinder_test_t01'
    }

    private static Map<String, String> configuracao(String url) {
        [(SuportePostgres.URL): url, (SuportePostgres.USUARIO): 'usuario-de-teste',
         (SuportePostgres.SENHA): 'valor-sintetico-sem-conexao']
    }
}
