package com.jpcode.dao.referencia

import com.jpcode.dao.contratos.ReferenciaCompetenciaRepository
import com.jpcode.dao.contratos.ReferenciaRepository
import com.jpcode.database.ConnectionFactory
import com.jpcode.model.referencia.Competencia

class CompetenciaDAO implements ReferenciaRepository<Competencia>, ReferenciaCompetenciaRepository {
    
    private final ConnectionFactory connectionFactory

    CompetenciaDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory
    }

    Optional<Competencia> buscarPorId(Long id) {
        String sql = """
            SELECT id, nome_competencia
            FROM competencias
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

            Competencia competencia = new Competencia(
                    resultSet.getLong("id"),
                    resultSet.getString("nome_competencia")
            )

            return Optional.of(competencia)

        }
    }

    Competencia buscarPorNome(String nome) {
        String sql = """
            SELECT id, nome_competencia
            FROM competencias
            WHERE nome_competencia = ?
        """

        try (
                def connection = connectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, nome)

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

    Optional<Long> buscarIdPorNome(String competencia) {
        String sql = """
            SELECT id
            FROM competencias
            WHERE nome_competencia = ?
        """

        try (
                def connection = connectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, competencia.toUpperCase())

            def resultSet = statement.executeQuery()

            if (!resultSet.next()) {
                return Optional.empty()
            }

            return Optional.of(resultSet.getLong("id"))
        }
    }

    List<String> listarTodasCompetencias() {
        String sql = """
        SELECT nome_competencia
        FROM competencias
        """

        try (
                def connection = connectionFactory.getConnection()
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
