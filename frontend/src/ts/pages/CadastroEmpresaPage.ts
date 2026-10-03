import { Empresa } from "../models/Empresa"

import { EmpresaService } from "../services/EmpresaService"

import { RepositoryConfig } from "../config/RepositoryConfig"
import { ServiceConfig } from "../config/ServiceConfig"

import { EmailJaCadastradoException } from "../exceptions/EmailJaCadastradoException"
import { CnpjJaCadastradoException } from "../exceptions/CnpjJaCadastradoException"


interface DadosEmpresaFormulario {
    nome: string
    email: string
    cnpj: string
    pais: string
    estado: string
    cep: string
    descricao: string
}


const repositoryConfig: RepositoryConfig = new RepositoryConfig()
const serviceConfig: ServiceConfig = new ServiceConfig(repositoryConfig)

const empresaService: EmpresaService =
    serviceConfig.criarEmpresaService()


export function cadastrarEmpresa(): void {
    document.getElementById("register-company")!.onclick = (): void => {
        limparErros()

        const dados: DadosEmpresaFormulario =
            capturarDadosEmpresa()

        const empresa: Empresa =
            construirEmpresa(dados)

        try {
            empresaService.salvar(empresa)

            adicionarNaSessao(empresa.id, "EMPRESA")

            window.location.href = "./empresa.html"

        } catch (erro) {

            if (erro instanceof EmailJaCadastradoException) {
                mostrarErroEmail("E-mail já cadastrado.")
                return
            }

            if (erro instanceof CnpjJaCadastradoException) {
                mostrarErroCnpj("CNPJ já cadastrado.")
                return
            }

            throw erro
        }
    }
}


function capturarDadosEmpresa(): DadosEmpresaFormulario {
    const nome: string =
        (document.getElementById("name") as HTMLInputElement).value

    const email: string =
        (document.getElementById("email") as HTMLInputElement).value

    const cnpj: string =
        (document.getElementById("cnpj") as HTMLInputElement).value

    const pais: string =
        (document.getElementById("country") as HTMLInputElement).value

    const estado: string =
        (document.getElementById("state") as HTMLInputElement).value

    const cep: string =
        (document.getElementById("cep") as HTMLInputElement).value

    const descricao: string =
        (document.getElementById("description") as HTMLTextAreaElement).value

    return {
        nome,
        email,
        cnpj,
        pais,
        estado,
        cep,
        descricao
    }
}


function construirEmpresa(dados: DadosEmpresaFormulario): Empresa {
    return new Empresa(
        dados.cnpj,
        dados.pais,
        dados.nome,
        dados.email,
        dados.estado,
        dados.cep,
        dados.descricao
    )
}


function mostrarErroEmail(mensagem: string): void {
    const inputEmail =
        document.getElementById("email") as HTMLInputElement

    inputEmail.classList.add("input-error")

    const mensagemErro: HTMLSpanElement =
        document.createElement("span")

    mensagemErro.id = "erro-email"
    mensagemErro.classList.add("error-message")
    mensagemErro.textContent = mensagem

    inputEmail.parentElement!.appendChild(mensagemErro)
}


function mostrarErroCnpj(mensagem: string): void {
    const inputCnpj =
        document.getElementById("cnpj") as HTMLInputElement

    inputCnpj.classList.add("input-error")

    const mensagemErro: HTMLSpanElement =
        document.createElement("span")

    mensagemErro.id = "erro-cnpj"
    mensagemErro.classList.add("error-message")
    mensagemErro.textContent = mensagem

    inputCnpj.parentElement!.appendChild(mensagemErro)
}


function limparErros(): void {
    document
        .getElementById("email")
        ?.classList.remove("input-error")

    document
        .getElementById("cnpj")
        ?.classList.remove("input-error")

    document.getElementById("erro-email")?.remove()
    document.getElementById("erro-cnpj")?.remove()
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