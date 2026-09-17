package com.jpcode.dao

import com.jpcode.database.ConnectionFactory
import com.jpcode.model.Candidato

import java.sql.Date
import java.sql.Statement

class CandidatoDAO {

    Candidato salvar(Candidato candidato) {

        String sql = """
            INSERT INTO candidatos
                (nome, sobrenome, email, data_nascimento, cpf,
                id_pais, cep, descricao, ativo, senha)
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

            
            statement.executeUpdate()

            def generatedKeys = statement.getGeneratedKeys()

            if (generatedKeys.next()) {
                candidato.id = generatedKeys.getLong(1)
            }

            return candidato
        }
    }

    Candidato buscacrPorId(Long id) {
        String sql = """
            SELECT id, nome, sobrenome, email, data_nascimento, 
            cpf, id_pais, cep, descricao, ativo, senha
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
                    resultSet.getString("estado"),
                    resultSet.getString("cep"),
                    resultSet.getString("descricao")
            )

        }

    }
}
