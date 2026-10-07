import { Vaga } from "../../models/Vaga"
import { VagaConsultaRepository } from "./contrato/VagaConsultaRepository"
import { CadastroRepository } from "../CadastroRepository";
import { StorageFactory } from "../../factory/storage/StorageFactory"

export class VagaRepository implements
            VagaConsultaRepository,
            CadastroRepository<Vaga> {

    private readonly storage: Storage

    constructor(storageFactory: StorageFactory) {
        this.storage = storageFactory.criarStorage()
    }

    salvar(vaga: Vaga): void {
        const vagasSalvas = this.storage.getItem("vagas")

        const vagas: Vaga[] =
            vagasSalvas
                ? JSON.parse(vagasSalvas)
                : []

        vaga.id = vagas.length > 0
        ? Math.max(...vagas.map(vagaSalva => vagaSalva.id   )) + 1
        : 1

        vagas.push(vaga)

        this.storage.setItem(
            "vagas",
            JSON.stringify(vagas)
        )
    }

    buscarTodos(): Vaga[] {
        const vagasSalvas = this.storage.getItem("vagas")

        if (vagasSalvas == null) {
            return []
        }

        return JSON.parse(vagasSalvas)
    }

    buscarPorId(id: number): Vaga | undefined {
        const vagas = this.buscarTodos()

        return vagas.find(
            vaga => vaga.id === id
        )
    }

    buscarPorEmpresa(idEmpresa: number): Vaga[] {
        const vagas = this.buscarTodos()

        return vagas.filter(
            vaga => vaga.idEmpresa === idEmpresa
        )
    }
}   