import { CompetenciaApiClient } from "../../api/clientes/CompetenciaApiClient";
import { VagaEmpresaResponseDTO } from "../../api/dto/vaga/VagaEmpresaResponseDTO";
import { Competencia } from "../../models/Competencia"
import { Vaga } from "../../models/Vaga"
import { VagaService } from "../../services/VagaService"

interface DadosVagaFormulario {
    nome: string
    descricao: string
    tipo: string
    localizacao: string
    competencias: Competencia[]
}

export async function carregarOpcoesCompetencias(): Promise<void> {
    const container = document.getElementById("skills-options")

    if (container == null) {
        return
    }

    try {
        const competenciaApiClient = new CompetenciaApiClient()
        const competencias = await competenciaApiClient.buscarTodas()

        container.innerHTML = ""

        competencias.forEach((competencia) => {
            const label = document.createElement("label")
            const checkbox = document.createElement("input")

            checkbox.type = "checkbox"
            checkbox.value = String(competencia.id)
            checkbox.dataset.nome = competencia.nome

            label.appendChild(checkbox)
            label.appendChild(
                document.createTextNode(competencia.nome)
            )

            container.appendChild(label)
        })
    } catch (erro: unknown) {
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

function capturarCompetenciasSelecionadas(): Competencia[] {
    const checkboxes =
        document.querySelectorAll<HTMLInputElement>(
            "#skills-options input[type='checkbox']:checked"
        )

    return Array.from(checkboxes).map(
        (checkbox) => new Competencia(
            Number(checkbox.value),
            checkbox.dataset.nome ?? ""
        )
    )
}

function construirVaga(
    dados: DadosVagaFormulario,
    idEmpresa: number
): Vaga {
    const vaga = new Vaga(
        dados.nome,
        dados.descricao,
        dados.tipo,
        dados.localizacao,
        idEmpresa
    )

    vaga.competencias = dados.competencias

    return vaga
}

export function configurarCriacaoVaga(
    idEmpresa: number,
    vagaService: VagaService
): void {
    const botao = document.getElementById("criar-vaga")

    if (botao == null) {
        return
    }

    botao.onclick = async (): Promise<void> => {
        try {
            const dados = capturarDadosVaga()
            const vaga = construirVaga(dados, idEmpresa)

            await vagaService.salvar(vaga)

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

function criarCardVaga(vaga: VagaEmpresaResponseDTO): HTMLElement {
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

    const checkboxes =
        document.querySelectorAll(
            "#skills-options input[type='checkbox']"
        )

    checkboxes.forEach((checkbox) => {
        (checkbox as HTMLInputElement).checked = false
    })
}