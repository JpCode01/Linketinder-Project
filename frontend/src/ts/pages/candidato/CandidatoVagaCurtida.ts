import { CurtidaVagaService } from "../../services/CurtidaVagaService"
import { tratarErroSessao } from "../login/SessaoNavegacao"

import {
    atualizarContador,
    criarCardVaga
} from "./CandidatoComponentes"

export async function exibirVagasCurtidas(
    idCandidato: number,
    curtidaVagaService: CurtidaVagaService
): Promise<void> {
    const listaVagas =
        document.getElementById("liked-job-grid")

    const contador =
        document.getElementById("liked-result-count")

    if (listaVagas == null) {
        return
    }

    listaVagas.innerHTML = ""
    if (contador != null) contador.textContent = "Carregando vagas curtidas..."

    try {
        const vagasCurtidas =
            await curtidaVagaService.buscarVagasCurtidas(
                idCandidato
            )

        atualizarContador({
            contador,
            quantidade: vagasCurtidas.length,
            singular: "vaga curtida",
            plural: "vagas curtidas"
        })

        if (vagasCurtidas.length === 0) {
            const vazio = document.createElement("p")
            vazio.textContent = "Você ainda não curtiu vagas."
            listaVagas.appendChild(vazio)
        }

        for (const vaga of vagasCurtidas) {
            const card = criarCardVaga(
                vaga,
                true
            )

            listaVagas.appendChild(card)
        }
    } catch (erro: unknown) {
        if (tratarErroSessao(erro)) return
        console.error(
            "Erro ao carregar vagas curtidas:",
            erro
        )

        if (contador != null) {
            contador.textContent =
                "Erro ao carregar vagas curtidas"
        }
    }
}
