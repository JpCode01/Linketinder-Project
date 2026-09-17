package com.jpcode.dao

import com.jpcode.database.ConnectionFactory
import com.jpcode.model.Match

import java.sql.Statement

class MatchDAO {
    Match salvar(Match match) {
        String sql = """
            INSERT INTO 
                (id_candidato, id_empresa, id_vaga)
            VALUES (?, ?, ?)
        """

        try (
            def connection = ConnectionFactory.getConnection(sql)
            def statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setLong(1, match.idCandidato)
            statement.setLong(2, match.idEmpresa)
            statement.setLong(3, match.idVaga)

            def generatedKeys = statement.getGeneratedKeys()

            if (generatedKeys.next()) {
                match.id = generatedKeys.getLong(1)
            }

            return match
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
}
