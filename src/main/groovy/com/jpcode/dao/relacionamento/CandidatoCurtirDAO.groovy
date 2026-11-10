package com.jpcode.dao.relacionamento

import com.jpcode.dao.contratos.CandidatoCurtidasConsultaRepository
import com.jpcode.dao.contratos.CurtidaRepository
import com.jpcode.database.ConnectionFactory
import com.jpcode.dto.candidato.CandidatoAnonimoDTO
import com.jpcode.dto.vaga.VagaAnonimaDTO
import com.jpcode.model.referencia.Competencia

import java.sql.Connection

class CandidatoCurtirDAO implements
        CurtidaRepository<VagaAnonimaDTO>,
        CandidatoCurtidasConsultaRepository {

    private final ConnectionFactory connectionFactory

    CandidatoCurtirDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory
    }

    void salvar(Long idCandidato, Long idVaga) {
        String sql = """
            INSERT INTO vagas_curtidas_candidato
                (id_candidato, id_vaga)
            VALUES (?, ?)
            ON CONFLICT (id_candidato, id_vaga) DO NOTHING
        """

        if (idCandidato == null || idCandidato <= 0 || idVaga == null || idVaga <= 0) throw new IllegalArgumentException('Candidato e vaga obrigatórios e positivos.')
        try (def conexao = connectionFactory.getConnection()) {
            conexao.transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
            conexao.autoCommit = false
            try {
                Long empresa = consultarId(conexao, 'SELECT id_empresa FROM vagas WHERE id = ?', [idVaga])
                if (empresa == null || consultarId(conexao, 'SELECT id FROM empresas WHERE id = ? AND ativo = true FOR SHARE', [empresa]) == null ||
                        consultarId(conexao, 'SELECT id FROM candidatos WHERE id = ? AND ativo = true FOR SHARE', [idCandidato]) == null ||
                        consultarId(conexao, 'SELECT id FROM vagas WHERE id = ? AND id_empresa = ? FOR SHARE', [idVaga, empresa]) == null) {
                    throw new IllegalArgumentException('Candidato ou vaga indisponível.')
                }
                try (def comando = conexao.prepareStatement(sql)) {
                    comando.setLong(1, idCandidato)
                    comando.setLong(2, idVaga)
                    comando.executeUpdate()
                }
                conexao.commit()
            } catch (Exception erro) {
                conexao.rollback()
                throw erro
            }
        }
    }

    private static Long consultarId(Connection conexao, String sql, List<Long> ids) {
        try (def comando = conexao.prepareStatement(sql)) {
            ids.eachWithIndex { Long id, int indice -> comando.setLong(indice + 1, id) }
            try (def resultado = comando.executeQuery()) {
                return resultado.next() ? resultado.getLong(1) : null
            }
        }
    }

    List<VagaAnonimaDTO> buscarCurtidas(Long idCandidato) {
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
                def connection = connectionFactory.getConnection()
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

    List<Long> buscarIdsCandidatosRetribuidos(Connection conexao, Long idVaga) {
        String sql = '''
            SELECT r.id_candidato
            FROM candidatos_curtidos_empresa r
            JOIN candidatos c ON c.id = r.id_candidato
            WHERE r.id_vaga = ? AND c.ativo = true
            ORDER BY r.id_candidato
        '''
        try (def statement = conexao.prepareStatement(sql)) {
            statement.setLong(1, idVaga)
            try (def resultado = statement.executeQuery()) {
                List<Long> ids = []
                while (resultado.next()) ids.add(resultado.getLong('id_candidato'))
                return ids
            }
        }
    }

    List<CandidatoAnonimoDTO> buscarCandidatosQueCurtiram(Connection conexao, Long idVaga) {
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
                def statement = conexao.prepareStatement(sql)
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
