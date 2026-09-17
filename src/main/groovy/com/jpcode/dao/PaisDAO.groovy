package com.jpcode.dao

import com.jpcode.database.ConnectionFactory
import com.jpcode.model.Pais

class PaisDAO {

    Pais buscarPorId(Long id) {
        String sql = """
            SELECT id, nome
            FROM pais
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

            return new Pais(
                    resultSet.getLong("id"),
                    resultSet.getString("nome")
                    
            )
        }
    }
}
