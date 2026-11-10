import { CompetenciaApiClient } from "../api/clientes/CompetenciaApiClient";

export class CompetenciaService {

    constructor(
        private competenciaApiClient: CompetenciaApiClient
    ) {}

    async buscarTodas(): Promise<string[]> {
        return await this.competenciaApiClient.buscarTodas()
    } 
}
