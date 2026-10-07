package com.jpcode.facade

import com.jpcode.service.EmpresaService
import com.jpcode.service.MatchService

class MatchFacade {

    final EmpresaService empresaService
    final MatchService matchService

    MatchFacade(
            EmpresaService empresaService,
            MatchService matchService
    ) {
        this.empresaService = empresaService
        this.matchService = matchService
    }

    void curtirCandidato(
            Long idEmpresa,
            Long idCandidato,
            Long idVaga
    ) {
        empresaService.curtirCandidato(
                idEmpresa,
                idCandidato
        )

        matchService.salvar(
                idCandidato,
                idEmpresa,
                idVaga
        )
    }
}