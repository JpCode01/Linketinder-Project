package com.jpcode.dao.candidato

import com.jpcode.database.ConnectionFactory
import com.jpcode.model.core.Candidato
import com.jpcode.model.referencia.Competencia

import java.sql.Statement

class CompetenciasCandidatoDAO {
    void salvar(Candidato candidato, Competencia competencia) {
        String sql = """
            INSERT INTO candidatos_competencias
                (id_candidato, id_competencia)
            VALUES (?, ?)
        """

        try (
            def connection = ConnectionFactory.getConnection()
            def statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, candidato.id)
            statement.setLong(2, competencia.id)

            statement.executeUpdate()
        }
    }

    List<Competencia> buscarPorCandidato(Long idCandidato) {
        String sql = """
        SELECT c.id, c.nome_competencia
        FROM competencias c
        JOIN candidatos_competencias cc
        ON cc.id_competencia = c.id
        WHERE cc.id_candidato = ?
        """

        try (
                def connection = ConnectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, idCandidato)

            def resultSet = statement.executeQuery()

            List<Competencia> competencias = []

            while (resultSet.next()) {
                competencias.add(
                        new Competencia(
                                resultSet.getLong("id"),
                                resultSet.getString("nome_competencia")
                        )
                )
            }

            return competencias
        }
    }

}
