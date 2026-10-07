import { CandidatoRepository } from "../repository/candidato/CandidatoRepository"
import { EmpresaRepository } from "../repository/empresa/EmpresaRepository"
import { VagaRepository } from "../repository/vaga/VagaRepository"
import { CurtidaVagaRepository } from "../repository/candidato/CurtidaVagaRepository"
import { CurtidaCandidatoRepository } from "../repository/empresa/CurtidaCandidatoRepository"
import { StorageFactory } from "../factory/storage/StorageFactory"
import { LocalStorageFactory } from "../factory/storage/LocalStorageFactory"

export class RepositoryConfig {

    readonly candidatoRepository: CandidatoRepository
    readonly empresaRepository: EmpresaRepository
    readonly vagaRepository: VagaRepository
    readonly curtidaVagaRepository: CurtidaVagaRepository
    readonly curtidaCandidatoRepository: CurtidaCandidatoRepository

    readonly storageFactory: StorageFactory

    constructor() {
        this.storageFactory = new LocalStorageFactory()

        this.candidatoRepository = new CandidatoRepository(
            this.storageFactory
        )

        this.empresaRepository = new EmpresaRepository(
            this.storageFactory
        )

        this.vagaRepository = new VagaRepository(
            this.storageFactory
        )

        this.curtidaVagaRepository = new CurtidaVagaRepository(
            this.storageFactory
        )

        this.curtidaCandidatoRepository = new CurtidaCandidatoRepository(
            this.storageFactory
        )
    }
}