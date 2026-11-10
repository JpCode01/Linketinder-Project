package com.jpcode.dao.referencia

import com.jpcode.dao.contratos.ReferenciaRepository
import com.jpcode.database.ConnectionFactory
import com.jpcode.model.referencia.Pais

class PaisDAO implements ReferenciaRepository {

    private final ConnectionFactory connectionFactory

    PaisDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory
    }

    Optional<Pais> buscarPorId(Long id) {
        String sql = """
            SELECT id, nome
            FROM pais
            WHERE id = ?
        """

        try (
                def connection = connectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, id)

            def resultSet = statement.executeQuery()

            if (!resultSet.next()) {
                return Optional.empty()
            }

            Pais pais = new Pais(
                    resultSet.getLong("id"),
                    resultSet.getString("nome")

            )

            return Optional.of(pais)
        }
    }

    Optional<Long> buscarIdPorNome(String nome) {
        String sql = """
            SELECT id
            FROM pais
            WHERE nome = ?
        """

        try (
                def connection = connectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, nome.toUpperCase())

            def resultSet = statement.executeQuery()

            if (!resultSet.next()) {
                return Optional.empty()
            }

            return Optional.of(resultSet.getLong("id"))
        }
    }
    List<Pais> listarTodos() {
        try (def conexao = connectionFactory.getConnection();
             def comando = conexao.prepareStatement('SELECT id, nome FROM pais ORDER BY id');
             def resultado = comando.executeQuery()) {
            List<Pais> referencias = []
            while (resultado.next()) referencias.add(new Pais(resultado.getLong('id'), resultado.getString('nome')))
            return referencias
        }
    }
}
