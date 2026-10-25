package com.jpcode.dao.empresa

import com.jpcode.dao.contratos.UsuarioRepository
import com.jpcode.database.ConnectionFactory
import com.jpcode.dto.empresa.EmpresaComReferenciasDTO
import com.jpcode.dto.empresa.EmpresaViewListDTO
import com.jpcode.model.core.Empresa
import com.jpcode.model.referencia.Estado
import com.jpcode.model.referencia.Pais

import java.sql.Statement

class EmpresaDAO implements
        UsuarioRepository<Empresa, EmpresaComReferenciasDTO, EmpresaViewListDTO>
         {

    private final ConnectionFactory connectionFactory

    EmpresaDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory
    }

    Empresa salvar(Empresa empresa) {
        String sql = """
        INSERT INTO empresas
            (nome, cnpj, email_corporativo, descricao, 
            id_pais, cep, id_estado, ativo, senha)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """

        try (
                def connection = connectionFactory.getConnection()
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

    Optional<Empresa> buscarPorId(Long id) {
        String sql = """
            SELECT id, nome, cnpj, email_corporativo, descricao, 
            id_pais, cep, id_estado, ativo, senha
            FROM empresas
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

            Empresa empresa = new Empresa(
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

            return Optional.of(empresa)
        }
    }

    Optional<Empresa> buscarPorEmailESenha(String emailCorporativo, String senha) {
        String sql = """
            SELECT id, nome, cnpj, email_corporativo, descricao, 
            id_pais, cep, id_estado, ativo, senha
            FROM empresas
            WHERE email_corporativo = ?
                AND senha = ?
        """

        try (
                def connection = connectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, emailCorporativo)
            statement.setString(2, senha)

            def resultSet = statement.executeQuery()

            if (!resultSet.next() || !resultSet.getBoolean("ativo")) {
                return Optional.empty()
            }

            Empresa empresa = new Empresa(
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

            return Optional.of(empresa)
        }
    }

    void desativar(Long id) {
        String sql = """
        UPDATE empresas
        SET ativo = false
        WHERE id = ?
    """

        try (
                def connection = connectionFactory.getConnection()
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
                def connection = connectionFactory.getConnection()
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


    Optional<EmpresaComReferenciasDTO> buscarPorIdComReferencias(Long id) {
        String sql = """
        SELECT
            e.id,
            e.nome,
            e.cnpj,
            e.email_corporativo,
            e.descricao,
            e.id_pais,
            e.cep,
            e.id_estado,
            e.ativo,
            e.senha,
            es.sigla AS estado_sigla,
            p.nome AS pais_nome
        FROM empresas e
        JOIN estados es ON es.id = e.id_estado
        JOIN pais p ON p.id = e.id_pais
        WHERE e.id = ?
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

            Empresa empresa = new Empresa(
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

            Estado estado = new Estado(
                    resultSet.getLong("id_estado"),
                    resultSet.getString("estado_sigla")
            )

            Pais pais = new Pais(
                    resultSet.getLong("id_pais"),
                    resultSet.getString("pais_nome")
            )

            return Optional.of(
                    new EmpresaComReferenciasDTO(empresa, estado, pais)
            )
        }
    }

    List<EmpresaViewListDTO> buscar() {
        String sql = """
        SELECT
            id,
            nome,
            email_corporativo,
            descricao
        FROM empresas
        ORDER BY id
    """

        try (
                def connection = connectionFactory.getConnection()
                def statement = connection.prepareStatement(sql)
                def resultSet = statement.executeQuery()
        ) {
            List<EmpresaViewListDTO> empresas = []

            while (resultSet.next()) {
                empresas.add(new EmpresaViewListDTO(
                        resultSet.getLong("id"),
                        resultSet.getString("nome"),
                        resultSet.getString("email_corporativo"),
                        resultSet.getString("descricao")
                ))
            }

            return empresas
        }
    }
}
