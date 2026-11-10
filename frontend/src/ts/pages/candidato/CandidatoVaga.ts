import { VagaAnonimaResponseDTO } from "../../api/dto/vaga/VagaAnonimaResponseDTO"
import { VagaService } from "../../services/VagaService"
import { CurtidaVagaService } from "../../services/CurtidaVagaService"
import { tratarErroSessao } from "../login/SessaoNavegacao"
import { exibirVagasCurtidas } from "./CandidatoVagaCurtida"

import {
    atualizarContador,
    criarCardVaga,
    atualizarCardCurtido
} from "./CandidatoComponentes"

export async function exibirVagas(
    idCandidato: number,
    vagaService: VagaService,
    curtidaVagaService: CurtidaVagaService
): Promise<void> {
    const listaVagas =
        document.getElementById("job-grid")

    const contador =
        document.getElementById("result-count")

    if (listaVagas == null) {
        return
    }

    listaVagas.innerHTML = ""
    if (contador != null) contador.textContent = "Carregando vagas..."

    try {
        const vagas =
            await vagaService.buscarTodasAsVagasAnonimas()

        const vagasCurtidas = await curtidaVagaService.buscarVagasCurtidas(idCandidato)
        const idsVagasCurtidas = new Set(vagasCurtidas.map(vaga => vaga.id))

        if (vagas.length === 0) {
            const vazio = document.createElement("p")
            vazio.textContent = "Nenhuma vaga disponível."
            listaVagas.appendChild(vazio)
        }

        atualizarContador({
            contador,
            quantidade: vagas.length,
            singular: "vaga",
            plural: "vagas"
        })

        for (const vaga of vagas) {
            const vagaJaCurtida =
                idsVagasCurtidas.has(vaga.id)

            const card = criarCardVaga(
                vaga,
                vagaJaCurtida,
                () => {
                    void curtirVaga(
                        vaga,
                        card,
                        idCandidato,
                        curtidaVagaService
                    )
                }
            )

            listaVagas.appendChild(card)
        }
    } catch (erro: unknown) {
        if (tratarErroSessao(erro)) return
        console.error("Erro ao carregar vagas:", erro)

        if (contador != null) {
            contador.textContent = "Erro ao carregar vagas"
        }
    }
}

async function curtirVaga(
    vaga: VagaAnonimaResponseDTO,
    card: HTMLElement,
    idCandidato: number,
    curtidaVagaService: CurtidaVagaService
): Promise<void> {
    const botao =
        card.querySelector<HTMLButtonElement>(".btn-like")

    if (botao == null || botao.disabled) {
        return
    }

    botao.disabled = true
    card.querySelector(".like-error")?.remove()

    try {
        await curtidaVagaService.curtirVaga(
            vaga.id
        )

        atualizarCardCurtido(card)
        await exibirVagasCurtidas(idCandidato, curtidaVagaService)
    } catch (erro: unknown) {
        if (tratarErroSessao(erro)) return
        console.error("Erro ao curtir vaga:", erro)

        botao.disabled = false
        const mensagem = document.createElement("p")
        mensagem.className = "like-error"
        mensagem.setAttribute("role", "alert")
        mensagem.textContent = "Não foi possível curtir a vaga. Tente novamente."
        card.appendChild(mensagem)
    }
}
