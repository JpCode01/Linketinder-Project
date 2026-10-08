package com.jpcode.controller.match

import com.jpcode.dto.Match.MatchEncontradoDTO
import com.jpcode.facade.MatchFacade
import com.jpcode.service.MatchService

class MatchController {

    final MatchService matchService
    final MatchFacade matchFacade

    MatchController(MatchService matchService, MatchFacade matchFacade) {
        this.matchService = matchService
        this.matchFacade = matchFacade
    }

    void salvar(
            Long idCandidato,
            Long idEmpresa,
            Long idVaga
    ) {
        matchService.salvar(
                idCandidato,
                idEmpresa,
                idVaga
        )
    }

    List<MatchEncontradoDTO> verMatchesPorEmpresa(Long idEmpresa) {
        return matchService.verMatchesPorEmpresa(idEmpresa)
    }

    List<MatchEncontradoDTO> verMatchesPorCandidato(Long idCandidato) {
        return matchService.verMatchesPorCandidato(idCandidato)
    }

    void curtirCandidato(
            Long idEmpresa,
            Long idCandidato,
            Long idVaga
    ) {
        matchFacade.curtirCandidato(
                idEmpresa,
                idCandidato,
                idVaga
        )
    }
}