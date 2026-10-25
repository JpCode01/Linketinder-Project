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
                daoConfig.candidatoCurtirDAO,
                daoConfig.empresaCurtirDAO,
                daoConfig.competenciasVagaDAO
        )

        vagaService = new VagaService(
                daoConfig.vagaDAO,
                daoConfig.competenciaDAO,
                daoConfig.competenciaDAO,
                daoConfig.competenciasVagaDAO,
                daoConfig.candidatoCurtirDAO
        )

        candidatoService = new CandidatoService(
                daoConfig.paisDAO,
                daoConfig.estadoDAO,
                daoConfig.competenciaDAO,
                daoConfig.competenciaDAO,
                daoConfig.competenciasCandidatoDAO,
                daoConfig.candidatoDAO
        )

        matchService = new MatchService(
                daoConfig.matchDAO
        )

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