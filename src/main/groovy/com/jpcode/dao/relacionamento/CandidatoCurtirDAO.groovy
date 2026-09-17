package com.jpcode.dao.relacionamento

import com.jpcode.database.ConnectionFactory
import com.jpcode.model.core.Candidato
import com.jpcode.model.core.Vaga

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

    List<Vaga> buscarVagasCurtidas(Long idCandidato) {
        String sql = """
            SELECT v.id, v.nome, v.descricao,
                v.local
            FROM vagas v
            JOIN vagas_curtidas_candidato vcc
            ON vcc.id_vaga = v.id
            WHERE vcc.id_candidato = ? 
        """

        try (
            def connection = ConnectionFactory.getConnection()
            def statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, idCandidato)

            def resultSet = statement.executeQuery()

            List<Vaga> vagas = []

            while (resultSet.next()) {
                vagas.add(
                        new Vaga(
                                resultSet.getLong("id"),
                                resultSet.getString("nome"),
                                resultSet.getString("descricao"),
                                resultSet.getString("local"),
                                null
                        )
                )
            }

            return vagas
        }
    }
    
}
