import { RecrutadorService } from "../../services/RecrutadorService"
import { tratarErroSessao } from "../login/SessaoNavegacao"

export async function configurarCadastroRecrutadorEmpresa(
    idEmpresa: number,
    recrutadorService: RecrutadorService
): Promise<void> {
    const formulario = document.getElementById("recruiter-form") as HTMLFormElement | null
    const area = document.getElementById("recruiter-area") as HTMLSelectElement | null
    const botao = document.getElementById("cadastrar-recrutador") as HTMLButtonElement | null
    const mensagem = document.getElementById("recruiter-status")
    if (formulario == null || area == null || botao == null || mensagem == null) return

    botao.disabled = true
    let carregado = false
    let enviando = false
    const valor = (id: string): string => (document.getElementById(id) as HTMLInputElement).value
    formulario.onsubmit = async (evento): Promise<void> => {
        evento.preventDefault()
        if (!carregado || enviando || !formulario.reportValidity()) return
        enviando = true
        botao.disabled = true
        mensagem.textContent = "Cadastrando recrutador..."
        try {
            await recrutadorService.cadastrarPelaEmpresa(idEmpresa, {
                nome: valor("recruiter-name").trim(),
                cpf: valor("recruiter-cpf").trim(),
                email: valor("recruiter-email").trim(),
                dataNascimento: valor("recruiter-birthdate"),
                descricao: valor("recruiter-description").trim(),
                senha: valor("recruiter-password"),
                idAreaRecrutamento: Number(area.value)
            })
            formulario.reset()
            mensagem.textContent = "Recrutador cadastrado com sucesso."
        } catch (erro: unknown) {
            if (tratarErroSessao(erro)) return
            mensagem.textContent = erro instanceof Error ? erro.message : "Não foi possível cadastrar o recrutador."
        } finally {
            enviando = false
            botao.disabled = false
        }
    }

    mensagem.textContent = "Carregando áreas de recrutamento..."
    try {
        const areas = await recrutadorService.buscarAreas()
        area.innerHTML = ""
        const placeholder = document.createElement("option")
        placeholder.value = ""
        placeholder.textContent = "Selecione uma área"
        area.appendChild(placeholder)
        for (const referencia of areas) {
            const opcao = document.createElement("option")
            opcao.value = String(referencia.id)
            opcao.textContent = referencia.nome
            area.appendChild(opcao)
        }
        carregado = areas.length > 0
        botao.disabled = !carregado
        mensagem.textContent = carregado ? "" : "Nenhuma área de recrutamento disponível."
    } catch (erro: unknown) {
        if (tratarErroSessao(erro)) return
        mensagem.textContent = "Não foi possível carregar as áreas. Recarregue a página para tentar novamente."
    }
}
