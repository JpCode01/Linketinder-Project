import { SeletorCompetencias } from "../../components/SeletorCompetencias"
import { CompetenciaApiClient } from "../../api/clientes/CompetenciaApiClient";
import { VagaEmpresaResponseDTO } from "../../api/dto/vaga/VagaEmpresaResponseDTO";
import { VagaService } from "../../services/VagaService"

interface DadosVagaFormulario {
    nome: string
    descricao: string
    tipo: string
    localizacao: string
    competencias: string[]
}

let seletorCompetencias: SeletorCompetencias | undefined

export async function carregarOpcoesCompetencias(): Promise<void> {
    seletorCompetencias = undefined
    const botao = document.getElementById("criar-vaga") as HTMLButtonElement | null
    if (botao != null) botao.disabled = true
    const container = document.getElementById("skills-options")

    if (container == null) {
        return
    }

    container.textContent = "Carregando competências..."
    try {
        const competenciaApiClient = new CompetenciaApiClient()
        const competencias = await competenciaApiClient.buscarTodas()
        seletorCompetencias = new SeletorCompetencias(container, competencias)
        if (botao != null) botao.disabled = false
    } catch (erro: unknown) {
        container.textContent = "Não foi possível carregar as competências. Recarregue a página para tentar novamente."
        const mensagem = erro instanceof Error
            ? erro.message
            : "Não foi possível carregar as competências."
        window.alert(mensagem)
    }
}


function capturarDadosVaga(): DadosVagaFormulario {
    const nome =
        (document.getElementById("job-title") as HTMLInputElement).value

    const tipo =
        (document.getElementById("job-type") as HTMLSelectElement).value

    const localizacao =
        (document.getElementById("job-location") as HTMLInputElement).value

    const descricao =
        (document.getElementById("job-description") as HTMLTextAreaElement).value

    const competencias = capturarCompetenciasSelecionadas()

    return {
        nome,
        descricao,
        tipo,
        localizacao,
        competencias
    }
}

function capturarCompetenciasSelecionadas(): string[] {
    if (seletorCompetencias == undefined) {
        throw new Error("Competências ainda não carregadas.")
    }
    return seletorCompetencias.obterSelecionadas()
}

export function configurarCriacaoVaga(
    vagaService: VagaService
): void {
    const botao = document.getElementById("criar-vaga")

    if (botao == null) {
        return
    }

    botao.onclick = async (): Promise<void> => {
        try {
            const dados = capturarDadosVaga()
            await vagaService.criarVagaEmpresa(dados)

            limparFormularioVaga()
            window.alert("Vaga cadastrada com sucesso.")
        } catch (erro: unknown) {
            const mensagem = erro instanceof Error
                ? erro.message
                : "Não foi possível cadastrar a vaga."

            window.alert(mensagem)
        }
    }
}

export async function exibirVagasEmpresa(
    idEmpresa: number,
    vagaService: VagaService
): Promise<void> {
    const lista = document.getElementById("job-list")

    if (lista == null) {
        return
    }

    const vagas = await vagaService.buscarPorEmpresa(idEmpresa)

    lista.innerHTML = ""

    vagas.forEach((vaga) => {
        lista.appendChild(
            criarCardVaga(vaga)
        )
    })
}

export function criarCardVaga(vaga: VagaEmpresaResponseDTO): HTMLElement {
    const article = document.createElement("article")
    article.className = "company-job-card"

    const titulo = document.createElement("h3")
    titulo.textContent = vaga.nome

    const descricao = document.createElement("p")
    descricao.className = "job-description"
    descricao.textContent = vaga.descricao

    const tags = document.createElement("div")
    tags.className = "job-tags"

    vaga.competencias.forEach((competencia) => {
        const tag = document.createElement("span")
        tag.textContent = competencia.nome

        tags.appendChild(tag)
    })

    const localizacao = document.createElement("span")
    localizacao.className = "job-location"
    localizacao.textContent = vaga.local

    const curtidas = document.createElement("span")
    curtidas.textContent =
        `Candidatos que curtiram: ${vaga.qtdeCandidatosQueCurtiram}`

    article.appendChild(titulo)
    article.appendChild(descricao)
    article.appendChild(tags)
    article.appendChild(localizacao)
    article.appendChild(curtidas)

    const autoria = document.createElement("p")
    autoria.textContent = vaga.idRecrutador == null
        ? "Criação direta pela empresa"
        : `Recrutador autor: ${vaga.nomeRecrutador} — ${vaga.areaRecrutamento}`
    article.appendChild(autoria)

    return article
}


function limparFormularioVaga(): void {
    const titulo =
        document.getElementById("job-title") as HTMLInputElement

    const tipo =
        document.getElementById("job-type") as HTMLSelectElement

    const localizacao =
        document.getElementById("job-location") as HTMLInputElement

    const descricao =
        document.getElementById("job-description") as HTMLTextAreaElement

    titulo.value = ""
    tipo.value = "CLT"
    localizacao.value = ""
    descricao.value = ""

    seletorCompetencias?.limpar()
}
