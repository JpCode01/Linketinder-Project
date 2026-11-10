import { ApiClient } from "./ApiClient"

export class CurtidaCandidatoApiClient {
    constructor(private api = new ApiClient()) {}

    buscarIdsCandidatosRetribuidosEmpresa(idVaga: number): Promise<number[]> {
        return this.api.obter(`/curtidas/candidatos/vaga/${idVaga}/retribuidos`)
    }

    buscarIdsCandidatosRetribuidosRecrutador(idVaga: number): Promise<number[]> {
        return this.api.obter(`/curtidas/candidatos/vaga/${idVaga}/retribuidos`)
    }

    retribuirInteresseEmpresa(idCandidato: number, idVaga: number): Promise<void> {
        return this.api.postar("/curtidas/candidatos", { idCandidato, idVaga })
    }

    retribuirInteresseRecrutador(idCandidato: number, idVaga: number): Promise<void> {
        return this.api.postar("/curtidas/candidatos", { idCandidato, idVaga })
    }
}
