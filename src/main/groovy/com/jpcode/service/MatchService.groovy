package com.jpcode.service

import com.jpcode.dao.match.MatchDAO

class MatchService {
    final MatchDAO matchDAO

    MatchService(MatchDAO matchDAO) {
        this.matchDAO = matchDAO
    }

    void salvar(Long idCandidato, Long idEmpresa, Long idVaga) {
        matchDAO.salvar(idCandidato, idEmpresa, idVaga)
    }
}
