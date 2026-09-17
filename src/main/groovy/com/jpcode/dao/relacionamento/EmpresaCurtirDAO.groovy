package com.jpcode.dao.relacionamento

import com.jpcode.database.ConnectionFactory
import com.jpcode.model.core.Candidato
import com.jpcode.model.core.Empresa

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

    List<Candidato> buscarCandidatosCurtidos(Long idEmpresa) {
        String sql = """
            SELECT c.id, c.descricao, c.ativo
            FROM candidatos c
            JOIN candidatos_curtidos_empresa cce
            ON cce.id_candidato = c.id
            WHERE cce.id_empresa = ?
        """

        try (
            def connection = ConnectionFactory.getConnection()
            def statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, idEmpresa)

            def resultSet = statement.executeQuery()

            List<Candidato> candidatos = []

            

                while (resultSet.next()) {
                    if (resultSet.getBoolean("ativo")) {
                        candidatos.add(
                                new Candidato(
                                        resultSet.getLong("id"),
                                        "Candidato",
                                        "Anônimo",
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        -1,
                                        null,
                                        null,
                                        resultSet.getString("descricao"),
                                        resultSet.getBoolean("ativo")
                                )
                        )
                    }

                    
                }
            return candidatos
        }

    }
}
