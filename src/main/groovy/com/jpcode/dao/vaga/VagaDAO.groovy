package com.jpcode.dao.vaga

import com.jpcode.dao.contratos.VagaRepository
import com.jpcode.database.ConnectionFactory
import com.jpcode.dto.vaga.VagaAnonimaDTO
import com.jpcode.dto.vaga.VagaEmpresaDTO
import com.jpcode.model.core.Vaga
import com.jpcode.model.referencia.Competencia

import java.sql.Statement
import java.sql.Connection
import java.sql.Types

class VagaDAO implements VagaRepository {

    private final ConnectionFactory connectionFactory
    private final CompetenciasVagaDAO competenciasVagaDAO

    VagaDAO(ConnectionFactory connectionFactory, CompetenciasVagaDAO competenciasVagaDAO = new CompetenciasVagaDAO(connectionFactory)) {
        this.connectionFactory = connectionFactory
        this.competenciasVagaDAO = competenciasVagaDAO
    }

    Vaga salvarComCompetencias(Vaga vaga, List<Long> idsCompetencias) {
        try (def connection = connectionFactory.getConnection()) {
            connection.autoCommit = false
            try {
                validarAtor(connection, 'empresas', vaga.idEmpresa, null)
                if (vaga.idRecrutador != null) {
                    validarAtor(connection, 'recrutadores', vaga.idRecrutador, vaga.idEmpresa)
                }
                Long id = inserir(connection, vaga)
                idsCompetencias.each { competenciasVagaDAO.salvar(connection, id, it) }
                connection.commit()
                vaga.id = id
                return vaga
            } catch (Exception erro) {
                connection.rollback()
                throw erro
            }
        }
    }

