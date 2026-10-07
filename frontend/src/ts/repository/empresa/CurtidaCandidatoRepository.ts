import { CurtidaCandidato } from "../../models/CurtidaCandidato"
import { CadastroRepository } from "../CadastroRepository";
import { CurtidaCandidatoConsultaRepository } from "../empresa/contrato/CurtidaCandidatoConsultaRepository"
import { StorageFactory } from "../../factory/storage/StorageFactory"

export class CurtidaCandidatoRepository implements 
            CurtidaCandidatoConsultaRepository,
            CadastroRepository<CurtidaCandidato> {

    private readonly storage: Storage

    constructor(storageFactory: StorageFactory) {
        this.storage = storageFactory.criarStorage()
    }
    salvar(curtida: CurtidaCandidato): void {
        const curtidasSalvas = this.storage.getItem("curtidasCandidatos")

        const curtidas: CurtidaCandidato[] =
            curtidasSalvas
                ? JSON.parse(curtidasSalvas)
                : []

        curtidas.push(curtida)

        this.storage.setItem(
            "curtidasCandidatos",
            JSON.stringify(curtidas)
        )
    }

    buscarTodos(): CurtidaCandidato[] {
        const curtidasSalvas = this.storage.getItem("curtidasCandidatos")

        if (curtidasSalvas == null) {
            return []
        }

        return JSON.parse(curtidasSalvas)
    }

    buscarPorEmpresa(idEmpresa: number): CurtidaCandidato[] {
        const curtidas = this.buscarTodos()

        return curtidas.filter(
            curtida => curtida.idEmpresa === idEmpresa
        )
    }

    buscarPorCandidato(idCandidato: number): CurtidaCandidato[] {
        const curtidas = this.buscarTodos()

        return curtidas.filter(
            curtida => curtida.idCandidato === idCandidato
        )
    }

    buscar(idEmpresa: number, idCandidato: number, idVaga: number): CurtidaCandidato | undefined {
        const curtidas = this.buscarTodos()

        return curtidas.find(
            curtida =>
                curtida.idEmpresa === idEmpresa &&
                curtida.idCandidato === idCandidato &&
                curtida.idVaga === idVaga
        )
    }
}