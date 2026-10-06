package com.jpcode.dao.referencia

import com.jpcode.dao.referencia.contrato.ReferenciaRepository
import com.jpcode.database.ConnectionFactory
import com.jpcode.model.referencia.Pais

class PaisDAO implements ReferenciaRepository {

    Optional<Pais> buscarPorId(Long id) {
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
            def connection = ConnectionFactory.getConnection()
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
}
