import { RepositoryConfig } from "../../config/RepositoryConfig"
import { ServiceConfig } from "../../config/ServiceConfig"
import { VagaService } from "../../services/VagaService"
import { CurtidaVagaService } from "../../services/CurtidaVagaService"
import { CandidatoApiClient } from "../../api/clientes/CandidatoApiClient"
import { MatchService } from "../../services/MatchService"

import { exibirDadosCandidato } from "./CandidatoPerfil"
import { exibirVagas } from "./CandidatoVaga"
import { exibirVagasCurtidas } from "./CandidatoVagaCurtida"
import { exibirMatchesCandidato } from "./CandidatoMatch"

interface Sessao {
    id: number
    tipo: "CANDIDATO" | "EMPRESA"
}

const repositoryConfig: RepositoryConfig = new RepositoryConfig()
const serviceConfig: ServiceConfig = new ServiceConfig(repositoryConfig)

const matchService: MatchService =
    serviceConfig.criarMatchService()

const vagaService: VagaService =
    serviceConfig.criarVagaService()

const curtidaVagaService: CurtidaVagaService =
    serviceConfig.criarCurtidaVagaService()

const candidatoApiClient: CandidatoApiClient =
    new CandidatoApiClient()

export async function exibirPageCandidato(): Promise<void> {
    const sessao = obterSessao()

    if (sessao == undefined || sessao.tipo !== "CANDIDATO") {
        return
    }

    try {
        const candidatoDto =
            await candidatoApiClient.buscarPorId(sessao.id)

        exibirDadosCandidato(candidatoDto)
    } catch (erro) {
        console.error(
            "Erro ao carregar perfil do candidato:",
            erro
        )
        return
    }

    await Promise.all([
        exibirVagas(
            sessao.id,
            vagaService,
            curtidaVagaService
        ),
        exibirVagasCurtidas(
            sessao.id,
            curtidaVagaService
        ),
        exibirMatchesCandidato(
            sessao.id,
            matchService
        )
    ])
}

function obterSessao(): Sessao | undefined {
    const sessaoSalva = localStorage.getItem("sessao")

    if (sessaoSalva == null) {
        return undefined
    }

    return JSON.parse(sessaoSalva) as Sessao
}