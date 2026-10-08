package com.jpcode.factory

import com.jpcode.config.DaoConfig
import com.jpcode.config.MenuCandidatoConfig
import com.jpcode.config.MenuEmpresaConfig
import com.jpcode.config.ServiceConfig
import com.jpcode.view.candidato.MenuCandidato
import com.jpcode.view.empresa.MenuEmpresa
import com.jpcode.config.ControllerConfig

class MenuFactory {
    private final Scanner scanner
    private final DaoConfig daoConfig
    private final ServiceConfig serviceConfig
    private final MenuCandidatoConfig menuCandidatoConfig
    private final MenuEmpresaConfig menuEmpresaConfig
    private final ControllerConfig controllerConfig

    MenuFactory(Scanner scanner) {
        this.scanner = scanner

        daoConfig = new DaoConfig()
        serviceConfig = new ServiceConfig(daoConfig)

        controllerConfig = new ControllerConfig(
                serviceConfig
        )

        menuCandidatoConfig = new MenuCandidatoConfig(
                scanner,
                controllerConfig
        )

        menuEmpresaConfig = new MenuEmpresaConfig(
                scanner,
                controllerConfig
        )
    }

    MenuCandidato criarMenuCandidato() {
        return new MenuCandidato(
                scanner,
                controllerConfig.candidatoController,
                menuCandidatoConfig.menuCandidatoCrud,
                menuCandidatoConfig.menuCurtirVaga
        )
    }

    MenuEmpresa criarMenuEmpresa() {
        return new MenuEmpresa(
                scanner,
                controllerConfig.empresaController,
                menuEmpresaConfig.menuEmpresaCrud,
                menuEmpresaConfig.menuVagaCrud,
                menuEmpresaConfig.menuCurtirCandidato
        )
    }
}