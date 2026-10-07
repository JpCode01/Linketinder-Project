import { Candidato } from "../../models/Candidato";
import { CandidatoConsultaRepository } from "./contrato/CandidatoConsultaRepository"
import { CadastroRepository } from "../CadastroRepository";
import { StorageFactory } from "../../factory/storage/StorageFactory"

export class CandidatoRepository implements
            CandidatoConsultaRepository,
            CadastroRepository<Candidato> {

    private readonly storage: Storage

    constructor(storageFactory: StorageFactory) {
        this.storage = storageFactory.criarStorage()
    }

    salvar(candidato: Candidato): void {
        const candidatosSalvos = this.storage.getItem("candidatos")

        const candidatos: Candidato[] =
            candidatosSalvos
                ? JSON.parse(candidatosSalvos)
                : []

        candidato.id = candidatos.length > 0
        ? Math.max(...candidatos.map(candidato => candidato.id)) + 1
        : 1

        candidatos.push(candidato)

        this.storage.setItem(
            "candidatos",
            JSON.stringify(candidatos)
        )
    }

    buscarTodos(): Candidato[] {
        const candidatosSalvos = this.storage.getItem("candidatos")

        if (candidatosSalvos == null) {
            return []
        }

        return JSON.parse(candidatosSalvos)
    }

    buscarPorId(id: number): Candidato | undefined {
        const candidatos = this.buscarTodos()

        return candidatos.find(
            candidato => candidato.id === id
        )
    }

    buscarPorEmail(email: string): Candidato | undefined {
        const candidatos = this.buscarTodos()

        return candidatos.find(
            candidato => candidato.email === email
        )
    }

    buscarPorCpf(cpf: string): Candidato | undefined {
        const candidatos = this.buscarTodos()

        return candidatos.find(
            candidato => candidato.cpf === cpf
        )
    }
}