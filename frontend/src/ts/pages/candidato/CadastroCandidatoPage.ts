import { Candidato } from "../../models/Candidato"
import { Competencia } from "../../models/Competencia"

import { CandidatoService } from "../../services/CandidatoService"

import { RepositoryConfig } from "../../config/RepositoryConfig"
import { ServiceConfig } from "../../config/ServiceConfig"

import { EmailJaCadastradoException } from "../../exceptions/EmailJaCadastradoException"
import { CpfJaCadastradoException } from "../../exceptions/CpfJaCadastradoException"

interface DadosCandidatoFormulario {
    nome: string
    email: string
    cpf: string
    idade: number
    formacao: string
    estado: string
    cep: string
    descricao: string
    competencias: Competencia[]
}

const repositoryConfig: RepositoryConfig = new RepositoryConfig()
const serviceConfig: ServiceConfig = new ServiceConfig(repositoryConfig)

const candidatoService: CandidatoService =
    serviceConfig.criarCandidatoService()

export function cadastrarCandidato(): void {
    carregarOpcoesCompetencias()

    document.getElementById("register-candidate")!.onclick = (): void => {
        const dados: DadosCandidatoFormulario = capturarDadosCandidato()

        const candidato: Candidato =
            construirCandidato(dados)

        try {
            candidatoService.salvar(candidato)

            adicionarNaSessao(candidato.id, "CANDIDATO")

            window.location.href = "./candidato.html"

        } catch (erro) {

            if (erro instanceof EmailJaCadastradoException) {
                mostrarErroEmail("E-mail já cadastrado.")
                return
            }

            if (erro instanceof CpfJaCadastradoException) {
                mostrarErroCpf("CPF já cadastrado.")
                return
            }
        }
    }
}

function mostrarErroEmail(mensagem: string): void {
    const inputEmail =
        document.getElementById("email") as HTMLInputElement

    inputEmail.classList.add("input-error")

    document.getElementById("erro-email")?.remove()

    const mensagemErro: HTMLSpanElement =
        document.createElement("span")

    mensagemErro.id = "erro-email"
    mensagemErro.classList.add("error-message")
    mensagemErro.textContent = mensagem

    inputEmail.parentElement!.appendChild(mensagemErro)
}

function mostrarErroCpf(mensagem: string): void {
    const inputCpf =
        document.getElementById("cpf") as HTMLInputElement

    inputCpf.classList.add("input-error")

    document.getElementById("erro-cpf")?.remove()

    const mensagemErro: HTMLSpanElement =
        document.createElement("span")

    mensagemErro.id = "erro-cpf"
    mensagemErro.classList.add("error-message")
    mensagemErro.textContent = mensagem

    inputCpf.parentElement!.appendChild(mensagemErro)
}

function construirCandidato(dados: DadosCandidatoFormulario): Candidato {
    const candidato: Candidato = new Candidato(
        dados.cpf,
        dados.idade,
        dados.formacao,
        dados.nome,
        dados.email,
        dados.estado,
        dados.cep,
        dados.descricao
    )

    candidato.competencias = dados.competencias

    return candidato
}


function carregarOpcoesCompetencias(): void {
    const containerCompetencias =
        document.getElementById("skills-options")!

    for (const competencia of Object.values(Competencia)) {
        const opcaoCompetencia = document.createElement("div")
        opcaoCompetencia.classList.add("skill-option")

        const checkbox = document.createElement("input")
        checkbox.type = "checkbox"
        checkbox.name = "competencias"
        checkbox.value = competencia
        checkbox.id = `competencia-${competencia}`

        const label = document.createElement("label")
        label.htmlFor = checkbox.id
        label.textContent = competencia

        opcaoCompetencia.appendChild(checkbox)
        opcaoCompetencia.appendChild(label)

        containerCompetencias.appendChild(opcaoCompetencia)
    }
}

function capturarDadosCandidato(): DadosCandidatoFormulario {
    const nome: string =
        (document.getElementById("name") as HTMLInputElement).value

    const email: string =
        (document.getElementById("email") as HTMLInputElement).value

    const cpf: string =
        (document.getElementById("cpf") as HTMLInputElement).value

    const idade: number =
        Number((document.getElementById("age") as HTMLInputElement).value)

    const formacao: string =
        (document.getElementById("education") as HTMLInputElement).value

    const estado: string =
        (document.getElementById("state") as HTMLInputElement).value

    const cep: string =
        (document.getElementById("cep") as HTMLInputElement).value

    const descricao: string =
        (document.getElementById("description") as HTMLTextAreaElement).value

    const competencias: Competencia[] =
        capturarCompetenciasSelecionadas()

    return {
        nome,
        email,
        cpf,
        idade,
        formacao,
        estado,
        cep,
        descricao,
        competencias
    }
}

function capturarCompetenciasSelecionadas(): Competencia[] {
    const checkboxesSelecionados =
        document.querySelectorAll<HTMLInputElement>(
            '#skills-options input[type="checkbox"]:checked'
        )

    return Array.from(checkboxesSelecionados).map(
        checkbox => checkbox.value as Competencia
    )
}

function adicionarNaSessao(id: number, tipo: string): void {
    localStorage.setItem(
        "sessao",
        JSON.stringify({
            id: id,
            tipo: tipo
        })
    )
}