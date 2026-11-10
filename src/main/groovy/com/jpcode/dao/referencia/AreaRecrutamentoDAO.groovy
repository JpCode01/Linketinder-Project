package com.jpcode.dao.referencia

import com.jpcode.dao.contratos.AreaRecrutamentoRepository
import com.jpcode.database.ConnectionFactory
import com.jpcode.model.referencia.AreaRecrutamento

class AreaRecrutamentoDAO implements AreaRecrutamentoRepository {
    private final ConnectionFactory connectionFactory

    AreaRecrutamentoDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory
    }

    Optional<AreaRecrutamento> buscarPorId(Long id) {
        try (def conexao = connectionFactory.getConnection();
             def comando = conexao.prepareStatement('SELECT id, nome FROM areas_recrutamento WHERE id = ?')) {
            comando.setLong(1, id)
            try (def resultado = comando.executeQuery()) {
                return resultado.next() ? Optional.of(new AreaRecrutamento(resultado.getLong('id'), resultado.getString('nome'))) : Optional.empty()
            }
        }
    }

    Optional<Long> buscarIdPorNome(String nome) {
        try (def conexao = connectionFactory.getConnection();
             def comando = conexao.prepareStatement('SELECT id FROM areas_recrutamento WHERE nome = ?')) {
            comando.setString(1, nome)
            try (def resultado = comando.executeQuery()) {
                return resultado.next() ? Optional.of(resultado.getLong('id')) : Optional.empty()
            }
        }
    }

    List<AreaRecrutamento> listarTodos() {
        return buscar()
    }

    List<AreaRecrutamento> buscar() {
        try (def conexao = connectionFactory.getConnection();
             def comando = conexao.prepareStatement('SELECT id, nome FROM areas_recrutamento ORDER BY id');
             def resultado = comando.executeQuery()) {
            List<AreaRecrutamento> areas = []
            while (resultado.next()) areas.add(new AreaRecrutamento(resultado.getLong('id'), resultado.getString('nome')))
            return areas
        }
    }
}
