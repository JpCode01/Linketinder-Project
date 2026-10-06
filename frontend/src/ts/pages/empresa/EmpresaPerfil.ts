import { Empresa } from "../../models/Empresa"

export function exibirDadosEmpresa(empresa: Empresa): void {
    const iniciais = obterIniciais(empresa.nome)

    document.getElementById("profile-avatar")!.textContent = iniciais

    document.getElementById("profile-name")!.textContent =
        empresa.nome

    document.getElementById("profile-description")!.textContent =
        empresa.descricao
}

export function obterIniciais(nome: string): string {
    const partesNome = nome.trim().split(" ")

    if (partesNome.length === 1) {
        return partesNome[0].charAt(0).toUpperCase()
    }

    return (
        partesNome[0].charAt(0) +
        partesNome[partesNome.length - 1].charAt(0)
    ).toUpperCase()
}