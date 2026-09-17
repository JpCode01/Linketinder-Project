package com.jpcode.dao.vaga

import com.jpcode.database.ConnectionFactory
import com.jpcode.model.core.Vaga

import java.sql.Statement

class VagaDAO {

    Vaga salvar(Vaga vaga) {
        String sql = """
            INSERT INTO     
                (nome, descricao, local, id_empresa)
            VALUES (?, ?, ?, ?)
        """

        try (
            def connection = ConnectionFactory.getConnection(sql)
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
            def connection = ConnectionFactory.getConnection(sql)
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

}
