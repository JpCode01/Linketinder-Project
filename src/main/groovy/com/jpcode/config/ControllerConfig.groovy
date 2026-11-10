package com.jpcode.config

import com.jpcode.controller.recrutador.RecrutadorController

import com.jpcode.controller.candidato.CandidatoController
import com.jpcode.controller.competencia.CompetenciaController
import com.jpcode.controller.empresa.EmpresaController
import com.jpcode.controller.match.MatchController
import com.jpcode.controller.referencia.ReferenciaController
import com.jpcode.controller.vaga.VagaController
import com.jpcode.api.SessaoHttp

class ControllerConfig {
    final SessaoHttp sessaoHttp
    final RecrutadorController recrutadorController

    final CandidatoController candidatoController
    final EmpresaController empresaController
    final VagaController vagaController
    final CompetenciaController competenciaController
    final MatchController matchController
    final ReferenciaController referenciaController

    ControllerConfig(ServiceConfig serviceConfig) {
        recrutadorController = new RecrutadorController(serviceConfig.recrutadorService)


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

        matchController = new MatchController(
                serviceConfig.matchService,
                serviceConfig.matchFacade
        )

        referenciaController = new ReferenciaController(
                serviceConfig.referenciaService
        )
        sessaoHttp = new SessaoHttp(candidatoController, empresaController, recrutadorController)
    }

}
