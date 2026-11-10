import { ServiceConfig } from "../../config/ServiceConfig"
import { obterSessaoDoPerfil, tratarErroSessao } from "../login/SessaoNavegacao"
import { configurarCriacaoVagaRecrutador, exibirVagasRecrutador } from "./RecrutadorVaga"
import { exibirInteressadosRecrutador } from "./RecrutadorInteressados"
import { exibirMatchesRecrutador } from "./RecrutadorMatch"

export async function exibirPageRecrutador(): Promise<void> {
    const sessao = obterSessaoDoPerfil("recrutador")
    if (sessao == undefined) return
    const config = new ServiceConfig()
    const vagas = config.criarVagaService()
    const matches = config.criarMatchService()
    const status = document.getElementById("page-status") as HTMLElement
    const executar = async (operacao: () => Promise<void>): Promise<void> => {
        status.textContent = ""
        try {
            await operacao()
        } catch (erro: unknown) {
            if (tratarErroSessao(erro)) return
            status.textContent = "Não foi possível atualizar a página. Recarregue para tentar novamente."
        }
    }
    const atualizarMatches = () => executar(() => exibirMatchesRecrutador(sessao.id, matches))
    const atualizarVagas = () => executar(async () => {
        await exibirVagasRecrutador(sessao.id, vagas)
        await exibirInteressadosRecrutador(sessao.id, vagas, config.criarCurtidaVagaService(),
            config.criarCurtidaCandidatoService(), atualizarMatches)
    })
    await executar(async () => {
        const perfil = await config.criarRecrutadorService().buscarPerfilProprio(sessao.id)
        exibirCampoPerfil("profile-name", perfil.nome)
        exibirCampoPerfil("profile-description", perfil.descricao)
        exibirCampoPerfil("profile-area", perfil.areaRecrutamento.nome)
        exibirCampoPerfil("profile-email", perfil.email)
        await configurarCriacaoVagaRecrutador(sessao.id, vagas, config.criarCompetenciaService(), atualizarVagas)
        await exibirVagasRecrutador(sessao.id, vagas)
        await exibirInteressadosRecrutador(sessao.id, vagas, config.criarCurtidaVagaService(),
            config.criarCurtidaCandidatoService(), atualizarMatches)
        await exibirMatchesRecrutador(sessao.id, matches)
    })
}

function exibirCampoPerfil(id: string, valor: string): void {
    const campo = document.getElementById(id) as HTMLElement
    campo.textContent = valor
}
