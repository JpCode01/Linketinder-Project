import { VagaService } from "../../services/VagaService"
import { CurtidaVagaService } from "../../services/CurtidaVagaService"
import { CurtidaCandidatoService } from "../../services/CurtidaCandidatoService"
import { criarCardCandidato } from "../empresa/EmpresaCandidato"
import { tratarErroSessao } from "../login/SessaoNavegacao"

export async function exibirInteressadosRecrutador(
    idRecrutador: number,
    vagaService: VagaService,
    curtidaVagaService: CurtidaVagaService,
    curtidaCandidatoService: CurtidaCandidatoService,
    aoRetribuir: () => Promise<void>
): Promise<void> {
    const lista = document.getElementById("candidate-grid") as HTMLElement
    const contador = document.getElementById("candidate-count") as HTMLElement
    lista.textContent = "Carregando interessados..."
    contador.textContent = ""
    try {
        const vagas = await vagaService.buscarPorRecrutador(idRecrutador)
        const cards: HTMLElement[] = []
        for (const vaga of vagas) {
            const candidatos = await curtidaVagaService.buscarCandidatosQueCurtiram(vaga.id)
            const retribuidos = new Set(await curtidaCandidatoService.buscarIdsCandidatosRetribuidosRecrutador(vaga.id))
            for (const candidato of candidatos) {
                const card = criarCardCandidato(candidato, vaga, retribuidos.has(candidato.id), async () => {
                    const botao = card.querySelector(".btn-like") as HTMLButtonElement
                    if (botao.disabled) return
                    botao.disabled = true
                    try {
                        await curtidaCandidatoService.retribuirInteresseRecrutador(candidato.id, vaga.id)
                    } catch (erro: unknown) {
                        if (tratarErroSessao(erro)) return
                        botao.disabled = false
                        window.alert(erro instanceof Error ? erro.message : "Não foi possível retribuir o interesse.")
                        return
                    }
                    botao.textContent = "♥"
                    botao.classList.add("curtido")
                    botao.title = "Candidato curtido"
                    await aoRetribuir()
                })
                cards.push(card)
            }
        }
        lista.innerHTML = ""
        if (cards.length === 0) lista.textContent = "Nenhum interessado nas suas vagas."
        for (const card of cards) lista.appendChild(card)
        contador.textContent = `${cards.length} interesses`
    } catch (erro: unknown) {
        lista.textContent = "Não foi possível carregar os interessados."
        throw erro
    }
}
