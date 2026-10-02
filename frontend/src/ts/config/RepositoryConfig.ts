import { CandidatoRepository } from "../repository/CandidatoRepository"
import { EmpresaRepository } from "../repository/EmpresaRepository"
import { VagaRepository } from "../repository/VagaRepository"
import { CurtidaVagaRepository } from "../repository/CurtidaVagaRepository"
import { CurtidaCandidatoRepository } from "../repository/CurtidaCandidatoRepository"

export class RepositoryConfig {

    readonly candidatoRepository: CandidatoRepository
    readonly empresaRepository: EmpresaRepository
    readonly vagaRepository: VagaRepository
    readonly curtidaVagaRepository: CurtidaVagaRepository
    readonly curtidaCandidatoRepository: CurtidaCandidatoRepository

    constructor() {
        this.candidatoRepository = new CandidatoRepository()
        this.empresaRepository = new EmpresaRepository()
        this.vagaRepository = new VagaRepository()
        this.curtidaVagaRepository = new CurtidaVagaRepository()
        this.curtidaCandidatoRepository = new CurtidaCandidatoRepository()
    }
}