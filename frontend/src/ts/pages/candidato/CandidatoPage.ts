import { ServiceConfig } from "../../config/ServiceConfig";
import { CurtidaVagaService } from "../../services/CurtidaVagaService";
import { MatchService } from "../../services/MatchService";
import { VagaService } from "../../services/VagaService";

import { CandidatoService } from "../../services/CandidatoService";
import { exibirMatchesCandidato } from "./CandidatoMatch";
import { exibirDadosCandidato } from "./CandidatoPerfil";
import { exibirVagas } from "./CandidatoVaga";
import { exibirVagasCurtidas } from "./CandidatoVagaCurtida";

import { SessaoResponseDTO } from "../../api/dto/login/SessaoResponseDTO"

const serviceConfig: ServiceConfig = new ServiceConfig()

const matchService: MatchService =
    serviceConfig.criarMatchService()

const vagaService: VagaService =
    serviceConfig.criarVagaService()

const curtidaVagaService: CurtidaVagaService =
    serviceConfig.criarCurtidaVagaService()

const candidatoService: CandidatoService =
    serviceConfig.criarCandidatoService()

export async function exibirPageCandidato(): Promise<void> {
    const sessao = obterSessao()

    if (sessao == undefined || sessao.tipo !== "candidato") {
        return
    }

    try {
        const candidatoDto =
            await candidatoService.buscarPorId(sessao.id)

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

function obterSessao(): SessaoResponseDTO | undefined {
    const sessaoSalva = localStorage.getItem("sessao")

    if (sessaoSalva == null) {
        return undefined
    }

    return JSON.parse(sessaoSalva) as SessaoResponseDTO
}