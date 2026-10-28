import { EmpresaViewDTO } from "../dto/empresa/EmpresaViewDto"

export class EmpresaApiClient {
    private readonly baseUrl: string = "http://localhost:8080/empresas"

    async buscarPorId(id: number): Promise<EmpresaViewDTO> {
        const resposta = await fetch(`${this.baseUrl}/${id}`)

        if (resposta.status === 404) {
            throw new Error(`Empresa de ID ${id} não encontrada.`)
        }

        if (!resposta.ok) {
            throw new Error(`Erro ao consultar empresa: HTTP ${resposta.status}`)
        }

        return await resposta.json() as EmpresaViewDTO
    }
}