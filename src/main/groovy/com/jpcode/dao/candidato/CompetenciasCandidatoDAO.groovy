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
            def connection = ConnectionFactory.getConnection(sql)
            def statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setLong(1, candidato.id)
            statement.setLong(2, competencia.id)

            statement.executeUpdate()
        }

    }
}
