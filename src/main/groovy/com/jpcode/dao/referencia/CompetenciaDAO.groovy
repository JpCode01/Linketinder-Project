package com.jpcode.dao.referencia

import com.jpcode.database.ConnectionFactory
import com.jpcode.model.referencia.Competencia

class CompetenciaDAO {
    Competencia buscarPorId(Long id) {
        String sql = """
            SELECT id, nome_competencia
            FROM competencias
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

            return new Competencia(
                    resultSet.getLong("id"),
                    resultSet.getString("nome_competencia")
            )

        }
    }

    Long buscarIdPorNomeCompetencia(String competencia) {
        String sql = """
            SELECT id
            FROM competencias
            WHERE nome_competencia = ?
        """

        try (
            def connection = ConnectionFactory.getConnection()
            def statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, competencia.toUpperCase())

            def resultSet = statement.executeQuery()

            if (!resultSet.next()) {
                return null
            }

            return resultSet.getLong("id")
        }
    }

    List<String> listarTodasCompetencias() {
        String sql = """
        SELECT nome_competencia
        FROM competencias
        """

        try (
            def connection = ConnectionFactory.getConnection()
            def statement = connection.prepareStatement(sql)
        ) {
            def resultSet = statement.executeQuery()
            List<String> competencias = []

            while (resultSet.next()) {
                competencias.add(
                        resultSet.getString("nome_competencia")
                )
            }

            return competencias
        }


    }
}
