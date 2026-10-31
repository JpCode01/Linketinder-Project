import { CompetenciaApiClient } from "../api/clientes/CompetenciaApiClient";
import { Competencia } from "../models/Competencia";

export class CompetenciaService {

    constructor(
        private competenciaApiClient: CompetenciaApiClient
    ) {}

    async buscarTodas(): Promise<Competencia[]> {
        return await this.competenciaApiClient.buscarTodas()
    } 
}