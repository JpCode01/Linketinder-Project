package com.jpcode.config

import com.jpcode.controller.candidato.CandidatoController
import com.jpcode.controller.competencia.CompetenciaController
import com.jpcode.controller.empresa.EmpresaController
import com.jpcode.controller.match.MatchController
import com.jpcode.controller.referencia.ReferenciaController
import com.jpcode.controller.vaga.VagaController
import com.jpcode.facade.MatchFacade

class ControllerConfig {

    final CandidatoController candidatoController
    final EmpresaController empresaController
    final VagaController vagaController
    final CompetenciaController competenciaController
    final MatchController matchController
    final ReferenciaController referenciaController

    ControllerConfig(ServiceConfig serviceConfig) {

        candidatoController = new CandidatoController(
                serviceConfig.candidatoService
        )

        empresaController = new EmpresaController(
                serviceConfig.empresaService
        )

        vagaController = new VagaController(
                serviceConfig.vagaService
        )

        competenciaController = new CompetenciaController(
                serviceConfig.competenciaService
        )

        MatchFacade matchFacade = new MatchFacade(
                serviceConfig.empresaService,
                serviceConfig.matchService
        )

        matchController = new MatchController(
                serviceConfig.matchService,
                matchFacade
        )

        referenciaController = new ReferenciaController(
                serviceConfig.referenciaService
        )
    }
}