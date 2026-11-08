package com.jpcode.config

import com.jpcode.service.RecrutadorService

import com.jpcode.service.CandidatoService
import com.jpcode.service.CompetenciaService
import com.jpcode.service.EmpresaService
import com.jpcode.service.MatchService
import com.jpcode.service.ReferenciaService
import com.jpcode.service.VagaService
import com.jpcode.service.SenhaService

import com.jpcode.facade.MatchFacade

class ServiceConfig {
    final MatchFacade matchFacade
    final RecrutadorService recrutadorService
    final SenhaService senhaService
    final EmpresaService empresaService
    final VagaService vagaService
    final CandidatoService candidatoService
    final MatchService matchService
    final CompetenciaService competenciaService
    final ReferenciaService referenciaService

    ServiceConfig(DaoConfig daoConfig) {
        senhaService = new SenhaService()
        recrutadorService = new RecrutadorService(
                daoConfig.recrutadorDAO,
                daoConfig.areaRecrutamentoDAO,
                daoConfig.empresaDAO,
                senhaService
        )

        empresaService = new EmpresaService(
                daoConfig.paisDAO,
                daoConfig.estadoDAO,
                daoConfig.empresaDAO,
                daoConfig.candidatoCurtirDAO,
                daoConfig.empresaCurtirDAO,
                daoConfig.competenciasVagaDAO,
                senhaService
        )

        vagaService = new VagaService(
                daoConfig.vagaDAO,
                daoConfig.competenciaDAO,
                daoConfig.competenciaDAO,
                daoConfig.competenciasVagaDAO,
                daoConfig.candidatoCurtirDAO,
                daoConfig.empresaDAO,
                daoConfig.recrutadorDAO
        )

        candidatoService = new CandidatoService(
                daoConfig.paisDAO,
                daoConfig.estadoDAO,
                daoConfig.competenciaDAO,
                daoConfig.competenciaDAO,
                daoConfig.competenciasCandidatoDAO,
                daoConfig.candidatoDAO,
                senhaService
        )

        matchService = new MatchService(
                daoConfig.matchDAO
        )

        matchFacade = new MatchFacade(daoConfig.connectionFactory, daoConfig.empresaCurtirDAO,
                daoConfig.candidatoCurtirDAO, matchService)

        competenciaService = new CompetenciaService(
                daoConfig.competenciaDAO,
                daoConfig.competenciaDAO
        )

        referenciaService = new ReferenciaService(
                daoConfig.paisDAO,
                daoConfig.estadoDAO
        )
    }

}
