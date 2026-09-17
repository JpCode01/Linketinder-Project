package com.jpcode.dao

import com.jpcode.database.ConnectionFactory
import com.jpcode.model.Candidato
import com.jpcode.model.Empresa

import java.sql.Statement

class EmpresaCurtirDAO {
    void salvar(Empresa empresa, Candidato candidato) {
       String sql = """
            INSERT INTO candidatos_curtidos_empresa
                (id_empresa, id_candidato)
            VALUES (?, ?)
        """

        try (
            def connection = ConnectionFactory.getConnection(sql)
            def statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setLong(1, empresa.id)
            statement.setLong(2, candidato.id)

            statement.executeUpdate()
        }
    }
}
