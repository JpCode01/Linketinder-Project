package com.jpcode.dao.empresa

import com.jpcode.database.ConnectionFactory
import com.jpcode.model.core.Empresa

import java.sql.Statement

class EmpresaDAO {

    Empresa salvar(Empresa empresa) {
        String sql = """
        INSERT INTO empresas
            (nome, cnpj, email_corporativo, descricao, 
            id_pais, cep, id_estado, ativo, senha)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """

        try (
                def connection = ConnectionFactory.getConnection()
                def statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setString(1, empresa.nome)

            statement.setString(2, empresa.cnpj)
            statement.setString(3, empresa.email)
            statement.setString(4, empresa.descricao)
            statement.setLong(5, empresa.idPais)
            statement.setString(6, empresa.cep)
            statement.setLong(7, empresa.idEstado)
            statement.setBoolean(8, empresa.ativo)
            statement.setString(9, empresa.senha)

            statement.executeUpdate()

            def generatedKeys = statement.getGeneratedKeys()

            if (generatedKeys.next()) {
                empresa.id = generatedKeys.getLong(1)
            }

            return empresa
        }
    }

    Empresa buscarPorId(Long id) {
        String sql = """
            SELECT id, nome, cnpj, email_corporativo, descricao, 
            id_pais, cep, id_estado, ativo, senha
            FROM empresas
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

            return new Empresa(
                    resultSet.getLong("id"),
                    resultSet.getString("nome"),
                    resultSet.getString("email_corporativo"),
                    resultSet.getString("senha"),
                    resultSet.getString("cnpj"),
                    resultSet.getLong("id_pais"),
                    resultSet.getLong("id_estado"),
                    resultSet.getString("cep"),
                    resultSet.getString("descricao"),
                    resultSet.getBoolean("ativo")
                    
                    
                    
            ) 
        }
    }
}
