package com.jpcode.service


import com.jpcode.dao.match.contrato.MatchRepository
import com.jpcode.dto.Match.MatchEncontradoDTO

class MatchService {
    final MatchRepository matchRepository

    MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository
    }

    void salvar(Long idCandidato, Long idEmpresa, Long idVaga) {
        matchRepository.salvar(idCandidato, idEmpresa, idVaga)
    }

    List<MatchEncontradoDTO> verMatchesPorEmpresa(Long idEmpresa) {
        return matchRepository.buscarMatchesPorEmpresa(idEmpresa)
    }

    List<MatchEncontradoDTO> verMatchesPorCandidato(Long idCandidato) {
        return matchRepository.buscarMatchesPorCandidato(idCandidato)
    }

}
