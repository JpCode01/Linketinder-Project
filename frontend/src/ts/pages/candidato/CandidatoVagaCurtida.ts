import { CurtidaVagaService } from "../../services/CurtidaVagaService"

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

        for (const vaga of vagasCurtidas) {
            const card = criarCardVaga(
                vaga,
                true
            )

            listaVagas.appendChild(card)
        }
    } catch (erro: unknown) {
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