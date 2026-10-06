package com.jpcode.config

import com.jpcode.service.CompetenciaService
import com.jpcode.view.candidato.MenuCandidatoCrud
import com.jpcode.view.candidato.MenuCurtirVaga
import com.jpcode.view.referencia.MenuCompetencia


class MenuCandidatoConfig {

    final MenuCandidatoCrud menuCandidatoCrud
    final MenuCurtirVaga menuCurtirVaga
    final MenuCompetencia menuCompetencia

    MenuCandidatoConfig(
            Scanner scanner,
            ServiceConfig serviceConfig
    ) {

        menuCurtirVaga = new MenuCurtirVaga(
                scanner,
                serviceConfig.vagaService
        )

        menuCompetencia = new MenuCompetencia(
                scanner,
                serviceConfig.competenciaService
        )

        menuCandidatoCrud = new MenuCandidatoCrud(
                scanner,
                serviceConfig.candidatoService,
                menuCompetencia,
                serviceConfig.referenciaService,
                serviceConfig.matchService
        )
    }
}