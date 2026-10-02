import { CandidatoService } from "../services/CandidatoService"
import { EmpresaService } from "../services/EmpresaService"
import { VagaService } from "../services/VagaService"
import { CurtidaVagaService } from "../services/CurtidaVagaService"
import { CurtidaCandidatoService } from "../services/CurtidaCandidatoService"

import { RepositoryConfig } from "./RepositoryConfig"

export class ServiceConfig {

    constructor(private repositoryConfig: RepositoryConfig) {}

    criarCandidatoService(): CandidatoService {
        return new CandidatoService(
            this.repositoryConfig.candidatoRepository,
            this.repositoryConfig.empresaRepository
        )
    }

    criarEmpresaService(): EmpresaService {
        return new EmpresaService(
            this.repositoryConfig.empresaRepository,
            this.repositoryConfig.candidatoRepository
        )
    }

    criarVagaService(): VagaService {
        return new VagaService(
            this.repositoryConfig.vagaRepository,
            this.repositoryConfig.empresaRepository
        )
    }

    criarCurtidaVagaService(): CurtidaVagaService {
        return new CurtidaVagaService(
            this.repositoryConfig.curtidaVagaRepository,
            this.repositoryConfig.candidatoRepository,
            this.repositoryConfig.vagaRepository
        )
    }

    criarCurtidaCandidatoService(): CurtidaCandidatoService {
        return new CurtidaCandidatoService(
            this.repositoryConfig.curtidaCandidatoRepository,
            this.repositoryConfig.empresaRepository,
            this.repositoryConfig.candidatoRepository
        )
    }
}