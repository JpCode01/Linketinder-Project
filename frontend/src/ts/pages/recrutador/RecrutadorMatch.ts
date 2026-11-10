import { MatchService } from "../../services/MatchService"
import { criarCardMatchEmpresa } from "../empresa/EmpresaMatch"

export async function exibirMatchesRecrutador(idRecrutador: number, matchService: MatchService): Promise<void> {
    const lista = document.getElementById("liked-candidate-grid") as HTMLElement
    const contador = document.getElementById("liked-candidate-count") as HTMLElement
    lista.textContent = "Carregando matches..."
    contador.textContent = ""
    try {
        const matches = await matchService.buscarPorRecrutador(idRecrutador)
        lista.innerHTML = ""
        if (matches.length === 0) lista.textContent = "Você ainda não tem matches."
        for (const match of matches) lista.appendChild(criarCardMatchEmpresa(match))
        contador.textContent = `${matches.length} matches`
    } catch (erro: unknown) {
        lista.textContent = "Não foi possível carregar os matches."
        throw erro
    }
}
