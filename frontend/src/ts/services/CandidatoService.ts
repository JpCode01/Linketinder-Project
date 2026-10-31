import { CandidatoApiClient } from "../api/clientes/CandidatoApiClient";
import { CadastrarCandidatoRequestDTO } from "../api/dto/candidato/CadastrarCandidatoRequestDTO";
import { CandidatoViewDTO } from "../api/dto/candidato/CandidatoViewDTO";

export class CandidatoService {

    constructor(
        private candidatoApiClient: CandidatoApiClient,
    ) {}

    async cadastrar(dados: CadastrarCandidatoRequestDTO): Promise<void> {
        await this.candidatoApiClient.cadastrar(dados)
    }

    async buscarPorId(id: number): Promise<CandidatoViewDTO> {
        return await this.candidatoApiClient.buscarPorId(id)
    }
}