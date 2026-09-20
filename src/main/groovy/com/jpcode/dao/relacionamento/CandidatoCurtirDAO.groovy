package com.jpcode.dao.relacionamento

import com.jpcode.database.ConnectionFactory
import com.jpcode.dto.candidato.CandidatoAnonimoDTO
import com.jpcode.dto.vaga.VagaAnonimaDTO
import com.jpcode.model.core.Vaga
import com.jpcode.model.referencia.Competencia

import java.sql.Statement

class CandidatoCurtirDAO {
    void salvar(Long idCandidato, Vaga vaga) {
        String sql = """
            INSERT INTO vagas_curtidas_candidato
                (id_candidato, id_vaga)
            VALUES (?, ?)
        """

        try (
            def connection = ConnectionFactory.getConnection(sql)
            def statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setLong(1, idCandidato)
            statement.setLong(2, vaga.id)

            statement.executeUpdate()
        }
    }

    List<VagaAnonimaDTO> buscarVagasCurtidas(Long idCandidato) {
        String sql = """
        SELECT
            v.id,
            v.descricao,
            comp.id AS competencia_id,
            comp.nome_competencia
        FROM vagas v
        JOIN vagas_curtidas_candidato vcc
            ON vcc.id_vaga = v.id
        LEFT JOIN vagas_competencias vc
            ON vc.id_vaga = v.id
        LEFT JOIN competencias comp
            ON comp.id = vc.id_competencia
        WHERE vcc.id_candidato = ?
        ORDER BY v.id
        """

        try (
                def connection = ConnectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, idCandidato)

            def resultSet = statement.executeQuery()

            List<VagaAnonimaDTO> vagas = []

            VagaAnonimaDTO vagaAtual = null
            List<Competencia> competencias = []

            while (resultSet.next()) {
                Long idVaga = resultSet.getLong("id")

                if (vagaAtual == null || vagaAtual.id != idVaga) {

                    if (vagaAtual != null) {
                        vagas.add(vagaAtual)
                    }

                    competencias = []

                    vagaAtual = new VagaAnonimaDTO(
                            idVaga,
                            resultSet.getString("descricao"),
                            competencias
                    )
                }

                Long competenciaId = resultSet.getLong("competencia_id")

                if (competenciaId != 0) {
                    competencias.add(
                            new Competencia(
                                    competenciaId,
                                    resultSet.getString("nome_competencia")
                            )
                    )
                }
            }

            if (vagaAtual != null) {
                vagas.add(vagaAtual)
            }

            return vagas
        }
    }

    List<CandidatoAnonimoDTO> buscarCandidatosQueCurtiram(Long idVaga) {
        String sql = """
        SELECT
            c.id,
            c.descricao,
            comp.id AS competencia_id,
            comp.nome_competencia
        FROM candidatos c
        JOIN vagas_curtidas_candidato vcc
            ON vcc.id_candidato = c.id
        LEFT JOIN candidatos_competencias cc
            ON cc.id_candidato = c.id
        LEFT JOIN competencias comp
            ON comp.id = cc.id_competencia
        WHERE vcc.id_vaga = ?
          AND c.ativo = true
        ORDER BY c.id
        """

        try (
            def connection = ConnectionFactory.getConnection()
            def statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, idVaga)

            def resultSet = statement.executeQuery()

            List<CandidatoAnonimoDTO> candidatos = []

            CandidatoAnonimoDTO candidatoAtual = null
            List<Competencia> competencias = []

            while (resultSet.next()) {
                Long idCandidato = resultSet.getLong("id")

                if (candidatoAtual == null || candidatoAtual.id != idCandidato) {

                    if (candidatoAtual != null) {
                        candidatos.add(candidatoAtual)
                    }

                    competencias = []

                    candidatoAtual = new CandidatoAnonimoDTO(
                            idCandidato,
                            resultSet.getString("descricao"),
                            competencias
                    )


                }

                Long competenciaId = resultSet.getLong("competencia_id")

                if (competenciaId != 0) {
                    competencias.add(
                            new Competencia(
                                    competenciaId,
                                    resultSet.getString("nome_competencia")
                            )
                    )
                }

            }

            if (candidatoAtual != null) {
                candidatos.add(candidatoAtual)
            }

            return candidatos

        }
    }
}
