import { VagaAnonimaResponseDTO } from "../../api/dto/vaga/VagaAnonimaResponseDTO"
import { VagaService } from "../../services/VagaService"
import { CurtidaVagaService } from "../../services/CurtidaVagaService"

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

    try {
        const vagas =
            await vagaService.buscarTodasAsVagasAnonimas()

        const estadosCurtidas = await Promise.all(
            vagas.map(async (vaga) => ({
                id: vaga.id,
                jaCurtiu: await curtidaVagaService.jaCurtiuVaga(
                    idCandidato,
                    vaga.id
                )
            }))
        )

        const idsVagasCurtidas = new Set<number>(
            estadosCurtidas
                .filter(estado => estado.jaCurtiu)
                .map(estado => estado.id)
        )

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
                        idCandidato,
                        vaga,
                        card,
                        curtidaVagaService
                    )
                }
            )

            listaVagas.appendChild(card)
        }
    } catch (erro: unknown) {
        console.error("Erro ao carregar vagas:", erro)

        if (contador != null) {
            contador.textContent = "Erro ao carregar vagas"
        }
    }
}

async function curtirVaga(
    idCandidato: number,
    vaga: VagaAnonimaResponseDTO,
    card: HTMLElement,
    curtidaVagaService: CurtidaVagaService
): Promise<void> {
    const botao =
        card.querySelector<HTMLButtonElement>(".btn-like")

    if (botao == null || botao.disabled) {
        return
    }

    botao.disabled = true

    try {
        await curtidaVagaService.curtirViaApi(
            idCandidato,
            vaga.id
        )

        atualizarCardCurtido(card)
    } catch (erro: unknown) {
        console.error("Erro ao curtir vaga:", erro)

        botao.disabled = false
    }
}