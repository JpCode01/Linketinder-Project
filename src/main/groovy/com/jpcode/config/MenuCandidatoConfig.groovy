package com.jpcode.config

import com.jpcode.view.candidato.MenuCandidatoCrud
import com.jpcode.view.candidato.MenuCurtirVaga
import com.jpcode.view.referencia.MenuCompetencia

class MenuCandidatoConfig {

    final MenuCandidatoCrud menuCandidatoCrud
    final MenuCurtirVaga menuCurtirVaga
    final MenuCompetencia menuCompetencia

    MenuCandidatoConfig(
            Scanner scanner,
            ControllerConfig controllerConfig
    ) {

        menuCurtirVaga = new MenuCurtirVaga(
                scanner,
                controllerConfig.vagaController
        )

        menuCompetencia = new MenuCompetencia(
                scanner,
                controllerConfig.competenciaController
        )

        menuCandidatoCrud = new MenuCandidatoCrud(
                scanner,
                controllerConfig.candidatoController,
                menuCompetencia,
                controllerConfig.referenciaController,
                controllerConfig.matchController
        )
    }
}