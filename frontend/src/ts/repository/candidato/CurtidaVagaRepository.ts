import { CurtidaVaga } from "../../models/CurtidaVaga"
import { CurtidaVagaConsultaRepository } from "./contrato/CurtidaVagaConsultaRepository"
import { CadastroRepository } from "../CadastroRepository";
import { StorageFactory } from "../../factory/storage/StorageFactory"

export class CurtidaVagaRepository implements 
            CurtidaVagaConsultaRepository,
            CadastroRepository<CurtidaVaga> {

    private readonly storage: Storage

    constructor(storageFactory: StorageFactory) {
        this.storage = storageFactory.criarStorage()
    }

    salvar(curtida: CurtidaVaga): void {
        const curtidasSalvas = this.storage.getItem("curtidasVagas")

        const curtidas: CurtidaVaga[] =
            curtidasSalvas
                ? JSON.parse(curtidasSalvas)
                : []

        curtidas.push(curtida)

        this.storage.setItem(
            "curtidasVagas",
            JSON.stringify(curtidas)
        )
    }

    buscarTodos(): CurtidaVaga[] {
        const curtidasSalvas = this.storage.getItem("curtidasVagas")

        if (curtidasSalvas == null) {
            return []
        }

        return JSON.parse(curtidasSalvas)
    }

    buscarPorCandidato(idCandidato: number): CurtidaVaga[] {
        const curtidas = this.buscarTodos()

        return curtidas.filter(
            curtida => curtida.idCandidato === idCandidato
        )
    }

    buscarPorVaga(idVaga: number): CurtidaVaga[] {
        const curtidas = this.buscarTodos()

        return curtidas.filter(
            curtida => curtida.idVaga === idVaga
        )
    }

    buscar(idCandidato: number, idVaga: number): CurtidaVaga | undefined {
        const curtidas = this.buscarTodos()

        return curtidas.find(
            curtida =>
                curtida.idCandidato === idCandidato &&
                curtida.idVaga === idVaga
        )
    }
}