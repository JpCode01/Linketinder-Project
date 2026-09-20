package com.jpcode.dao.relacionamento

import com.jpcode.database.ConnectionFactory
import com.jpcode.dto.candidato.CandidatoAnonimoDTO
import com.jpcode.model.referencia.Competencia

import java.sql.Statement

class EmpresaCurtirDAO {
    void salvar(Long idEmpresa, Long idCandidato) {
       String sql = """
            INSERT INTO candidatos_curtidos_empresa
                (id_empresa, id_candidato)
            VALUES (?, ?)
        """

        try (
            def connection = ConnectionFactory.getConnection(sql)
            def statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setLong(1, idEmpresa)
            statement.setLong(2, idCandidato)

            statement.executeUpdate()
        }
    }

    List<CandidatoAnonimoDTO> buscarCandidatosCurtidos(Long idEmpresa) {
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
            def connection = ConnectionFactory.getConnection()
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
}
