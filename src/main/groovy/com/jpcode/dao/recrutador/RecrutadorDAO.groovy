package com.jpcode.dao.recrutador

import com.jpcode.dao.conta.ValidacaoContaDAO
import com.jpcode.dao.contratos.RecrutadorRepository
import com.jpcode.database.ConnectionFactory
import com.jpcode.model.core.Recrutador
import java.sql.Date
import java.sql.Statement

class RecrutadorDAO implements RecrutadorRepository {
    private final ConnectionFactory connectionFactory
    private final ValidacaoContaDAO validacaoContaDAO

    RecrutadorDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory
        this.validacaoContaDAO = new ValidacaoContaDAO(connectionFactory)
    }

    Recrutador salvar(Recrutador recrutador) {
        return validacaoContaDAO.executar('recrutadores', null, recrutador.email, recrutador.cpf) { conexao ->
            // Revalidar depois dos locks: desativação concorrente da empresa não pode passar.
            try (def referencia = conexao.prepareStatement('SELECT 1 FROM empresas WHERE id = ? AND ativo = true')) {
                referencia.setLong(1, recrutador.idEmpresa)
                try (def resultado = referencia.executeQuery()) {
                    if (!resultado.next()) throw new IllegalArgumentException('Empresa inexistente ou inativa.')
                }
            }
            try (def referencia = conexao.prepareStatement('SELECT id FROM areas_recrutamento WHERE id = ? FOR KEY SHARE')) {
                referencia.setLong(1, recrutador.idAreaRecrutamento)
                try (def resultado = referencia.executeQuery()) {
                    if (!resultado.next()) throw new IllegalArgumentException('Área de recrutamento inexistente.')
                }
            }
            try (def comando = conexao.prepareStatement('''INSERT INTO recrutadores
                    (nome, cpf, email, data_nascimento, descricao, senha, id_area_recrutamento, id_empresa, ativo)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, true)''', Statement.RETURN_GENERATED_KEYS)) {
                comando.setString(1, recrutador.nome)
                comando.setString(2, recrutador.cpf)
                comando.setString(3, recrutador.email)
                comando.setDate(4, Date.valueOf(recrutador.dataNascimento))
                comando.setString(5, recrutador.descricao)
                comando.setString(6, recrutador.senha)
                comando.setLong(7, recrutador.idAreaRecrutamento)
                comando.setLong(8, recrutador.idEmpresa)
                comando.executeUpdate()
                try (def chaves = comando.generatedKeys) {
                    if (chaves.next()) recrutador.id = chaves.getLong(1)
                }
                recrutador.ativo = true
                return recrutador
            }
        }
    }

    Optional<Recrutador> buscarAtivoPorEmail(String email) {
        try (def conexao = connectionFactory.getConnection();
             def comando = conexao.prepareStatement('SELECT id, senha, ativo FROM recrutadores WHERE email = ? AND ativo = true')) {
            comando.setString(1, email)
            try (def resultado = comando.executeQuery()) {
                if (!resultado.next()) return Optional.empty()
                return Optional.of(new Recrutador(id: resultado.getLong('id'),
                        senha: resultado.getString('senha'), ativo: resultado.getBoolean('ativo')))
            }
        }
    }

    Optional<Recrutador> buscarPorId(Long id) {
        try (def conexao = connectionFactory.getConnection();
             def comando = conexao.prepareStatement('SELECT * FROM recrutadores WHERE id = ?')) {
            comando.setLong(1, id)
            try (def resultado = comando.executeQuery()) {
                if (!resultado.next()) return Optional.empty()
                return Optional.of(new Recrutador(id: resultado.getLong('id'), nome: resultado.getString('nome'),
                        cpf: resultado.getString('cpf'), email: resultado.getString('email'),
                        dataNascimento: resultado.getDate('data_nascimento').toLocalDate(),
                        descricao: resultado.getString('descricao'), senha: resultado.getString('senha'),
                        idAreaRecrutamento: resultado.getLong('id_area_recrutamento'),
                        idEmpresa: resultado.getLong('id_empresa'), ativo: resultado.getBoolean('ativo')))
            }
        }
    }
}
