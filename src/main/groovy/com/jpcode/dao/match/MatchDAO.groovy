package com.jpcode.dao.match

import com.jpcode.dao.candidato.CompetenciasCandidatoDAO
import com.jpcode.dao.vaga.CompetenciasVagaDAO
import com.jpcode.database.ConnectionFactory
import com.jpcode.dto.Match.MatchEncontradoDTO
import com.jpcode.model.core.Match
import com.jpcode.model.referencia.Competencia

class MatchDAO {

    final CompetenciasCandidatoDAO competenciasCandidatoDAO
    final CompetenciasVagaDAO competenciasVagaDAO

    MatchDAO(CompetenciasCandidatoDAO competenciasCandidatoDAO, CompetenciasVagaDAO competenciasVagaDAO) {
        this.competenciasCandidatoDAO = competenciasCandidatoDAO
        this.competenciasVagaDAO = competenciasVagaDAO
    }

    void salvar(Long idCandidato, Long idEmpresa, Long idVaga) {
        String sql = """
            INSERT INTO "match"
                (id_candidato, id_empresa, id_vaga)
            VALUES (?, ?, ?)
            ON CONFLICT (id_candidato, id_empresa, id_vaga) DO NOTHING
        """

        try (
            def connection = ConnectionFactory.getConnection()
            def statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, idCandidato)
            statement.setLong(2, idEmpresa)
            statement.setLong(3, idVaga)

            statement.executeUpdate()
        }
    }

    Match buscarPorId(Long id) {
        String sql = """
            SELECT id, id_candidato, id_empresa, id_vaga
            FROM match
            WHERE id = ?
        """

        try (
            def connection = ConnectionFactory.getConnection()
            def statement = connection.prepareStatement(sql)
        ) {
            def resultSet = statement.executeQuery()

            if (!resultSet.next()) {
                return null
            }

            return new Match(
                    resultSet.getLong("id"),
                    resultSet.getLong("id_candidato"),
                    resultSet.getLong("id_empresa"),
                    resultSet.getLong("id_vaga"),
            )
        }
    }

    List<MatchEncontradoDTO> buscarMatchesPorEmpresa(Long idEmpresa) {

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
            v.descricao AS descricao_vaga

        FROM "match" m

        JOIN candidatos c
            ON c.id = m.id_candidato

        JOIN empresas e
            ON e.id = m.id_empresa

        JOIN vagas v
            ON v.id = m.id_vaga

        LEFT JOIN estados estado_candidato
            ON estado_candidato.id = c.id_estado

        LEFT JOIN pais pais_candidato
            ON pais_candidato.id = c.id_pais

        LEFT JOIN estados estado_empresa
            ON estado_empresa.id = e.id_estado

        LEFT JOIN pais pais_empresa
            ON pais_empresa.id = e.id_pais

        WHERE m.id_empresa = ?
    """

        try (
                def connection = ConnectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {

            statement.setLong(1, idEmpresa)

            def resultSet = statement.executeQuery()

            List<MatchEncontradoDTO> matches = []

            while (resultSet.next()) {

                Long idCandidato = resultSet.getLong("id_candidato")
                Long idVaga = resultSet.getLong("id_vaga")

                List<Competencia> competenciasCandidato =
                        competenciasCandidatoDAO.buscarPorCandidato(idCandidato)

                List<String> competenciasVaga =
                        competenciasVagaDAO.buscarPorVagaString(idVaga)

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
                                competenciasVaga
                        )
                )
            }

            return matches
        }
    }

    List<MatchEncontradoDTO> buscarMatchesPorCandidato(Long idCandidato) {

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
            v.descricao AS descricao_vaga

        FROM "match" m

        JOIN candidatos c
            ON c.id = m.id_candidato

        JOIN empresas e
            ON e.id = m.id_empresa

        JOIN vagas v
            ON v.id = m.id_vaga

        LEFT JOIN estados estado_candidato
            ON estado_candidato.id = c.id_estado

        LEFT JOIN pais pais_candidato
            ON pais_candidato.id = c.id_pais

        LEFT JOIN estados estado_empresa
            ON estado_empresa.id = e.id_estado

        LEFT JOIN pais pais_empresa
            ON pais_empresa.id = e.id_pais

        WHERE m.id_candidato = ?
    """

        try (
                def connection = ConnectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {

            statement.setLong(1, idCandidato)

            def resultSet = statement.executeQuery()

            List<MatchEncontradoDTO> matches = []

            while (resultSet.next()) {

                Long idVaga = resultSet.getLong("id_vaga")

                List<Competencia> competenciasCandidato =
                        competenciasCandidatoDAO.buscarPorCandidato(idCandidato)

                List<String> competenciasVaga =
                        competenciasVagaDAO.buscarPorVagaString(idVaga)

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
                                competenciasVaga
                        )
                )
            }

            return matches
        }
    }
}
