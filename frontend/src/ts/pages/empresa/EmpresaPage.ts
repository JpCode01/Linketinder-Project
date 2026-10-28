import { RepositoryConfig } from "../../config/RepositoryConfig"
import { ServiceConfig } from "../../config/ServiceConfig"
import { EmpresaService } from "../../services/EmpresaService"
import { VagaService } from "../../services/VagaService"
import { CandidatoService } from "../../services/CandidatoService"
import { CurtidaVagaService } from "../../services/CurtidaVagaService"
import { CurtidaCandidatoService } from "../../services/CurtidaCandidatoService"
import { RelatorioCompetenciasService } from "../../services/RelatorioCompetenciasService"

import { exibirDadosEmpresa } from "./EmpresaPerfil"
import {
    carregarOpcoesCompetencias,
    configurarCriacaoVaga,
    exibirVagasEmpresa
} from "./EmpresaVaga"
import { exibirCandidatosEmpresa } from "./EmpresaCandidato"
import { exibirCandidatosCurtidos } from "./EmpresaCandidatoCurtido"
import { criarGraficoCompetencias } from "../../charts/CompetenciaChart"
import { EmpresaApiClient } from "../../api/clientes/EmpresaApiClient"

interface Sessao {
    id: number
    tipo: string
}

const repositoryConfig: RepositoryConfig = new RepositoryConfig()
const serviceConfig: ServiceConfig = new ServiceConfig(repositoryConfig)
const empresaApiClient: EmpresaApiClient = new EmpresaApiClient()

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
        carregarOpcoesCompetencias()

        configurarCriacaoVaga(
            empresa.id,
            vagaService
        )

        exibirVagasEmpresa(
            empresa.id,
            vagaService
        )

        exibirCandidatosEmpresa(
            empresa.id,
            vagaService,
            candidatoService,
            curtidaVagaService,
            curtidaCandidatoService
        )

        exibirCandidatosCurtidos(
            empresa.id,
            vagaService,
            candidatoService,
            curtidaVagaService,
            curtidaCandidatoService
        )

        const contagem =
            relatorioCompetenciasService.contarCompetencias(empresa.id)

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