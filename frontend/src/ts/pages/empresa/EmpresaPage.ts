import { ServiceConfig } from "../../config/ServiceConfig";
import { CurtidaCandidatoService } from "../../services/CurtidaCandidatoService";
import { CurtidaVagaService } from "../../services/CurtidaVagaService";
import { RelatorioCompetenciasService } from "../../services/RelatorioCompetenciasService";
import { VagaService } from "../../services/VagaService";

import { EmpresaApiClient } from "../../api/clientes/EmpresaApiClient";
import { criarGraficoCompetencias } from "../../charts/CompetenciaChart";
import { MatchService } from "../../services/MatchService";
import { exibirCandidatosEmpresa } from "./EmpresaCandidato";
import { exibirMatchesEmpresa } from "./EmpresaMatch";
import { exibirDadosEmpresa } from "./EmpresaPerfil";
import { configurarCadastroRecrutadorEmpresa } from "./EmpresaRecrutador";
import {
    carregarOpcoesCompetencias,
    configurarCriacaoVaga,
    exibirVagasEmpresa
} from "./EmpresaVaga";

import { obterSessaoDoPerfil, tratarErroSessao } from "../login/SessaoNavegacao"

const serviceConfig: ServiceConfig = new ServiceConfig()
const empresaApiClient: EmpresaApiClient = new EmpresaApiClient()

const matchService: MatchService =
    serviceConfig.criarMatchService()

const vagaService: VagaService =
    serviceConfig.criarVagaService()

const curtidaVagaService: CurtidaVagaService =
    serviceConfig.criarCurtidaVagaService()

const curtidaCandidatoService: CurtidaCandidatoService =
    serviceConfig.criarCurtidaCandidatoService()

const relatorioCompetenciasService: RelatorioCompetenciasService =
    serviceConfig.criarRelatorioCompetenciasService()

export async function exibirPageEmpresa(): Promise<void> {
    const sessao = obterSessaoDoPerfil("empresa")

    if (sessao == undefined || sessao.tipo !== "empresa") {
        return
    }

    let empresa

    try {
        empresa = await empresaApiClient.buscarPorId(sessao.id)
    } catch (erro: unknown) {
        if (tratarErroSessao(erro)) return
        console.error("Erro ao carregar o perfil da empresa:", erro)

        const mensagem = document.getElementById("profile-name")

        if (mensagem != null) {
            mensagem.textContent = "Não foi possível carregar o perfil."
        }

        return
    }

    exibirDadosEmpresa(empresa)

    await configurarCadastroRecrutadorEmpresa(empresa.id, serviceConfig.criarRecrutadorService())

    try {
        await carregarOpcoesCompetencias()

        configurarCriacaoVaga(
            vagaService
        )

        await exibirVagasEmpresa(
            empresa.id,
            vagaService
        )

        await exibirCandidatosEmpresa(
            empresa.id,
            vagaService,
            curtidaVagaService,
            curtidaCandidatoService,
            () => exibirMatchesEmpresa(empresa.id, matchService)
        )

        await exibirMatchesEmpresa(
            empresa.id,
            matchService
        )

        const contagem =
            await relatorioCompetenciasService.contarCompetencias(empresa.id)

        criarGraficoCompetencias(contagem)
    } catch (erro: unknown) {
        if (tratarErroSessao(erro)) return
        console.error("Erro ao carregar a página da empresa:", erro)
    }
}
