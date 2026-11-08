package com.jpcode.dao.conta

import com.jpcode.database.ConnectionFactory
import com.jpcode.exception.conta.IdentificadorEmUsoException

import java.sql.Connection
import java.sql.SQLException

/** Protocolo comum a todos os escritores de contas. A conexão pertence a esta operação. */
class ValidacaoContaDAO {
    private final ConnectionFactory connectionFactory
    private static final Map<String, List<String>> PERFIS = [
            candidatos: ['email', 'cpf'], empresas: ['email_corporativo', 'cnpj'],
            recrutadores: ['email', 'cpf']].asImmutable()

    ValidacaoContaDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory
    }

    def executar(String perfil, Long id, String email, String identificador, Closure escrita) {
        if (!PERFIS.containsKey(perfil)) throw new IllegalArgumentException('Perfil inválido.')
        try (Connection conexao = connectionFactory.getConnection()) {
            conexao.transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
            conexao.autoCommit = false
            try {
                // Ordem única, inclusive em desativação; leituras comuns continuam permitidas.
                conexao.createStatement().withCloseable { comando ->
                    comando.execute('LOCK TABLE candidatos, empresas, recrutadores IN SHARE ROW EXCLUSIVE MODE')
                }
                if (id != null && email != null) exigirAtivo(conexao, perfil, id)
                if (email != null) validar(conexao, perfil, id, email, identificador)
                def resultado = escrita.call(conexao)
                conexao.commit()
                return resultado
            } catch (Exception erro) {
                conexao.rollback()
                if (erro instanceof SQLException && erro.SQLState == '23505') {
                    throw new IdentificadorEmUsoException()
                }
                throw erro
            }
        }
    }

    private static void exigirAtivo(Connection conexao, String perfil, Long id) {
        conexao.prepareStatement("SELECT 1 FROM ${perfil} WHERE id = ? AND ativo = true".toString()).withCloseable { comando ->
            comando.setLong(1, id)
            comando.executeQuery().withCloseable { resultado ->
                if (!resultado.next()) throw new IllegalStateException('Conta inexistente ou inativa.')
            }
        }
    }

    private static void validar(Connection conexao, String perfil, Long id, String email, String identificador) {
        PERFIS.each { String tabela, List<String> campos ->
            String exclusao = tabela == perfil && id != null ? ' AND id <> ?' : ''
            conexao.prepareStatement("SELECT 1 FROM ${tabela} WHERE ativo = true AND ${campos[0]} = ?${exclusao}".toString()).withCloseable { comando ->
                comando.setString(1, email)
                if (exclusao) comando.setLong(2, id)
                comando.executeQuery().withCloseable { resultado ->
                    if (resultado.next()) throw new IdentificadorEmUsoException()
                }
            }
        }
        String exclusao = id == null ? '' : ' AND id <> ?'
        conexao.prepareStatement("SELECT 1 FROM ${perfil} WHERE ativo = true AND ${PERFIS[perfil][1]} = ?${exclusao}".toString()).withCloseable { comando ->
            comando.setString(1, identificador)
            if (id != null) comando.setLong(2, id)
            comando.executeQuery().withCloseable { resultado ->
                if (resultado.next()) throw new IdentificadorEmUsoException()
            }
        }
    }
}
