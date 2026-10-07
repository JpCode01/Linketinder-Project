package com.jpcode.factory

import com.jpcode.config.DaoConfig
import com.jpcode.config.MenuCandidatoConfig
import com.jpcode.config.MenuEmpresaConfig
import com.jpcode.config.ServiceConfig
import com.jpcode.view.candidato.MenuCandidato
import com.jpcode.view.empresa.MenuEmpresa

class MenuFactory {
    private final Scanner scanner
    private final DaoConfig daoConfig
    private final ServiceConfig serviceConfig
    private final MenuCandidatoConfig menuCandidatoConfig
    private final MenuEmpresaConfig menuEmpresaConfig

    MenuFactory(Scanner scanner) {
        this.scanner = scanner

        daoConfig = new DaoConfig()
        serviceConfig = new ServiceConfig(daoConfig)

        menuCandidatoConfig = new MenuCandidatoConfig(
                scanner,
                serviceConfig
        )

        menuEmpresaConfig = new MenuEmpresaConfig(
                scanner,
                serviceConfig
        )
    }

    MenuCandidato criarMenuCandidato() {
        return new MenuCandidato(
                scanner,
                serviceConfig.candidatoService,
                menuCandidatoConfig.menuCandidatoCrud,
                menuCandidatoConfig.menuCurtirVaga
        )
    }

    MenuEmpresa criarMenuEmpresa() {
        return new MenuEmpresa(
                scanner,
                serviceConfig.empresaService,
                menuEmpresaConfig.menuEmpresaCrud,
                menuEmpresaConfig.menuVagaCrud,
                menuEmpresaConfig.menuCurtirCandidato
        )
    }
}