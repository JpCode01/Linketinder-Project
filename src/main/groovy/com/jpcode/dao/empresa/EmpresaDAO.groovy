package com.jpcode.dao.empresa

import com.jpcode.database.ConnectionFactory
import com.jpcode.dto.empresa.EmpresaMatchDTO
import com.jpcode.model.core.Candidato
import com.jpcode.model.core.Empresa

import java.sql.Date
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
            def connection = ConnectionFactory.getConnection()
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

    EmpresaMatchDTO buscarPorIdParaMatch(Long id) {
        String sql = """
            SELECT
            e.id, e.nome, e.cnpj,
            e.email_corporativo,
            e.descricao,
            p.nome AS pais, e.cep,
            es.sigla AS estado
            FROM empresas e
            JOIN pais p
            ON p.id = e.id_pais
            JOIN estados es
            ON es.id = e.id_estado
            WHERE e.id = ?
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

            return new EmpresaMatchDTO(
                    resultSet.getLong("id"),
                    resultSet.getString("nome"),
                    resultSet.getString("cnpj"),
                    resultSet.getString("email_corporativo"),
                    resultSet.getString("descricao"),
                    resultSet.getString("pais"),
                    resultSet.getString("cep"),
                    resultSet.getString("estado")
            )
        }
    }

    Empresa buscarPorEmailESenha(String emailCorporativo, String senha) {
        String sql = """
            SELECT id, nome, cnpj, email_corporativo, descricao, 
            id_pais, cep, id_estado, ativo, senha
            FROM empresas
            WHERE email_corporativo = ?
                AND senha = ?
        """

        try (
                def connection = ConnectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, emailCorporativo)
            statement.setString(2, senha)

            def resultSet = statement.executeQuery()

            if (!resultSet.next() || !resultSet.getBoolean("ativo")) {
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

    void desativar(Long id) {
        String sql = """
        UPDATE empresas
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

    Empresa atualizarDados(Empresa empresa) {
        String sql = """
        UPDATE empresas
        SET nome = ?,
            email_corporativo = ?,
            senha = ?,  
            cnpj = ?,   
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
            statement.setString(1, empresa.nome)
            statement.setString(2, empresa.email)
            statement.setString(3, empresa.senha)
            statement.setString(4, empresa.cnpj)           
            statement.setLong(5, empresa.idPais)
            statement.setLong(6, empresa.idEstado)
            statement.setString(7, empresa.cep)
            statement.setString(8, empresa.descricao)
            statement.setLong(9, empresa.id)

            statement.executeUpdate()
        }

        return empresa
    }
}
