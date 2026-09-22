package com.jpcode.dao.vaga

import com.jpcode.database.ConnectionFactory
import com.jpcode.dto.vaga.VagaAnonimaDTO
import com.jpcode.dto.vaga.VagaEmpresaDTO
import com.jpcode.model.core.Vaga
import com.jpcode.model.referencia.Competencia

import java.sql.Statement

class VagaDAO {

    Vaga salvar(Vaga vaga) {
        String sql = """
            INSERT INTO vagas           
                (nome, descricao, local, id_empresa)
            VALUES (?, ?, ?, ?)
        """
    
        try (
            def connection = ConnectionFactory.getConnection()
            def statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setString(1, vaga.nome)
            statement.setString(2, vaga.descricao)
            statement.setString(3, vaga.local)
            statement.setLong(4, vaga.idEmpresa)

            statement.executeUpdate()

            def generatedKeys = statement.getGeneratedKeys()

            if (generatedKeys.next()) {
                vaga.id = generatedKeys.getLong(1)
            }

            return vaga
        }


    }

    Vaga buscarPorId(Long id) {
        String sql = """
            SELECT id, nome, descricao, local, id_empresa
            FROM vagas
            WHERE id = ? 
        """

        try (
            def connection = ConnectionFactory.getConnection()
            def statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, id)

            def resultSet = statement.executeQuery()

            if (!resultSet.next()) {
                return null
            }

            return new Vaga(
                    resultSet.getLong("id"),
                    resultSet.getString("nome"),
                    resultSet.getString("descricao"),
                    resultSet.getString("local"),
                    resultSet.getLong("id_empresa")
            )

        }
    }

        List<VagaEmpresaDTO> buscarVagasEmpresa(Long idEmpresa) {
            String sql = """
            SELECT
                v.id,
                v.nome,
                v.descricao,
                v.local,
                v.id_empresa,
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
            LEFT JOIN vagas_competencias vc
                ON vc.id_vaga = v.id
            LEFT JOIN competencias c
                ON c.id = vc.id_competencia
            WHERE v.id_empresa = ?
            ORDER BY v.id
            """
    
            try (
                def connection = ConnectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
            ) {
                statement.setLong(1, idEmpresa)
    
                def resultSet = statement.executeQuery()
    
                List<VagaEmpresaDTO> vagas = []
    
                VagaEmpresaDTO vagaAtual = null
                List<Competencia> competencias = []
    
                while (resultSet.next()) {
    
                    Long idVaga = resultSet.getLong("id")
                    // Apenas cria quando o id da vaga for diferente da vaga atual, caso contrário, considera a mesma vaga e add as competencias
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
            def connection = ConnectionFactory.getConnection()
            def statement = connection.prepareStatement(sql)
        ) {
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

                    vagas = []

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

    void deletar(Long id) {
        String sql = """
        DELETE FROM vagas
        WHERE id = ?
    """

        try (
                def connection = ConnectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, id)
            statement.executeUpdate()
        }
    }

    Vaga atualizarDados(Vaga vaga) {
        String sql = """
        UPDATE vagas
        SET nome = ?,
            descricao = ?,
            "local" = ?
        WHERE id = ?
        """

        try (
                def connection = ConnectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, vaga.nome)
            statement.setString(2, vaga.descricao)
            statement.setString(3, vaga.local)
            statement.setLong(4, vaga.id)

            statement.executeUpdate()

        }

        return vaga
    }
}
