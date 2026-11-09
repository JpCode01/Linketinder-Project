import { CurtidaCandidatoApiClient } from "../api/clientes/CurtidaCandidatoApiClient"

export class CurtidaCandidatoService {
    constructor(private curtidaCandidatoApiClient: CurtidaCandidatoApiClient) {}

    async retribuirInteresseEmpresa(idCandidato: number, idVaga: number): Promise<void> {
        await this.curtidaCandidatoApiClient.retribuirInteresseEmpresa(idCandidato, idVaga)
    }

    async retribuirInteresseRecrutador(idCandidato: number, idVaga: number): Promise<void> {
        await this.curtidaCandidatoApiClient.retribuirInteresseRecrutador(idCandidato, idVaga)
    }
}
