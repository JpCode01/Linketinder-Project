import { ApiClient } from "./ApiClient"

export class CurtidaCandidatoApiClient {
    constructor(private api = new ApiClient()) {}

    retribuirInteresseEmpresa(idCandidato: number, idVaga: number): Promise<void> {
        return this.api.postar("/curtidas/candidatos", { idCandidato, idVaga })
    }

    retribuirInteresseRecrutador(idCandidato: number, idVaga: number): Promise<void> {
        return this.api.postar("/curtidas/candidatos", { idCandidato, idVaga })
    }
}
