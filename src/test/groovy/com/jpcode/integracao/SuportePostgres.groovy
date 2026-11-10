package com.jpcode.integracao

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager

class SuportePostgres {

    static final String URL = 'LINKETINDER_TEST_JDBC_URL'
    static final String USUARIO = 'LINKETINDER_TEST_DB_USER'
    static final String SENHA = 'LINKETINDER_TEST_DB_PASSWORD'

    static boolean configurado(Map<String, String> ambiente = System.getenv()) {
        ambiente.containsKey(URL) || ambiente.containsKey(USUARIO) || ambiente.containsKey(SENHA)
    }

    static String validarConfiguracao(Map<String, String> ambiente) {
        if ([URL, USUARIO, SENHA].any { !ambiente[it]?.trim() }) {
            throw new IllegalArgumentException('Configure URL, usuário e senha do banco de testes explicitamente.')
        }
        def endereco = ambiente[URL] =~ /^jdbc:postgresql:\/\/[a-zA-Z0-9.\-]+(?::[0-9]+)?\/(linketinder_test(?:_[a-z0-9]+)*)$/
        if (!endereco.matches()) {
            throw new IllegalArgumentException('Use URL JDBC sem parâmetros e banco dedicado linketinder_test (ou linketinder_test_<sufixo>); banco de desenvolvimento proibido.')
        }
        endereco.group(1)
    }

    static Connection conectar(Map<String, String> ambiente = System.getenv()) {
        String banco = validarConfiguracao(ambiente)
        Properties propriedades = new Properties()
        propriedades.setProperty('user', ambiente[USUARIO])
        propriedades.setProperty('password', ambiente[SENHA])
        propriedades.setProperty('logServerErrorDetail', 'false')
        propriedades.setProperty('connectTimeout', '5')
        Connection conexao = DriverManager.getConnection(ambiente[URL], propriedades)
        try {
            conexao.createStatement().withCloseable { comando ->
                comando.executeQuery('SELECT current_database()').withCloseable { resultado ->
                    if (!resultado.next() || resultado.getString(1) != banco) {
                        throw new IllegalStateException('Conexão não corresponde ao banco de testes autorizado.')
                    }
                }
            }
            return conexao
        } catch (Exception erro) {
            conexao.close()
            throw erro
        }
    }

    static Connection abrir(Map<String, String> ambiente = System.getenv()) {
        Connection conexao = conectar(ambiente)
        try {
            conexao.autoCommit = false
            String schema = 'linketinder_test_' + UUID.randomUUID().toString().replace('-', '')
            conexao.createStatement().withCloseable { comando ->
                comando.execute("CREATE SCHEMA ${schema}".toString())
                comando.execute("SET LOCAL search_path TO ${schema}".toString())
            }
            return conexao
        } catch (Exception erro) {
            fechar(conexao)
            throw erro
        }
    }

    static void criarSchemaAtual(Connection conexao) {
        String sql = Files.readString(Path.of('queriesLinketinder.sql'), StandardCharsets.UTF_8)
        String marcador = '-- Versão Atualizada para Criação do projeto'
        int inicio = sql.indexOf(marcador)
        int fim = sql.indexOf('\n-- Inserções', inicio)
        if (inicio < 0 || fim < 0) {
            throw new IllegalStateException('Delimitadores do DDL atual não encontrados.')
        }
        conexao.createStatement().withCloseable { comando ->
            comando.execute(sql.substring(inicio, fim))
        }
    }

    static void fechar(Connection conexao) {
        if (conexao != null) {
            try {
                if (!conexao.autoCommit) {
                    conexao.rollback()
                }
            } finally {
                conexao.close()
            }
        }
    }
}
