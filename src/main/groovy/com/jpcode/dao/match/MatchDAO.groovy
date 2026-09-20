package com.jpcode.dao.match

import com.jpcode.database.ConnectionFactory
import com.jpcode.model.core.Match

import java.sql.Statement

class MatchDAO {
    void salvar(Long idCandidato, Long idEmpresa, Long idVaga) {
        String sql = """
            INSERT INTO 
                (id_candidato, id_empresa, id_vaga)
            VALUES (?, ?, ?)
        """

        try (
            def connection = ConnectionFactory.getConnection(sql)
            def statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setLong(1, idCandidato)
            statement.setLong(2, idEmpresa)
            statement.setLong(3, idVaga)

            statement.executeUpdate()
        }
    }

    Match buscarPorId(Long id) {
        String sql = """
            SELECT id, id_candidato, id_empresa, id_vaga
            FROM match
            WHERE id = ?
        """

        try (
            def connection = ConnectionFactory.getConnection()
            def statement = connection.prepareStatement(sql)
        ) {
            def resultSet = statement.executeQuery()

            if (!resultSet.next()) {
                return null
            }

            return new Match(
                    resultSet.getLong("id"),
                    resultSet.getLong("id_candidato"),
                    resultSet.getLong("id_empresa"),
                    resultSet.getLong("id_vaga"),
            )
        }
    }

    List<Match> buscarMatchesPorEmpresa(Long idEmpresa) {
        String sql = """
        SELECT id, id_candidato, id_empresa, id_vaga
        FROM match
        WHERE id_empresa = ?
        """

        try (
            def connection = ConnectionFactory.getConnection()
            def statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, idEmpresa)

            def resultSet = statement.executeQuery()

            List<Match> matches = []
            
            while(resultSet.next()) {
                matches.add(
                        new Match(
                                resultSet.getLong("id"),
                                resultSet.getLong("id_candidato"),
                                resultSet.getLong("id_empresa"),
                                resultSet.getLong("id_vaga")
                        )
                )
            }

            return matches
        }
    }
}
