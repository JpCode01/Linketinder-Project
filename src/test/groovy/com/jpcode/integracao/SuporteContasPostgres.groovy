package com.jpcode.integracao

import com.jpcode.database.ConnectionFactory
import java.sql.Connection

/** Schema próprio confirmado para testes que precisam de várias conexões/transações reais. */
class SuporteContasPostgres implements AutoCloseable {
    final String schema
    final ConnectionFactory factory

    SuporteContasPostgres() {
        try (Connection conexao = SuportePostgres.abrir()) {
            schema = conexao.schema
            SuportePostgres.criarSchemaAtual(conexao)
            conexao.createStatement().withCloseable { comando ->
                comando.execute("INSERT INTO pais (nome) VALUES ('BRASIL')")
                comando.execute("INSERT INTO estados (sigla) VALUES ('SP')")
                comando.execute("INSERT INTO areas_recrutamento (nome) VALUES ('Recursos Humanos')")
            }
            conexao.commit()
        }
        factory = { -> abrirConexao() } as ConnectionFactory
    }

    Connection abrirConexao() {
        // O validador é executado em CADA conexão, sem fallback e antes de qualquer escrita.
        Connection conexao = SuportePostgres.conectar()
        try {
            validarSchema()
            conexao.createStatement().withCloseable { comando ->
                comando.execute("SET search_path TO ${schema}".toString())
                comando.execute("SET lock_timeout TO '5s'")
                comando.execute("SET statement_timeout TO '10s'")
            }
            return conexao
        } catch (Exception erro) {
            conexao.close()
            throw erro
        }
    }

    void close() {
        validarSchema()
        try (Connection conexao = SuportePostgres.conectar(); def comando = conexao.createStatement()) {
            // Nome obtido da conexão que criou o schema; nunca aceita schema/banco externo.
            comando.execute("DROP SCHEMA ${schema} CASCADE".toString())
        }
    }

    private void validarSchema() {
        if (!(schema ==~ /linketinder_test_[a-f0-9]{32}/)) {
            throw new IllegalStateException('Cleanup restrito ao schema aleatório criado pelo teste.')
        }
    }
}
