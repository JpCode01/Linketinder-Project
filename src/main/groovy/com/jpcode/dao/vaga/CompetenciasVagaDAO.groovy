package com.jpcode.dao.vaga

import com.jpcode.database.ConnectionFactory
import com.jpcode.model.core.Vaga
import com.jpcode.model.referencia.Competencia

import java.sql.Statement

class CompetenciasVagaDAO {
    void salvar(Vaga vaga, Competencia competencia) {
        String sql = """
            INSERT INTO vagas_competencias 
                (id_vaga, id_competencia)
            VALUES (?, ?)
        """

        try (
            def connection = ConnectionFactory.getConnection(sql)
            def statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setLong(1, vaga.id)
            statement.setLong(2, competencia.id)

            statement.executeUpdate()
        }
    }
}
