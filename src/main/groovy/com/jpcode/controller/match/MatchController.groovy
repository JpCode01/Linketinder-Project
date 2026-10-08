package com.jpcode.controller.match

import com.jpcode.dto.Match.MatchEncontradoDTO
import com.jpcode.service.MatchService

class MatchController {

    final MatchService matchService

    MatchController(MatchService matchService) {
        this.matchService = matchService
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
}