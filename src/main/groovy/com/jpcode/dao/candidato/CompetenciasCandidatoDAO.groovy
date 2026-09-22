package com.jpcode.dao.candidato

import com.jpcode.database.ConnectionFactory
import com.jpcode.dto.competencia.RemoverCompetenciaDTO
import com.jpcode.model.referencia.Competencia

class CompetenciasCandidatoDAO {
    void salvar(Long idCandidato, Long idCompetencia) {
        String sql = """
            INSERT INTO candidatos_competencias
                (id_candidato, id_competencia)
            VALUES (?, ?)
            ON CONFLICT (id_candidato, id_competencia) DO NOTHING
        """

        try (
            def connection = ConnectionFactory.getConnection()
            def statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, idCandidato)
            statement.setLong(2, idCompetencia)

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

    List<String> buscarPorCandidatoString(Long idCandidato) {
        String sql = """
        SELECT c.nome_competencia
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

            List<String> competencias = []

            while (resultSet.next()) {
                competencias.add(
                            resultSet.getString("nome_competencia")
                )
            }

            return competencias
        }
    }

    List<RemoverCompetenciaDTO> converterCompetenciasParaDTO(List<Competencia> competencias) {
        List<RemoverCompetenciaDTO> competenciasConvertidas = []
        competencias.each {competencia ->
            competenciasConvertidas.add(
                    new RemoverCompetenciaDTO(competencia.id,
                                            competencia.nome
            ))
        }

        return competenciasConvertidas
    }


    void removerCompetencia(Long idCandidato, Long idCompetencia) {
        String sql = """
        DELETE FROM candidatos_competencias
        WHERE  id_candidato = ?
        AND id_competencia = ?
        """

        try (
            def connection = ConnectionFactory.getConnection()
            def statement =  connection.prepareStatement(sql)
        ) {
            statement.setLong(1, idCandidato)
            statement.setLong(2, idCompetencia)

            statement.executeUpdate()

            
        }
    }

    void atualizar(Long idCandidato, List<Competencia> competencias) {
        String sql = """
        INSERT INTO candidatos_competencias
            (id_candidato, id_competencia)
        VALUES (?, ?)
        ON CONFLICT (id_candidato, id_competencia) DO NOTHING
    """

        try (
                def connection = ConnectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            competencias.each { competencia ->
                statement.setLong(1, idCandidato)
                statement.setLong(2, competencia.id)

                statement.executeUpdate()
            }
        }
    }
}
