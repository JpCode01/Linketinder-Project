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

interface Sessao {
    id: number
    tipo: string
}

const repositoryConfig: RepositoryConfig = new RepositoryConfig()
const serviceConfig: ServiceConfig = new ServiceConfig(repositoryConfig)

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

export function exibirPageEmpresa(): void {
    const sessao = obterSessao()

    if (sessao == undefined || sessao.tipo !== "EMPRESA") {
        return
    }

    const empresa = empresaService.buscarPorId(sessao.id)

    exibirDadosEmpresa(empresa)

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
}

function obterSessao(): Sessao | undefined {
    const sessao = localStorage.getItem("sessao")

    if (sessao == null) {
        return undefined
    }

    return JSON.parse(sessao) as Sessao
}