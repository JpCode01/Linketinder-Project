package com.jpcode.dao.referencia

import com.jpcode.dao.contratos.ReferenciaRepository
import com.jpcode.database.ConnectionFactory
import com.jpcode.model.referencia.Estado

class EstadoDAO implements ReferenciaRepository<Estado> {

    private final ConnectionFactory connectionFactory

    EstadoDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory
    }

    Optional<Estado> buscarPorId(Long id) {
        String sql = """
            SELECT id, sigla
            FROM estados
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

            Estado estado = new Estado(
                    resultSet.getLong("id"),
                    resultSet.getString("sigla")
            )

            return Optional.of(estado)
        }
    }

    Optional<Long> buscarIdPorNome(String sigla) {
        String sql = """
            SELECT id
            FROM estados
            WHERE sigla = ?
        """

        try (
                def connection = connectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, sigla.toUpperCase())

            def resultSet = statement.executeQuery()

            if (!resultSet.next()) {
                return Optional.empty()
            }

            return Optional.of(resultSet.getLong("id"))
        }
    }

    List<Estado> listarTodos() {
        String sql = """
        SELECT id, sigla
        FROM estados
    """

        try (
                def connection = connectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            def resultSet = statement.executeQuery()
            List<Estado> estados = []

            while (resultSet.next()) {
                Estado estado = new Estado(
                        resultSet.getLong("id"),
                        resultSet.getString("sigla")
                )

                estados.add(estado)
            }

            return estados
        }
    }
}
