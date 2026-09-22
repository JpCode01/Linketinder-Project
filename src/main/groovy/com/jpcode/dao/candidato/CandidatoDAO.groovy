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
                id_pais, cep, descricao, ativo, senha, id_estado, idade)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)            
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
            statement.setLong(12, candidato.idade)

            
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
            cpf, idade, id_pais, cep, descricao, ativo, senha, id_estado
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

    Candidato buscarPorEmailESenha(String email, String senha) {
        String sql = """
            SELECT id, nome, sobrenome, email, data_nascimento, 
            idade, cpf, id_pais, cep, descricao, ativo, senha, id_estado
            FROM candidatos
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

            if (!resultSet.next() || !resultSet.getBoolean("ativo")) {
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

    void desativar(Long id) {
        String sql = """
        UPDATE candidatos
        SET ativo = false
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

    Candidato atualizarDados(Candidato candidato) {
        String sql = """
        UPDATE candidatos
        SET nome = ?,
            sobrenome = ?,
            email = ?,
            senha = ?,
            cpf = ?,
            data_nascimento = ?,
            id_pais = ?,
            id_estado = ?,
            cep = ?,
            descricao = ?
        WHERE id = ?
    """

        try (
                def connection = ConnectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, candidato.nome)
            statement.setString(2, candidato.sobrenome)
            statement.setString(3, candidato.email)
            statement.setString(4, candidato.senha)
            statement.setString(5, candidato.cpf)
            statement.setDate(6, Date.valueOf(candidato.dataNascimento))
            statement.setLong(7, candidato.idPais)
            statement.setLong(8, candidato.idEstado)
            statement.setString(9, candidato.cep)
            statement.setString(10, candidato.descricao)
            statement.setLong(11, candidato.id)

            statement.executeUpdate()
        }

        return candidato
    }
}
