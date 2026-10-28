import { Competencia } from "../../models/Competencia"

import { RepositoryConfig } from "../../config/RepositoryConfig"
import { ServiceConfig } from "../../config/ServiceConfig"

import { CompetenciaApiClient } from "../../api/clientes/CompetenciaApiClient";
import { ReferenciaApiClient } from "../../api/clientes/ReferenciaApiClient";
import { CadastrarCandidatoRequestDTO } from "../../api/dto/candidato/CadastrarCandidatoRequestDTO";
import { Estado } from "../../models/Estado";
import { Pais } from "../../models/Pais";
import { LoginCandidatoStrategy } from "../../strategy/login/LoginCandidatoStrategy";

const repositoryConfig: RepositoryConfig = new RepositoryConfig()
const serviceConfig: ServiceConfig = new ServiceConfig(repositoryConfig)

const candidatoService = serviceConfig.criarCandidatoService()

const competenciaApiClient: CompetenciaApiClient =
    new CompetenciaApiClient()

const referenciaApiClient: ReferenciaApiClient =
    new ReferenciaApiClient()

export function cadastrarCandidato(): void {
    const formulario = document.getElementById(
        "candidate-registration-form"
    ) as HTMLFormElement | null

    const botaoCadastro = document.getElementById(
        "register-candidate"
    ) as HTMLButtonElement | null

    const campoDataNascimento = document.getElementById(
        "birth-date"
    ) as HTMLInputElement | null

    if (
        formulario == null ||
        botaoCadastro == null ||
        campoDataNascimento == null
    ) {
        console.error("Elementos do formulário de cadastro não encontrados.")
        return
    }

    botaoCadastro.disabled = true

    void carregarOpcoesFormulario()
        .then((): void => {
            botaoCadastro.disabled = false
        })
        .catch((erro: unknown): void => {
            mostrarErro(
                `Não foi possível carregar as opções do cadastro. ${obterMensagemErro(erro)}`
            )
        })

    botaoCadastro.onclick = async (): Promise<void> => {
        if (!formulario.reportValidity()) {
            return
        }

        limparErro()
        botaoCadastro.disabled = true

        let cadastroConcluido = false

        try {
            const dados: CadastrarCandidatoRequestDTO =
                capturarDadosCandidato()

            await candidatoService.cadastrar(dados)
            cadastroConcluido = true

            const login: LoginCandidatoStrategy =
                new LoginCandidatoStrategy()

            await login.login(dados.email, dados.senha)
        } catch (erro: unknown) {
            const mensagem: string = obterMensagemErro(erro)

            if (cadastroConcluido) {
                mostrarErro(
                    "Cadastro realizado, mas não foi possível entrar automaticamente. " +
                    "Acesse a tela de login. " + mensagem
                )
            } else {
                mostrarErro(mensagem)
            }
        } finally {
            botaoCadastro.disabled = false
        }
    }
}

async function carregarOpcoesFormulario(): Promise<void> {
    const competencias: Competencia[] =
        await competenciaApiClient.buscarTodas()

    const paises: Pais[] =
        await referenciaApiClient.buscarPaises()

    const estados: Estado[] =
        await referenciaApiClient.buscarEstados()

    preencherCompetencias(competencias)
    preencherPaises(paises)
    preencherEstados(estados)
}

function preencherCompetencias(competencias: Competencia[]): void {
    const containerCompetencias = document.getElementById("skills-options")

    if (containerCompetencias == null) {
        throw new Error("Container de competências não encontrado.")
    }

    containerCompetencias.innerHTML = ""

    for (const competencia of competencias) {
        const opcaoCompetencia = document.createElement("div")
        opcaoCompetencia.classList.add("skill-option")

        const checkbox = document.createElement("input")
        checkbox.type = "checkbox"
        checkbox.name = "competencias"
        checkbox.value = competencia.nome
        checkbox.id = `competencia-${competencia.id}`

        const label = document.createElement("label")
        label.htmlFor = checkbox.id
        label.textContent = competencia.nome

        opcaoCompetencia.appendChild(checkbox)
        opcaoCompetencia.appendChild(label)

        containerCompetencias.appendChild(opcaoCompetencia)
    }
}

function preencherPaises(paises: Pais[]): void {
    const selectPais = document.getElementById(
        "country"
    ) as HTMLSelectElement | null

    if (selectPais == null) {
        throw new Error("Campo de país não encontrado.")
    }

    selectPais.innerHTML = '<option value="">Selecione um país</option>'

    for (const pais of paises) {
        const opcao = document.createElement("option")
        opcao.value = pais.nome
        opcao.textContent = pais.nome

        selectPais.appendChild(opcao)
    }
}

