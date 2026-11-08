package com.jpcode.service


import com.jpcode.dao.contratos.MatchRepository
import java.sql.Connection
import com.jpcode.dto.Match.MatchEncontradoDTO

class MatchService {
    final MatchRepository matchRepository

    MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository
    }

    void salvar(Long idCandidato, Long idEmpresa, Long idVaga) {
        throw new UnsupportedOperationException('Match exige retribuição transacional pela MatchFacade.')
    }

    void salvarNaTransacao(Connection conexao, Long idCandidato, Long idEmpresa, Long idVaga) {
        matchRepository.salvar(conexao, idCandidato, idEmpresa, idVaga)
    }

    List<MatchEncontradoDTO> verMatchesPorEmpresa(Long idEmpresa) {
        return matchRepository.buscarMatchesPorEmpresa(idEmpresa)
    }

    List<MatchEncontradoDTO> verMatchesPorCandidato(Long idCandidato) {
        return matchRepository.buscarMatchesPorCandidato(idCandidato)
    }

}
