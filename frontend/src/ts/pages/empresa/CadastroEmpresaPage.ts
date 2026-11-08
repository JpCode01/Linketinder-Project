import { CadastrarEmpresaRequestDTO } from "../../api/dto/empresa/CadastrarEmpresaRequestDTO";
import { ServiceConfig } from "../../config/ServiceConfig";
import { Estado } from "../../models/Estado";
import { Pais } from "../../models/Pais";
import { LoginEmpresaStrategy } from "../../strategy/login/LoginEmpresaStrategy";

const serviceConfig: ServiceConfig = new ServiceConfig()

const empresaService = serviceConfig
    .criarEmpresaService()

const referenciaService = serviceConfig
    .criarReferenciaService()

export function cadastrarEmpresa(): void {
    const formulario = document.getElementById(
        "company-registration-form"
    ) as HTMLFormElement | null

    const botaoCadastro = document.getElementById(
        "register-company"
    ) as HTMLButtonElement | null

    if (formulario == null || botaoCadastro == null) {
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
            const dados: CadastrarEmpresaRequestDTO =
                capturarDadosEmpresa()

            await empresaService.cadastrar(dados)
            cadastroConcluido = true

            const login: LoginEmpresaStrategy =
                new LoginEmpresaStrategy()

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
    const paises: Pais[] =
        await referenciaService.buscarPaises()

    const estados: Estado[] =
        await referenciaService.buscarEstados()

    preencherPaises(paises)
    preencherEstados(estados)
}

function preencherPaises(paises: Pais[]): void {
    const campoPais = document.getElementById(
        "country"
    ) as HTMLSelectElement | null

    if (campoPais == null) {
        throw new Error("Campo de país não encontrado.")
    }

    campoPais.innerHTML = '<option value="">Selecione um país</option>'

    for (const pais of paises) {
        const opcao: HTMLOptionElement =
            document.createElement("option")

        opcao.value = pais.nome
        opcao.textContent = pais.nome

        campoPais.appendChild(opcao)
    }
}

function preencherEstados(estados: Estado[]): void {
    const campoEstado = document.getElementById(
        "state"
    ) as HTMLSelectElement | null

    if (campoEstado == null) {
        throw new Error("Campo de estado não encontrado.")
    }

    campoEstado.innerHTML = '<option value="">Selecione um estado</option>'

    for (const estado of estados) {
        const opcao: HTMLOptionElement =
            document.createElement("option")

        opcao.value = estado.sigla
        opcao.textContent = estado.sigla

        campoEstado.appendChild(opcao)
    }
}

function capturarDadosEmpresa(): CadastrarEmpresaRequestDTO {
    const nome: string = obterValor("name")
    const email: string = obterValor("email")
    const senha: string = obterValor("password")
    const cnpj: string = obterValor("cnpj")
    const pais: string = obterValor("country")
    const estado: string = obterValor("state")
    const cep: string = obterValor("cep")
    const descricao: string = obterValor("description")

    return {
        nome,
        email,
        senha,
        cnpj,
        pais,
        estado,
        cep,
        descricao
    }
}

function obterValor(id: string): string {
    const campo = document.getElementById(id) as
        | HTMLInputElement
        | HTMLSelectElement
        | HTMLTextAreaElement
        | null

    if (campo == null) {
        throw new Error(`Campo '${id}' não encontrado.`)
    }

    return campo.value.trim()
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
