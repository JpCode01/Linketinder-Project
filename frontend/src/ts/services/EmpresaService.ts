import { EmpresaApiClient } from "../api/clientes/EmpresaApiClient";
import { CadastrarEmpresaRequestDTO } from "../api/dto/empresa/CadastrarEmpresaRequestDTO";

export class EmpresaService {

    constructor(
        private empresaApiClient: EmpresaApiClient
    ) {}

    async cadastrar(dados: CadastrarEmpresaRequestDTO): Promise<void> {
        await this.empresaApiClient.cadastrar(dados)
    }
}