function preencherEstados(estados: Estado[]): void {
    const selectEstado = document.getElementById(
        "state"
    ) as HTMLSelectElement | null

    if (selectEstado == null) {
        throw new Error("Campo de estado não encontrado.")
    }

    selectEstado.innerHTML = '<option value="">Selecione um estado</option>'

    for (const estado of estados) {
        const opcao = document.createElement("option")
        opcao.value = estado.sigla
        opcao.textContent = estado.sigla

        selectEstado.appendChild(opcao)
    }
}

function calcularIdade(dataNascimento: string): number {
    const [ano, mes, dia] = dataNascimento.split("-").map(Number)

    const hoje = new Date()
    const dataAtual = new Date(
        hoje.getFullYear(),
        hoje.getMonth(),
        hoje.getDate()
    )

    const nascimento = new Date(ano, mes - 1, dia)

    if (
        Number.isNaN(nascimento.getTime()) ||
        nascimento.getFullYear() !== ano ||
        nascimento.getMonth() !== mes - 1 ||
        nascimento.getDate() !== dia
    ) {
        throw new Error("Informe uma data de nascimento válida.")
    }

    if (nascimento > dataAtual) {
        throw new Error("A data de nascimento não pode ser futura.")
    }

    let idade = dataAtual.getFullYear() - nascimento.getFullYear()

    const aindaNaoFezAniversario =
        dataAtual.getMonth() < nascimento.getMonth() ||
        (
            dataAtual.getMonth() === nascimento.getMonth() &&
            dataAtual.getDate() < nascimento.getDate()
        )

    if (aindaNaoFezAniversario) {
        idade--
    }

    if (idade <= 0 || idade > 120) {
        throw new Error("A idade informada deve estar entre 1 e 120 anos.")
    }

    return idade
}

function capturarDadosCandidato(): CadastrarCandidatoRequestDTO {
    const nome: string = obterValor("name")
    const sobrenome: string = obterValor("surname")
    const email: string = obterValor("email")
    const senha: string = obterValor("password")
    const cpf: string = obterValor("cpf")
    const dataNascimento: string = obterValor("birth-date")
    const pais: string = obterValor("country")
    const estado: string = obterValor("state")
    const cep: string = obterValor("cep")
    const descricao: string = obterValor("description")

    const idade: number = calcularIdade(dataNascimento)

    const campoIdade = document.getElementById(
        "age"
    ) as HTMLInputElement | null

    if (campoIdade != null) {
        campoIdade.value = String(idade)
    }

    const competencias: string[] = capturarCompetenciasSelecionadas()

    if (competencias.length === 0) {
        throw new Error("Selecione pelo menos uma competência.")
    }

    return {
        nome,
        sobrenome,
        email,
        senha,
        cpf,
        dataNascimento,
        pais,
        idade,
        estado,
        cep,
        descricao,
        competencias
    }
}

function obterValor(id: string): string {
    const elemento = document.getElementById(id) as
        | HTMLInputElement
        | HTMLSelectElement
        | HTMLTextAreaElement
        | null

    if (elemento == null) {
        throw new Error(`Campo '${id}' não encontrado.`)
    }

    return elemento.value.trim()
}

function capturarCompetenciasSelecionadas(): string[] {
    const checkboxesSelecionados =
        document.querySelectorAll<HTMLInputElement>(
            '#skills-options input[name="competencias"]:checked'
        )

    return Array.from(checkboxesSelecionados).map(
        (checkbox: HTMLInputElement): string => checkbox.value
    )
}

function mostrarErro(mensagem: string): void {
    const elementoErro = document.getElementById("registration-error")

    if (elementoErro == null) {
        console.error(mensagem)
        return
    }

    elementoErro.textContent = mensagem
    elementoErro.hidden = false
}

function limparErro(): void {
    const elementoErro = document.getElementById("registration-error")

    if (elementoErro != null) {
        elementoErro.textContent = ""
        elementoErro.hidden = true
    }
}

function obterMensagemErro(erro: unknown): string {
    if (erro instanceof TypeError) {
        return "Não foi possível conectar ao servidor. Verifique se o backend está em execução."
    }

    if (erro instanceof Error) {
        return erro.message
    }

    return "Ocorreu um erro inesperado ao cadastrar."
}