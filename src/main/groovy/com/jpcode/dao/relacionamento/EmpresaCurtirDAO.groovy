package com.jpcode.dao.relacionamento

import com.jpcode.dao.contratos.CurtidaRepository
import com.jpcode.database.ConnectionFactory
import com.jpcode.dto.candidato.CandidatoAnonimoDTO
import com.jpcode.model.referencia.Competencia

import java.sql.Statement

class EmpresaCurtirDAO implements CurtidaRepository<CandidatoAnonimoDTO> {

    private final ConnectionFactory connectionFactory

    EmpresaCurtirDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory
    }

    void salvar(Long idEmpresa, Long idCandidato) {
        String sql = """
            INSERT INTO candidatos_curtidos_empresa
                (id_empresa, id_candidato)
            VALUES (?, ?)
            ON CONFLICT (id_empresa, id_candidato) DO NOTHING
        """

        try (
                def connection = connectionFactory.getConnection()
                def statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setLong(1, idEmpresa)
            statement.setLong(2, idCandidato)

            statement.executeUpdate()
        }
    }

    List<CandidatoAnonimoDTO> buscarCurtidas(Long idEmpresa) {
        String sql = """
        SELECT
            c.id,
            c.descricao,
            comp.id AS competencia_id,
            comp.nome_competencia
        FROM candidatos c
        JOIN candidatos_curtidos_empresa cce
            ON cce.id_candidato = c.id
        LEFT JOIN candidatos_competencias cc
            ON cc.id_candidato = c.id
        LEFT JOIN competencias comp
            ON comp.id = cc.id_competencia
        WHERE cce.id_empresa = ?
          AND c.ativo = true
        ORDER BY c.id
        """

        try (
                def connection = connectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, idEmpresa)

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

    boolean jaCurtiu(Long idEmpresa, Long idCandidato) {
        String sql = """
        SELECT EXISTS (
            SELECT 1
            FROM candidatos_curtidos_empresa
            WHERE id_empresa = ?
              AND id_candidato = ?
        )
    """

        try (
                def connection = connectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, idEmpresa)
            statement.setLong(2, idCandidato)

            def resultSet = statement.executeQuery()

            resultSet.next()
            return resultSet.getBoolean(1)
        }
    }
}
