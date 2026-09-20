package com.jpcode.dao.referencia

import com.jpcode.database.ConnectionFactory
import com.jpcode.model.referencia.Pais

class PaisDAO {

    Pais buscarPorId(Long id) {
        String sql = """
            SELECT id, nome
            FROM pais
            WHERE id = ?
        """

        try (
            def connection = ConnectionFactory.getConnection()
            def statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, id)

            def resultSet = statement.executeQuery()

            if (!resultSet.next()) {
                return null
            }

            return new Pais(
                    resultSet.getLong("id"),
                    resultSet.getString("nome")
                    
            )
        }
    }

    Long buscarIdPorNome(String nome) {
        String sql = """
            SELECT id
            FROM pais
            WHERE nome = ?
        """

        try (
            def connection = ConnectionFactory.getConnection()
            def statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, nome.toUpperCase())

            def resultSet = statement.executeQuery()

            if (!resultSet.next()) {
                return null
            }

            return resultSet.getLong("id")
        }
    }
}
