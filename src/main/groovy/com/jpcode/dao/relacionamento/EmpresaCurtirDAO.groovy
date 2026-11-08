package com.jpcode.dao.relacionamento

import com.jpcode.dao.contratos.RetribuicaoRepository
import com.jpcode.database.ConnectionFactory
import com.jpcode.dto.candidato.CandidatoAnonimoDTO
import com.jpcode.model.referencia.Competencia

import com.jpcode.model.core.Vaga
import java.sql.Connection
import java.sql.Types

class EmpresaCurtirDAO implements RetribuicaoRepository {

    private final ConnectionFactory connectionFactory

    EmpresaCurtirDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory
    }

    Long buscarEmpresaDaVaga(Connection conexao, Long idVaga) {
        return consultarId(conexao, 'SELECT id_empresa FROM vagas WHERE id = ?', [idVaga])
    }

    boolean bloquearEmpresaAtiva(Connection conexao, Long idEmpresa) {
        return consultarId(conexao, 'SELECT id FROM empresas WHERE id = ? AND ativo = true FOR SHARE', [idEmpresa]) != null
    }

    Long bloquearRecrutadorAtivo(Connection conexao, Long idRecrutador) {
        return consultarId(conexao, 'SELECT id_empresa FROM recrutadores WHERE id = ? AND ativo = true FOR SHARE', [idRecrutador])
    }

    boolean bloquearCandidatoAtivo(Connection conexao, Long idCandidato) {
        return consultarId(conexao, 'SELECT id FROM candidatos WHERE id = ? AND ativo = true FOR SHARE', [idCandidato]) != null
    }

    Vaga bloquearVaga(Connection conexao, Long idVaga) {
        try (def comando = conexao.prepareStatement('SELECT id_empresa, id_recrutador FROM vagas WHERE id = ? FOR UPDATE')) {
            comando.setLong(1, idVaga)
            try (def resultado = comando.executeQuery()) {
                if (!resultado.next()) return null
                Vaga vaga = new Vaga(idVaga, null, null, null, resultado.getLong('id_empresa'))
                long autor = resultado.getLong('id_recrutador')
                vaga.idRecrutador = resultado.wasNull() ? null : autor
                return vaga
            }
        }
    }

    boolean bloquearInteresse(Connection conexao, Long idCandidato, Long idVaga) {
        return consultarId(conexao, 'SELECT id FROM vagas_curtidas_candidato WHERE id_candidato = ? AND id_vaga = ? FOR SHARE', [idCandidato, idVaga]) != null
    }

    private static Long consultarId(Connection conexao, String sql, List<Long> ids) {
        try (def comando = conexao.prepareStatement(sql)) {
            ids.eachWithIndex { Long id, int indice -> comando.setLong(indice + 1, id) }
            try (def resultado = comando.executeQuery()) {
                return resultado.next() ? resultado.getLong(1) : null
            }
        }
    }

    void salvar(Connection conexao, Long idEmpresa, Long idCandidato, Long idVaga, Long idRecrutadorAtor) {
        if (conexao.autoCommit) throw new IllegalStateException('Retribuição exige transação explícita.')
        String sql = """
            INSERT INTO candidatos_curtidos_empresa
                (id_empresa, id_candidato, id_vaga, id_recrutador_ator)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (id_candidato, id_vaga) DO NOTHING
        """

        try (
                def statement = conexao.prepareStatement(sql)
        ) {
            statement.setLong(1, idEmpresa)
            statement.setLong(2, idCandidato)
            statement.setLong(3, idVaga)
            if (idRecrutadorAtor == null) statement.setNull(4, Types.INTEGER)
            else statement.setLong(4, idRecrutadorAtor)

            statement.executeUpdate()
        }
        Long empresaPersistida = consultarId(conexao,
                'SELECT id_empresa FROM candidatos_curtidos_empresa WHERE id_candidato = ? AND id_vaga = ?', [idCandidato, idVaga])
        if (empresaPersistida != idEmpresa) throw new IllegalStateException('Contexto de retribuição incompatível.')
    }

    List<CandidatoAnonimoDTO> buscarCurtidas(Long idEmpresa) {
        String sql = """
        SELECT DISTINCT
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
}
