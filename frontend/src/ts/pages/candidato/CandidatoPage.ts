import { RepositoryConfig } from "../../config/RepositoryConfig"
import { ServiceConfig } from "../../config/ServiceConfig"
import { CandidatoService } from "../../services/CandidatoService"
import { VagaService } from "../../services/VagaService"
import { CurtidaVagaService } from "../../services/CurtidaVagaService"
import { CandidatoApiClient } from "../../api/clientes/CandidatoApiClient"

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
        console.error("Erro ao carregar perfil do candidato:", erro)
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