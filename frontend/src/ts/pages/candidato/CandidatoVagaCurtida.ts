import { Candidato } from "../../models/Candidato"
import { Vaga } from "../../models/Vaga"
import { VagaService } from "../../services/VagaService"
import { CurtidaVagaService } from "../../services/CurtidaVagaService"

import {
    atualizarContador,
    criarCardVaga
} from "./CandidatoComponentes"

export function exibirVagasCurtidas(
    candidato: Candidato,
    vagaService: VagaService,
    curtidaVagaService: CurtidaVagaService
): void {
    const listaVagas =
        document.getElementById("liked-job-grid")

    const contador =
        document.getElementById("liked-result-count")

    if (listaVagas == null) {
        return
    }

    const vagasCurtidas =
        buscarVagasCurtidas(
            candidato,
            vagaService,
            curtidaVagaService
        )

    listaVagas.innerHTML = ""

    atualizarContador({
        contador: contador,
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
}

function buscarVagasCurtidas(
    candidato: Candidato,
    vagaService: VagaService,
    curtidaVagaService: CurtidaVagaService
): Vaga[] {
    const curtidas =
        curtidaVagaService.buscarPorCandidato(
            candidato.id
        )

    const vagasCurtidas: Vaga[] = []

    for (const curtida of curtidas) {
        const vaga =
            vagaService.buscarPorId(
                curtida.idVaga
            )

        if (vaga != undefined) {
            vagasCurtidas.push(vaga)
        }
    }

    return vagasCurtidas
}