package com.jpcode.dao.vaga

import com.jpcode.database.ConnectionFactory
import com.jpcode.model.referencia.Competencia

import java.sql.Statement

class CompetenciasVagaDAO {
    void salvar(Long idVaga, Long idCompetencia) {
        String sql = """
            INSERT INTO vagas_competencias 
                (id_vaga, id_competencia)
            VALUES (?, ?)
            ON CONFLICT (id_vaga, id_competencia) DO NOTHING
        """

        try (
            def connection = ConnectionFactory.getConnection()
            def statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setLong(1,  idVaga)
            statement.setLong(2, idCompetencia)

            statement.executeUpdate()
        }
    }

    List<Competencia> buscarPorVaga(Long idVaga) {
        String sql = """
            SELECT c.id, c.nome_competencia
            FROM competencias c
            JOIN vagas_competencias vc
            ON vc.id_competencia = c.id
            WHERE vc.id_vaga = ?
        """

        try (
            def connection = ConnectionFactory.getConnection()
            def statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, idVaga)

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

    List<String> buscarPorVagaString(Long idVaga) {
        String sql = """
        SELECT c.nome_competencia
        FROM competencias c
        JOIN vagas_competencias vc
        ON vc.id_competencia = c.id
        WHERE vc.id_vaga = ?
        """

        try (
                def connection = ConnectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, idVaga)

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

    void atualizar(Long idVaga, List<Competencia> competencias) {
        String sql = """
        INSERT INTO vagas_competencias
            (id_vaga, id_competencia)
        VALUES (?, ?)
        ON CONFLICT (id_vaga, id_competencia) DO NOTHING
    """

        try (
                def connection = ConnectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            competencias.each { competencia ->
                statement.setLong(1, idVaga)
                statement.setLong(2, competencia.id)

                statement.executeUpdate()
            }
        }
    }

    void removerCompetencia(Long idVaga, Long idCompetencia) {
        String sql = """
        DELETE FROM vagas_competencias
        WHERE  id_vaga = ?
        AND id_competencia = ?
        """

        try (
                def connection = ConnectionFactory.getConnection()
                def statement =  connection.prepareStatement(sql)
        ) {
            statement.setLong(1, idVaga)
            statement.setLong(2, idCompetencia)

            statement.executeUpdate()


        }
    }
}
