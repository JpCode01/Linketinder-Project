package com.jpcode.config

import com.jpcode.service.CandidatoService
import com.jpcode.service.CompetenciaService
import com.jpcode.service.EmpresaService
import com.jpcode.service.MatchService
import com.jpcode.service.ReferenciaService
import com.jpcode.service.VagaService

class ServiceConfig {
    final EmpresaService empresaService
    final VagaService vagaService
    final CandidatoService candidatoService
    final MatchService matchService
    final CompetenciaService competenciaService
    final ReferenciaService referenciaService

    ServiceConfig(DaoConfig daoConfig) {
        empresaService = new EmpresaService(
                daoConfig.paisDAO,
                daoConfig.estadoDAO,
                daoConfig.empresaDAO,
                daoConfig.empresaDAO,
                daoConfig.empresaDAO,
                daoConfig.candidatoCurtirDAO,
                daoConfig.empresaCurtirDAO,
                daoConfig.competenciasVagaDAO,
                daoConfig.competenciaDAO
        )

        vagaService = new VagaService(
                daoConfig.competenciaDAO,
                daoConfig.competenciasVagaDAO,
                daoConfig.vagaDAO,
                daoConfig.candidatoCurtirDAO
        )

        candidatoService = new CandidatoService(
                daoConfig.paisDAO,
                daoConfig.estadoDAO,
                daoConfig.competenciaDAO,
                daoConfig.getCompetenciasCandidatoDAO(),
                daoConfig.candidatoDAO,
                daoConfig.candidatoDAO,
                daoConfig.candidatoDAO
        )

        matchService = new MatchService(
                daoConfig.matchDAO
        )

        competenciaService = new CompetenciaService(
                daoConfig.competenciaDAO
        )

        referenciaService = new ReferenciaService(
                daoConfig.paisDAO,
                daoConfig.estadoDAO
        )
    }
}