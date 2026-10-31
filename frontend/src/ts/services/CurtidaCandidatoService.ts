import { CurtidaCandidatoApiClient } from "../api/clientes/CurtidaCandidatoApiClient";

export class CurtidaCandidatoService {

    constructor(
        private curtidaCandidatoApiClient: CurtidaCandidatoApiClient
    ) {}

    async curtir(
        idEmpresa: number,
        idCandidato: number,
        idVaga: number
    ): Promise<void> {
        await this.curtidaCandidatoApiClient.curtir(
            idEmpresa,
            idCandidato,
            idVaga
        )
    }

    async jaCurtiuCandidato(
        idEmpresa: number,
        idCandidato: number
    ): Promise<boolean> {
        return await this.curtidaCandidatoApiClient.jaCurtiuCandidato(
            idEmpresa,
            idCandidato
        )
    }
}