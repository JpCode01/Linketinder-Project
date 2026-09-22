package com.jpcode.service

import com.jpcode.dao.match.MatchDAO
import com.jpcode.dto.Match.MatchEncontradoDTO

class MatchService {
    final MatchDAO matchDAO

    MatchService(MatchDAO matchDAO) {
        this.matchDAO = matchDAO
    }

    void salvar(Long idCandidato, Long idEmpresa, Long idVaga) {
        matchDAO.salvar(idCandidato, idEmpresa, idVaga)
    }

    List<MatchEncontradoDTO> verMatchesPorEmpresa(Long idEmpresa) {
        return matchDAO.buscarMatchesPorEmpresa(idEmpresa)
    }

    List<MatchEncontradoDTO> verMatchesPorCandidato(Long idCandidato) {
        return matchDAO.buscarMatchesPorCandidato(idCandidato)
    }

}
