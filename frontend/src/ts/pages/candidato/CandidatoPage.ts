import { RepositoryConfig } from "../../config/RepositoryConfig"
import { ServiceConfig } from "../../config/ServiceConfig"
import { CandidatoService } from "../../services/CandidatoService"
import { VagaService } from "../../services/VagaService"
import { CurtidaVagaService } from "../../services/CurtidaVagaService"

import { exibirDadosCandidato } from "./CandidatoPerfil"
import { exibirVagas } from "./CandidatoVaga"
import { exibirVagasCurtidas } from "./CandidatoVagaCurtida"

interface Sessao {
    id: number
    tipo: "CANDIDATO" | "EMPRESA"
}

const repositoryConfig: RepositoryConfig = new RepositoryConfig()
const serviceConfig: ServiceConfig = new ServiceConfig(repositoryConfig)

const candidatoService: CandidatoService =
    serviceConfig.criarCandidatoService()

const vagaService: VagaService =
    serviceConfig.criarVagaService()

const curtidaVagaService: CurtidaVagaService =
    serviceConfig.criarCurtidaVagaService()

export function exibirPageCandidato(): void {
    const sessao = obterSessao()

    if (sessao == undefined || sessao.tipo !== "CANDIDATO") {
        return
    }

    const candidato = candidatoService.buscarPorId(sessao.id)

    exibirDadosCandidato(candidato)

    exibirVagas(
        candidato,
        vagaService,
        curtidaVagaService
    )

    exibirVagasCurtidas(
        candidato,
        vagaService,
        curtidaVagaService
    )
}

function obterSessao(): Sessao | undefined {
    const sessaoSalva = localStorage.getItem("sessao")

    if (sessaoSalva == null) {
        return undefined
    }

    return JSON.parse(sessaoSalva) as Sessao
}