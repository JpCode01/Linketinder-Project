import { ServiceConfig } from "../../config/ServiceConfig";
import { CandidatoService } from "../../services/CandidatoService";
import { CurtidaCandidatoService } from "../../services/CurtidaCandidatoService";
import { CurtidaVagaService } from "../../services/CurtidaVagaService";
import { EmpresaService } from "../../services/EmpresaService";
import { RelatorioCompetenciasService } from "../../services/RelatorioCompetenciasService";
import { VagaService } from "../../services/VagaService";

import { EmpresaApiClient } from "../../api/clientes/EmpresaApiClient";
import { criarGraficoCompetencias } from "../../charts/CompetenciaChart";
import { MatchService } from "../../services/MatchService";
import { exibirCandidatosEmpresa } from "./EmpresaCandidato";
import { exibirMatchesEmpresa } from "./EmpresaMatch";
import { exibirDadosEmpresa } from "./EmpresaPerfil";
import {
    carregarOpcoesCompetencias,
    configurarCriacaoVaga,
    exibirVagasEmpresa
} from "./EmpresaVaga";

interface Sessao {
    id: number
    tipo: string
}

const serviceConfig: ServiceConfig = new ServiceConfig()
const empresaApiClient: EmpresaApiClient = new EmpresaApiClient()

const matchService: MatchService =
    serviceConfig.criarMatchService()

const empresaService: EmpresaService =
    serviceConfig.criarEmpresaService()

const vagaService: VagaService =
    serviceConfig.criarVagaService()

const candidatoService: CandidatoService =
    serviceConfig.criarCandidatoService()

const curtidaVagaService: CurtidaVagaService =
    serviceConfig.criarCurtidaVagaService()

const curtidaCandidatoService: CurtidaCandidatoService =
    serviceConfig.criarCurtidaCandidatoService()

const relatorioCompetenciasService: RelatorioCompetenciasService =
    serviceConfig.criarRelatorioCompetenciasService()

export async function exibirPageEmpresa(): Promise<void> {
    const sessao = obterSessao()

    if (sessao == undefined || sessao.tipo !== "EMPRESA") {
        return
    }

    let empresa

    try {
        empresa = await empresaApiClient.buscarPorId(sessao.id)
    } catch (erro: unknown) {
        console.error("Erro ao carregar o perfil da empresa:", erro)

        const mensagem = document.getElementById("profile-name")

        if (mensagem != null) {
            mensagem.textContent = "Não foi possível carregar o perfil."
        }

        return
    }

    exibirDadosEmpresa(empresa)

    try {
        await carregarOpcoesCompetencias()

        configurarCriacaoVaga(
            empresa.id,
            vagaService
        )

        await exibirVagasEmpresa(
            empresa.id,
            vagaService
        )

        await exibirCandidatosEmpresa(
            empresa.id,
            vagaService,
            candidatoService,
            curtidaVagaService,
            curtidaCandidatoService
        )

        await exibirMatchesEmpresa(
            empresa.id,
            matchService
        )

        const contagem =
            await relatorioCompetenciasService.contarCompetencias(empresa.id)

        criarGraficoCompetencias(contagem)
    } catch (erro: unknown) {
        console.error("Erro ao carregar a página da empresa:", erro)
    }
}

function obterSessao(): Sessao | undefined {
    const sessao = localStorage.getItem("sessao")

    if (sessao == null) {
        return undefined
    }

    return JSON.parse(sessao) as Sessao
}