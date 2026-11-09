import { ApiClient } from "./ApiClient"
import { CadastrarEmpresaRequestDTO } from "../dto/empresa/CadastrarEmpresaRequestDTO"
import { EmpresaViewDTO } from "../dto/empresa/EmpresaViewDto"

export class EmpresaApiClient {
    constructor(private api = new ApiClient()) {}

    cadastrar(dados: CadastrarEmpresaRequestDTO): Promise<void> {
        return this.api.postar("/empresas", dados)
    }

    buscarPorId(id: number): Promise<EmpresaViewDTO> {
        return this.api.obter(`/empresas/${id}`)
    }
}
