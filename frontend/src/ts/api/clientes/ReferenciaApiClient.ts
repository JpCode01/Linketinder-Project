import { ApiClient } from "./ApiClient"
import { Pais } from "../../models/Pais"
import { Estado } from "../../models/Estado"

export class ReferenciaApiClient {
    constructor(private api = new ApiClient()) {}

    buscarPaises(): Promise<Pais[]> {
        return this.api.obter("/referencias/paises")
    }

    buscarEstados(): Promise<Estado[]> {
        return this.api.obter("/referencias/estados")
    }
}
