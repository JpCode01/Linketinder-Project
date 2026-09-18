package com.jpcode.dao.referencia

import com.jpcode.database.ConnectionFactory
import com.jpcode.model.referencia.Estado

class EstadoDAO {

    Estado buscarPorId(Long id) {
        String sql = """
            SELECT id, sigla
            FROM estados
            WHERE id = ?
        """

        try (
            def connection = ConnectionFactory.getConnection(sql)
            def statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, id)

            def resultSet = statement.executeQuery()

            if (!resultSet.next()) {
                return null
            }

            return new Estado(
                    resultSet.getLong("id"),
                    resultSet.getString("sigla")
            )
        }
    }

    Long buscarrIdPorNome(String sigla) {
        String sql = """
            SELECT id
            FROM estados
            WHERE sigla = ?
        """

        try (
            def connection = ConnectionFactory.getConnection()
            def statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, sigla.toUpperCase())

            def resultSet = statement.executeQuery()

            if (!resultSet.next()) {
                return null
            }

            return resultSet.getLong("id")
        }
    }
}
