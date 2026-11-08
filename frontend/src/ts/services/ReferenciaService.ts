import { ReferenciaApiClient } from "../api/clientes/ReferenciaApiClient";
import { Estado } from "../models/Estado";
import { Pais } from "../models/Pais";

export class ReferenciaService {

    constructor(
        private referenciaApiClient: ReferenciaApiClient
    ) {}

    async buscarPaises(): Promise<Pais[]> {
        return await this.referenciaApiClient.buscarPaises()
    }

    async buscarEstados(): Promise<Estado[]> {
        return await this.referenciaApiClient.buscarEstados()
    }
}