import { CompetenciaService } from "../../services/CompetenciaService"
import { VagaService } from "../../services/VagaService"
import { SeletorCompetencias } from "../../components/SeletorCompetencias"
import { criarCardVaga } from "../empresa/EmpresaVaga"
import { tratarErroSessao } from "../login/SessaoNavegacao"

export async function configurarCriacaoVagaRecrutador(
    idRecrutador: number,
    vagaService: VagaService,
    competenciaService: CompetenciaService,
    aoCriar: () => Promise<void>
): Promise<void> {
    const formulario = document.getElementById("job-form") as HTMLFormElement
    const botao = document.getElementById("criar-vaga") as HTMLButtonElement
    const container = document.getElementById("skills-options") as HTMLElement
    const status = document.getElementById("job-status") as HTMLElement
    botao.disabled = true
    formulario.onsubmit = evento => evento.preventDefault()
    container.textContent = "Carregando competências..."
    let seletor: SeletorCompetencias
    try {
        seletor = new SeletorCompetencias(container, await competenciaService.buscarTodas())
    } catch (erro: unknown) {
        if (tratarErroSessao(erro)) throw erro
        container.textContent = "Não foi possível carregar as competências. Recarregue a página para tentar novamente."
        return
    }
    botao.disabled = false
    formulario.onsubmit = async (evento): Promise<void> => {
        evento.preventDefault()
        if (botao.disabled || !formulario.reportValidity()) return
        botao.disabled = true
        status.textContent = "Publicando vaga..."
        try {
            await vagaService.criarVagaRecrutador(idRecrutador, {
                nome: (document.getElementById("job-title") as HTMLInputElement).value,
                descricao: (document.getElementById("job-description") as HTMLTextAreaElement).value,
                local: (document.getElementById("job-location") as HTMLInputElement).value,
                competencias: seletor.obterSelecionadas()
            })
        } catch (erro: unknown) {
            if (tratarErroSessao(erro)) return
            status.textContent = erro instanceof Error ? erro.message : "Não foi possível publicar a vaga."
            botao.disabled = false
            return
        }
        formulario.reset()
        seletor.limpar()
        status.textContent = "Vaga cadastrada com sucesso."
        botao.disabled = false
        await aoCriar()
    }
}

export async function exibirVagasRecrutador(idRecrutador: number, vagaService: VagaService): Promise<void> {
    const lista = document.getElementById("job-list") as HTMLElement
    lista.textContent = "Carregando vagas..."
    try {
        const vagas = await vagaService.buscarPorRecrutador(idRecrutador)
        lista.innerHTML = ""
        if (vagas.length === 0) lista.textContent = "Você ainda não tem vagas."
        for (const vaga of vagas) lista.appendChild(criarCardVaga(vaga))
    } catch (erro: unknown) {
        lista.textContent = "Não foi possível carregar as vagas."
        throw erro
    }
}
