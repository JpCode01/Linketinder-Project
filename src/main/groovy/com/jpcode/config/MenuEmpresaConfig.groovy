package com.jpcode.config


import com.jpcode.view.empresa.MenuCurtirCandidato
import com.jpcode.view.empresa.MenuEmpresaCrud
import com.jpcode.view.vaga.MenuVagaCrud

class MenuEmpresaConfig {

    final MenuEmpresaCrud menuEmpresaCrud
    final MenuVagaCrud menuVagaCrud
    final MenuCurtirCandidato menuCurtirCandidato

    MenuEmpresaConfig(
            Scanner scanner,
            ControllerConfig controllerConfig
    ) {

        menuVagaCrud = new MenuVagaCrud(
                scanner,
                controllerConfig.vagaController,
                controllerConfig.competenciaController,
                controllerConfig.empresaController
        )

        menuEmpresaCrud = new MenuEmpresaCrud(
                scanner,
                controllerConfig.empresaController,
                controllerConfig.referenciaController,
                controllerConfig.matchController
        )

        menuCurtirCandidato = new MenuCurtirCandidato(
                scanner,
                menuVagaCrud,
                controllerConfig.empresaController,
                controllerConfig.matchController
        )
    }
}