    private static void validarAtor(Connection connection, String tabela, Long id, Long idEmpresa) {
        String sql = "SELECT id FROM ${tabela} WHERE id = ? AND ativo = true" +
                (idEmpresa == null ? '' : ' AND id_empresa = ?') + ' FOR SHARE'
        try (def statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id)
            if (idEmpresa != null) statement.setLong(2, idEmpresa)
            try (def resultado = statement.executeQuery()) {
                if (!resultado.next()) throw new IllegalArgumentException('Conta inativa, inexistente ou vínculo incompatível.')
            }
        }
    }

    private static Long inserir(Connection connection, Vaga vaga) {
        String sql = """
            INSERT INTO vagas           
                (nome, descricao, local, id_empresa, id_recrutador)
            VALUES (?, ?, ?, ?, ?)
        """

        try (
                def statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setString(1, vaga.nome)
            statement.setString(2, vaga.descricao)
            statement.setString(3, vaga.local)
            statement.setLong(4, vaga.idEmpresa)
            if (vaga.idRecrutador == null) statement.setNull(5, Types.INTEGER)
            else statement.setLong(5, vaga.idRecrutador)

            statement.executeUpdate()

            try (def generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) throw new IllegalStateException('Vaga não gravada.')
                return generatedKeys.getLong(1)
            }
        }


    }

    Optional<Vaga> buscarPorId(Long id) {
        String sql = """
            SELECT id, nome, descricao, local, id_empresa, id_recrutador
            FROM vagas
            WHERE id = ? 
        """

        try (
                def connection = connectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, id)

            def resultSet = statement.executeQuery()

            if (!resultSet.next()) {
                return Optional.empty()
            }

            Vaga vaga = new Vaga(
                    resultSet.getLong("id"),
                    resultSet.getString("nome"),
                    resultSet.getString("descricao"),
                    resultSet.getString("local"),
                    resultSet.getLong("id_empresa")
            )
            long idRecrutador = resultSet.getLong('id_recrutador')
            vaga.idRecrutador = resultSet.wasNull() ? null : idRecrutador

            return Optional.of(vaga)

        }
    }

    List<VagaEmpresaDTO> buscarVagasEmpresa(Long idEmpresa) {
        return buscarVagasInternas(idEmpresa, false)
    }

    List<VagaEmpresaDTO> buscarVagasRecrutador(Long idRecrutador) {
        return buscarVagasInternas(idRecrutador, true)
    }

    private List<VagaEmpresaDTO> buscarVagasInternas(Long idAtor, boolean porAutor) {
        String sql = """
            SELECT
                v.id,
                v.nome,
                v.descricao,
                v.local,
                v.id_empresa,
                v.id_recrutador,
                r.nome AS nome_recrutador,
                ar.nome AS area_recrutamento,
                (
                    SELECT COUNT(*)
                    FROM vagas_curtidas_candidato vcc
                    WHERE vcc.id_vaga = v.id
                ) AS quantidade_candidatos,
                c.id AS competencia_id,
                c.nome_competencia
            FROM vagas v
            JOIN empresas e
                ON e.id = v.id_empresa
            LEFT JOIN recrutadores r ON r.id = v.id_recrutador
            LEFT JOIN areas_recrutamento ar ON ar.id = r.id_area_recrutamento
            LEFT JOIN vagas_competencias vc
                ON vc.id_vaga = v.id
            LEFT JOIN competencias c
                ON c.id = vc.id_competencia
            WHERE ${porAutor ? 'v.id_recrutador' : 'v.id_empresa'} = ?
              AND e.ativo = true
              ${porAutor ? 'AND r.ativo = true' : ''}
            ORDER BY v.id
            """

        try (
                def connection = connectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, idAtor)

            def resultSet = statement.executeQuery()

            List<VagaEmpresaDTO> vagas = []

            VagaEmpresaDTO vagaAtual = null
            List<Competencia> competencias = []

            while (resultSet.next()) {

                Long idVaga = resultSet.getLong("id")

                if (vagaAtual == null || vagaAtual.id != idVaga) {

                    if (vagaAtual != null) {
                        vagas.add(vagaAtual)
                    }

                    competencias = []

                    vagaAtual = new VagaEmpresaDTO(
                            idVaga,
                            resultSet.getString("nome"),
                            resultSet.getString("descricao"),
                            resultSet.getString("local"),
                            resultSet.getInt("quantidade_candidatos"),
                            competencias
                    )
                    long idRecrutador = resultSet.getLong('id_recrutador')
                    vagaAtual.idRecrutador = resultSet.wasNull() ? null : idRecrutador
                    vagaAtual.nomeRecrutador = resultSet.getString('nome_recrutador')
                    vagaAtual.areaRecrutamento = resultSet.getString('area_recrutamento')
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


    List<VagaAnonimaDTO> buscarTodasAsVagas() {
        String sql = """
        SELECT
            v.id,
            v.descricao,
            comp.id AS competencia_id,
            comp.nome_competencia
        FROM vagas v
        LEFT JOIN vagas_competencias vc
            ON vc.id_vaga = v.id
        LEFT JOIN competencias comp
            ON comp.id = vc.id_competencia
        ORDER BY v.id
    """

        try (
                def connection = connectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            def resultSet = statement.executeQuery()

            List<VagaAnonimaDTO> vagas = []
            VagaAnonimaDTO vagaAtual = null
            Set<Long> competenciasAdicionadas = []

            while (resultSet.next()) {
                Long idVaga = resultSet.getLong("id")

                if (vagaAtual == null || vagaAtual.id != idVaga) {
                    if (vagaAtual != null) {
                        vagas.add(vagaAtual)
                    }

                    competenciasAdicionadas = []

                    vagaAtual = new VagaAnonimaDTO(
                            idVaga,
                            resultSet.getString("descricao"),
                            []
                    )
                }

                Long competenciaId = resultSet.getLong("competencia_id")
                boolean competenciaNula = resultSet.wasNull()

                if (!competenciaNula &&
                        !competenciasAdicionadas.contains(competenciaId)) {

                    vagaAtual.competencias.add(
                            new Competencia(
                                    competenciaId,
                                    resultSet.getString("nome_competencia")
                            )
                    )

                    competenciasAdicionadas.add(competenciaId)
                }
            }

            if (vagaAtual != null) {
                vagas.add(vagaAtual)
            }

            return vagas
        }
    }


    void deletar(Long id) {
        try (Connection conexao = connectionFactory.getConnection()) {
            conexao.autoCommit = false
            try {
                try (def comando = conexao.prepareStatement('SELECT id FROM vagas WHERE id = ? FOR UPDATE')) {
                    comando.setLong(1, id)
                    comando.executeQuery().close()
                }
                for (String sql : ['DELETE FROM match WHERE id_vaga = ?',
                                   'DELETE FROM candidatos_curtidos_empresa WHERE id_vaga = ?',
                                   'DELETE FROM vagas WHERE id = ?']) {
                    try (def comando = conexao.prepareStatement(sql)) {
                        comando.setLong(1, id)
                        comando.executeUpdate()
                    }
                }
                conexao.commit()
            } catch (Exception erro) {
                conexao.rollback()
                throw erro
            }
        }
    }

    Vaga atualizarDados(Vaga vaga) {
        String sql = """
        UPDATE vagas
        SET nome = ?,
            descricao = ?,
            "local" = ?
        WHERE id = ? AND id_empresa = ?
          AND EXISTS (SELECT 1 FROM empresas e WHERE e.id = vagas.id_empresa AND e.ativo = true)
        """

        try (
                def connection = connectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, vaga.nome)
            statement.setString(2, vaga.descricao)
            statement.setString(3, vaga.local)
            statement.setLong(4, vaga.id)
            statement.setLong(5, vaga.idEmpresa)

            if (statement.executeUpdate() != 1) {
                throw new IllegalArgumentException('Vaga fora do escopo da empresa ativa.')
            }

        }

        return vaga
    }
}
