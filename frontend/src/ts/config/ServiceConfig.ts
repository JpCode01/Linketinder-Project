import { CandidatoService } from "../services/CandidatoService"
import { EmpresaService } from "../services/EmpresaService"
import { VagaService } from "../services/VagaService"
import { CurtidaVagaService } from "../services/CurtidaVagaService"
import { CurtidaCandidatoService } from "../services/CurtidaCandidatoService"
import { RelatorioCompetenciasService } from "../services/RelatorioCompetenciasService"

import { RepositoryConfig } from "./RepositoryConfig"
import { CandidatoApiClient } from "../api/clientes/CandidatoApiClient";
import { EmpresaApiClient } from "../api/clientes/EmpresaApiClient"

export class ServiceConfig {

    constructor(private repositoryConfig: RepositoryConfig) {}

    criarCandidatoService(): CandidatoService {
        return new CandidatoService(
            new CandidatoApiClient(),
            this.repositoryConfig.candidatoRepository
        )
    }

    criarRelatorioCompetenciasService(): RelatorioCompetenciasService {
        return new RelatorioCompetenciasService(
            this.criarVagaService(),
            this.criarCurtidaVagaService(),
            this.criarCandidatoService()
        )
    }

    criarEmpresaService(): EmpresaService {
    return new EmpresaService(
        this.repositoryConfig.empresaRepository,
        this.repositoryConfig.empresaRepository,
        this.repositoryConfig.candidatoRepository,
        new EmpresaApiClient()
    )
}

    criarVagaService(): VagaService {
        return new VagaService(
            this.repositoryConfig.vagaRepository,
            this.repositoryConfig.vagaRepository,
            this.repositoryConfig.empresaRepository
        )
    }

    criarCurtidaVagaService(): CurtidaVagaService {
        return new CurtidaVagaService(
            this.repositoryConfig.curtidaVagaRepository,
            this.repositoryConfig.curtidaVagaRepository,
            this.repositoryConfig.candidatoRepository,
            this.repositoryConfig.vagaRepository
        )
    }

    criarCurtidaCandidatoService(): CurtidaCandidatoService {
        return new CurtidaCandidatoService(
            this.repositoryConfig.curtidaCandidatoRepository,
            this.repositoryConfig.curtidaCandidatoRepository,
            this.repositoryConfig.empresaRepository,
            this.repositoryConfig.candidatoRepository
        )
    }
}