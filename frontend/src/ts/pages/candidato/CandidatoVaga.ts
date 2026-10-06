import { Candidato } from "../../models/Candidato"
import { Vaga } from "../../models/Vaga"
import { CurtidaVaga } from "../../models/CurtidaVaga"
import { VagaService } from "../../services/VagaService"
import { CurtidaVagaService } from "../../services/CurtidaVagaService"
import { VagaJaCurtidaException } from "../../exceptions/vaga/VagaJaCurtidaException"

import {
    atualizarContador,
    criarCardVaga,
    atualizarCardCurtido
} from "./CandidatoComponentes"

export function exibirVagas(
    candidato: Candidato,
    vagaService: VagaService,
    curtidaVagaService: CurtidaVagaService
): void {
    const listaVagas =
        document.getElementById("job-grid")

    const contador =
        document.getElementById("result-count")

    if (listaVagas == null) {
        return
    }

    const vagas = vagaService.buscarTodos()

    const curtidas =
        curtidaVagaService.buscarPorCandidato(
            candidato.id
        )

    listaVagas.innerHTML = ""

    atualizarContador({
        contador: contador,
        quantidade: vagas.length,
        singular: "vaga",
        plural: "vagas"
    })

    for (const vaga of vagas) {
        const vagaJaCurtida =
            vagaFoiCurtida(vaga, curtidas)

        const card = criarCardVaga(
            vaga,
            vagaJaCurtida,
            () =>
                curtirVaga(
                    candidato.id,
                    vaga,
                    card,
                    curtidaVagaService
                )
        )

        listaVagas.appendChild(card)
    }
}

function curtirVaga(
    idCandidato: number,
    vaga: Vaga,
    card: HTMLElement,
    curtidaVagaService: CurtidaVagaService
): void {
    try {
        curtidaVagaService.curtir(
            idCandidato,
            vaga.id
        )

        atualizarCardCurtido(card)
    } catch (erro) {
        if (erro instanceof VagaJaCurtidaException) {
            atualizarCardCurtido(card)
            return
        }

        throw erro
    }
}

function vagaFoiCurtida(
    vaga: Vaga,
    curtidas: CurtidaVaga[]
): boolean {
    return curtidas.some(
        curtida => curtida.idVaga === vaga.id
    )
}