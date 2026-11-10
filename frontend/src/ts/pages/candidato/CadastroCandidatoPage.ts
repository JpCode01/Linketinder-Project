import { SeletorCompetencias } from "../../components/SeletorCompetencias"
import { CadastrarCandidatoRequestDTO } from "../../api/dto/candidato/CadastrarCandidatoRequestDTO";
import { ServiceConfig } from "../../config/ServiceConfig";
import { Estado } from "../../models/Estado";
import { Pais } from "../../models/Pais";

const serviceConfig: ServiceConfig = new ServiceConfig()

const candidatoService = serviceConfig
    .criarCandidatoService()

const competenciaService = serviceConfig
    .criarCompetenciaService()

const referenciaService = serviceConfig
    .criarReferenciaService()

let seletorCompetencias: SeletorCompetencias | undefined

export function cadastrarCandidato(): void {
    seletorCompetencias = undefined
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

    formulario.onsubmit = (evento: SubmitEvent): void => {
        evento.preventDefault()
        if (!botaoCadastro.disabled) botaoCadastro.click()
    }

    botaoCadastro.onclick = async (): Promise<void> => {
        if (botaoCadastro.disabled) return
        if (!formulario.reportValidity()) {
            return
        }

        limparErro()
        botaoCadastro.disabled = true

        try {
            const dados: CadastrarCandidatoRequestDTO =
                capturarDadosCandidato()

            await candidatoService.cadastrar(dados)
            window.location.href = "./index.html"
        } catch (erro: unknown) {
            mostrarErro(obterMensagemErro(erro))
        } finally {
            botaoCadastro.disabled = false
        }
    }
}

async function carregarOpcoesFormulario(): Promise<void> {
    const competencias: string[] =
        await competenciaService.buscarTodas()

    const paises: Pais[] =
        await referenciaService.buscarPaises()

    const estados: Estado[] =
        await referenciaService.buscarEstados()

    preencherCompetencias(competencias)
    preencherPaises(paises)
    preencherEstados(estados)
}

function preencherCompetencias(competencias: string[]): void {
    const containerCompetencias = document.getElementById("skills-options")

    if (containerCompetencias == null) {
        throw new Error("Container de competências não encontrado.")
    }

    seletorCompetencias = new SeletorCompetencias(containerCompetencias, competencias)
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

    return id === "password" ? elemento.value : elemento.value.trim()
}

function capturarCompetenciasSelecionadas(): string[] {
    if (seletorCompetencias == undefined) {
        throw new Error("Competências ainda não carregadas.")
    }
    return seletorCompetencias.obterSelecionadas()
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
