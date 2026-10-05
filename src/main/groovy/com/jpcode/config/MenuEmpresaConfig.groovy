package com.jpcode.config

import com.jpcode.view.empresa.MenuCurtirCandidato
import com.jpcode.view.empresa.MenuEmpresaCrud
import com.jpcode.view.vaga.MenuVagaCrud

class MenuEmpresaConfig {

    final MenuEmpresaCrud menuEmpresaCrud
    final MenuVagaCrud menuVagaCrud
    final MenuCurtirCandidato menuCurtirCandidato

    MenuEmpresaConfig(Scanner scanner, ServiceConfig serviceConfig) {

        menuVagaCrud = new MenuVagaCrud(
                scanner,
                serviceConfig.vagaService,
                serviceConfig.competenciaService,
                serviceConfig.empresaService
        )

        menuEmpresaCrud = new MenuEmpresaCrud(
                scanner,
                serviceConfig.empresaService,
                serviceConfig.referenciaService,
                serviceConfig.matchService
        )

        menuCurtirCandidato = new MenuCurtirCandidato(
                scanner,
                menuVagaCrud,
                serviceConfig.empresaService,
                serviceConfig.matchService
        )
    }
}