import { ApiClient } from "./ApiClient"

export class CompetenciaApiClient {
    constructor(private api = new ApiClient()) {}

    buscarTodas(): Promise<string[]> {
        return this.api.obter("/competencias")
    }
}
