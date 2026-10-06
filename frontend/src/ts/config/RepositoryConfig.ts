import { CandidatoRepository } from "../repository/candidato/CandidatoRepository"
import { EmpresaRepository } from "../repository/empresa/EmpresaRepository"
import { VagaRepository } from "../repository/vaga/VagaRepository"
import { CurtidaVagaRepository } from "../repository/candidato/CurtidaVagaRepository"
import { CurtidaCandidatoRepository } from "../repository/empresa/CurtidaCandidatoRepository"

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