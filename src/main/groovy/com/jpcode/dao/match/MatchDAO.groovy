package com.jpcode.dao.match


import com.jpcode.dao.contratos.CompetenciaRepository
import com.jpcode.dao.contratos.MatchRepository
import com.jpcode.database.ConnectionFactory
import com.jpcode.dto.Match.MatchEncontradoDTO
import com.jpcode.model.referencia.Competencia

import java.sql.Connection

class MatchDAO implements MatchRepository {
    
    private final ConnectionFactory connectionFactory
    final CompetenciaRepository competenciasCandidatoRepository
    final CompetenciaRepository competenciasVagaRepository

    MatchDAO(ConnectionFactory connectionFactory, CompetenciaRepository competenciasCandidatoRepository, CompetenciaRepository competenciasVagaRepository) {
        this.connectionFactory = connectionFactory
        this.competenciasCandidatoRepository = competenciasCandidatoRepository
        this.competenciasVagaRepository = competenciasVagaRepository
    }

    void salvar(Connection conexao, Long idCandidato, Long idEmpresa, Long idVaga) {
        if (conexao.autoCommit) throw new IllegalStateException('Match exige transação explícita.')
        String sql = """
            INSERT INTO "match"
                (id_candidato, id_empresa, id_vaga)
            VALUES (?, ?, ?)
            ON CONFLICT (id_candidato, id_vaga) DO NOTHING
        """

        try (
                def statement = conexao.prepareStatement(sql)
        ) {
            statement.setLong(1, idCandidato)
            statement.setLong(2, idEmpresa)
            statement.setLong(3, idVaga)

            statement.executeUpdate()
        }
        try (def comando = conexao.prepareStatement('SELECT id_empresa FROM match WHERE id_candidato = ? AND id_vaga = ?')) {
            comando.setLong(1, idCandidato)
            comando.setLong(2, idVaga)
            try (def resultado = comando.executeQuery()) {
                if (!resultado.next() || resultado.getLong(1) != idEmpresa) {
                    throw new IllegalStateException('Contexto de match incompatível.')
                }
            }
        }
    }

    List<MatchEncontradoDTO> buscarMatchesPorEmpresa(Long idEmpresa) {
        return consultarMatches('m.id_empresa = ? AND e.ativo = true', idEmpresa)
    }

    List<MatchEncontradoDTO> buscarMatchesPorCandidato(Long idCandidato) {
        return consultarMatches('m.id_candidato = ? AND c.ativo = true', idCandidato)
    }

    List<MatchEncontradoDTO> buscarMatchesPorRecrutador(Long idRecrutador) {
        return consultarMatches('v.id_recrutador = ? AND r.ativo = true AND e.ativo = true', idRecrutador)
    }

    // Filtros definidos exclusivamente pelos métodos de cada ator, nunca pela requisição.
    private List<MatchEncontradoDTO> consultarMatches(String filtro, Long idTitular) {

        String sql = """
        SELECT
            m.id,
            m.id_candidato,
            m.id_vaga,

            c.nome AS nome_candidato,
            c.sobrenome AS sobrenome_candidato,
            c.email AS email_candidato,
            c.cpf AS cpf_candidato,
            c.descricao AS descricao_candidato,
            c.idade AS idade_candidato,
            estado_candidato.sigla AS estado_candidato,
            pais_candidato.nome AS pais_candidato,

            e.nome AS nome_empresa,
            e.cnpj AS cnpj_empresa,
            e.email_corporativo,
            e.descricao AS descricao_empresa,
            estado_empresa.sigla AS estado_empresa,
            pais_empresa.nome AS pais_empresa,

            v.nome AS nome_vaga,
            v.descricao AS descricao_vaga,
            r.nome AS nome_recrutador,
            area.nome AS area_recrutamento

        FROM "match" m

        JOIN candidatos c
            ON c.id = m.id_candidato

        JOIN empresas e
            ON e.id = m.id_empresa

        JOIN vagas v
            ON v.id = m.id_vaga

        LEFT JOIN recrutadores r
            ON r.id = v.id_recrutador AND r.id_empresa = v.id_empresa

        LEFT JOIN areas_recrutamento area
            ON area.id = r.id_area_recrutamento

        LEFT JOIN estados estado_candidato
            ON estado_candidato.id = c.id_estado

        LEFT JOIN pais pais_candidato
            ON pais_candidato.id = c.id_pais

        LEFT JOIN estados estado_empresa
            ON estado_empresa.id = e.id_estado

        LEFT JOIN pais pais_empresa
            ON pais_empresa.id = e.id_pais

        WHERE ${filtro}
        ORDER BY m.id
    """

        try (
                def connection = connectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {

            statement.setLong(1, idTitular)

            try (def resultSet = statement.executeQuery()) {

                List<MatchEncontradoDTO> matches = []

                while (resultSet.next()) {

                    Long idCandidato = resultSet.getLong("id_candidato")
                    Long idVaga = resultSet.getLong("id_vaga")

                    List<Competencia> competenciasCandidato =
                            competenciasCandidatoRepository.buscarPorId(idCandidato)

                    List<String> competenciasVaga =
                            competenciasVagaRepository.buscarEmString(idVaga)

                    matches.add(
                            new MatchEncontradoDTO(
                                    resultSet.getLong("id"),

                                    resultSet.getString("nome_candidato"),
                                    resultSet.getString("sobrenome_candidato"),
                                    resultSet.getString("email_candidato"),
                                    resultSet.getString("cpf_candidato"),
                                    resultSet.getString("descricao_candidato"),
                                    resultSet.getInt("idade_candidato"),
                                    resultSet.getString("estado_candidato"),
                                    resultSet.getString("pais_candidato"),
                                    competenciasCandidato,

                                    resultSet.getString("nome_empresa"),
                                    resultSet.getString("cnpj_empresa"),
                                    resultSet.getString("email_corporativo"),
                                    resultSet.getString("descricao_empresa"),
                                    resultSet.getString("pais_empresa"),
                                    resultSet.getString("estado_empresa"),

                                    resultSet.getString("nome_vaga"),
                                    resultSet.getString("descricao_vaga"),
                                    competenciasVaga,
                                    resultSet.getString("nome_recrutador"),
                                    resultSet.getString("area_recrutamento")
                            )
                    )
                }

                return matches
            }
        }
    }

}
