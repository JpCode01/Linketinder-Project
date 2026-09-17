package com.jpcode.dao

import com.jpcode.database.ConnectionFactory
import com.jpcode.model.Candidato
import com.jpcode.model.Vaga

import java.sql.Statement

class CandidatoCurtirDAO {
    void salvar(Candidato candidato, Vaga vaga) {
        String sql = """
            INSERT INTO vagas_curtidas_candidato
                (id_candidato, id_vaga)
            VALUES (?, ?)
        """

        try (
            def connection = ConnectionFactory.getConnection(sql)
            def statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setLong(1, candidato.id)
            statement.setLong(2, vaga.id)

            statement.executeUpdate()
        }
    }
}
