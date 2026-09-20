package com.jpcode.dao.candidato

import com.jpcode.database.ConnectionFactory
import com.jpcode.dto.candidato.CandidatoMatchDTO
import com.jpcode.model.core.Candidato
import com.jpcode.model.referencia.Competencia

import java.sql.Date
import java.sql.Statement

class CandidatoDAO {

    Candidato salvar(Candidato candidato) {

        String sql = """
            INSERT INTO candidatos
                (nome, sobrenome, email, data_nascimento, cpf,
                id_pais, cep, descricao, ativo, senha, id_estado)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)            
        """



        
        try (
            def connection = ConnectionFactory.getConnection()
            def statement = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            )
        ) {

            statement.setString(1, candidato.nome)
            statement.setString(2, candidato.sobrenome)
            statement.setString(3, candidato.email)
            statement.setDate(
                    4,
                    Date.valueOf(candidato.dataNascimento)
            )
            statement.setString(5, candidato.cpf)
            statement.setLong(6, candidato.idPais)
            statement.setString(7, candidato.cep)
            statement.setString(8, candidato.descricao)
            statement.setBoolean(9, candidato.ativo)
            statement.setString(10, candidato.senha)
            statement.setLong(11, candidato.idEstado)

            
            statement.executeUpdate()

            def generatedKeys = statement.getGeneratedKeys()

            if (generatedKeys.next()) {
                candidato.id = generatedKeys.getLong(1)
            }

            return candidato
        }
    }

    Candidato buscarPorId(Long id) {
        String sql = """
            SELECT id, nome, sobrenome, email, data_nascimento, 
            cpf, id_pais, cep, descricao, ativo, senha, id_estado
            FROM candidatos
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

            return new Candidato(
                    resultSet.getLong("id"),
                    resultSet.getString("nome"),
                    resultSet.getString("sobrenome"),
                    resultSet.getString("email"),
                    resultSet.getString("senha"),
                    resultSet.getString("cpf"),
                    resultSet.getDate("data_nascimento").toLocalDate(),
                    resultSet.getLong("id_pais"),
                    resultSet.getInt("idade"),
                    resultSet.getLong("id_estado"),
                    resultSet.getString("cep"),
                    resultSet.getString("descricao"),
                    resultSet.getBoolean("ativo")
            )

        }

    }

    CandidatoMatchDTO buscarPorIdParaMatch(Long id) {
        String sql = """
        SELECT
            c.id,
            c.nome,
            c.sobrenome,
            c.email,
            c.cpf,
            c.data_nascimento,
            c.cep,
            c.descricao,
            p.nome AS pais,
            e.sigla AS estado,
            comp.id AS competencia_id,
            comp.nome_competencia
        FROM candidatos c
        JOIN pais p
            ON p.id = c.id_pais
        JOIN estados e
            ON e.id = c.id_estado
        LEFT JOIN candidatos_competencias cc
            ON cc.id_candidato = c.id
        LEFT JOIN competencias comp
            ON comp.id = cc.id_competencia
        WHERE c.id = ?
        """

        try (
            def connection = ConnectionFactory.getConnection()
            def statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, id)

            def resultSet = statement.executeQuery()

            CandidatoMatchDTO candidato = null
            List<Competencia> competencias = []

            while (resultSet.next()) {
                if (candidato == null) {
                    new CandidatoMatchDTO(
                            resultSet.getLong("id"),
                            resultSet.getString("nome"),
                            resultSet.getString("sobrenome"),
                            resultSet.getString("email"),
                            resultSet.getString("cpf"),
                            resultSet.getDate("nascimento"),
                            resultSet.getString("cep"),
                            resultSet.getString("descricao"),
                            resultSet.getInt("idade"),
                            resultSet.getString("estado"),
                            resultSet.getString("pais"),
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
            return candidato
        }
    }

    Candidato buscarPorEmailESenha(String email, String senha) {
        String sql = """
            SELECT id, nome, sobrenome, email, data_nascimento, 
            cpf, id_pais, cep, descricao, ativo, senha, id_estado
            FROM empresas
            WHERE email = ?
                AND senha = ?
        """

        try (
                def connection = ConnectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, email)
            statement.setString(2, senha)

            def resultSet = statement.executeQuery()

            if (!resultSet.next()) {
                return null
            }

            return new Candidato(
                    resultSet.getLong("id"),
                    resultSet.getString("nome"),
                    resultSet.getString("sobrenome"),
                    resultSet.getString("email"),
                    resultSet.getString("senha"),
                    resultSet.getString("cpf"),
                    resultSet.getDate("data_nascimento").toLocalDate(),
                    resultSet.getLong("id_pais"),
                    resultSet.getInt("idade"),
                    resultSet.getLong("id_estado"),
                    resultSet.getString("cep"),
                    resultSet.getString("descricao"),
                    resultSet.getBoolean("ativo")
            )

        }
    }
}
