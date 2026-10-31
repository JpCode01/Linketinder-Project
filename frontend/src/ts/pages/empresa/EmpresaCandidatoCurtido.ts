import { CandidatoViewDTO } from "../../api/dto/candidato/CandidatoViewDTO"
import { VagaService } from "../../services/VagaService"
import { CandidatoService } from "../../services/CandidatoService"
import { CurtidaVagaService } from "../../services/CurtidaVagaService"
import { CurtidaCandidatoService } from "../../services/CurtidaCandidatoService"
import { obterIniciais } from "./EmpresaPerfil"
import { VagaEmpresaResponseDTO } from "../../api/dto/vaga/VagaEmpresaResponseDTO";

export async function exibirCandidatosCurtidos(
    idEmpresa: number,
    vagaService: VagaService,
    candidatoService: CandidatoService,
    curtidaVagaService: CurtidaVagaService,
    curtidaCandidatoService: CurtidaCandidatoService
): Promise<void> {
    const lista =
        document.getElementById("liked-candidate-grid")

    const contador =
        document.getElementById("liked-candidate-count")

    if (lista == null) {
        return
    }

    const vagas =
        await vagaService.buscarPorEmpresa(idEmpresa)

    lista.innerHTML = ""

    let quantidade = 0

    for (const vaga of vagas) {
        const candidatos =
            await curtidaVagaService.buscarCandidatosQueCurtiram(vaga.id)

        for (const candidatoAnonimo of candidatos) {
            const empresaCurtiuCandidato =
                await curtidaCandidatoService.jaCurtiuCandidato(
                    idEmpresa,
                    candidatoAnonimo.id
                )

            if (!empresaCurtiuCandidato) {
                continue
            }

            const candidato =
                await candidatoService.buscarPorId(candidatoAnonimo.id)

            lista.appendChild(
                criarCardCandidatoCurtido(candidato, vaga)
            )

            quantidade++
        }
    }

    if (contador != null) {
        contador.textContent = quantidade.toString()
    }
}

function criarCardCandidatoCurtido(
    candidato: CandidatoViewDTO,
    vaga: VagaEmpresaResponseDTO
): HTMLElement {
    const article = document.createElement("article")
    article.className = "candidate-card"

    article.appendChild(
        criarTopoCandidatoCurtido(candidato)
    )

    article.appendChild(
        criarSecaoFormacao(candidato)
    )

    article.appendChild(
        criarSecaoCompetencias(candidato)
    )

    article.appendChild(
        criarSecaoVaga(vaga)
    )

    return article
}

function criarTopoCandidatoCurtido(
    candidato: CandidatoViewDTO
): HTMLElement {
    const topo = document.createElement("div")
    topo.className = "candidate-top"

    const avatar = document.createElement("div")
    avatar.className = "candidate-avatar"
    avatar.textContent = obterIniciais(candidato.nome)

    const nome = document.createElement("h3")
    nome.textContent = candidato.nome

    topo.appendChild(avatar)
    topo.appendChild(nome)

    return topo
}

function criarSecaoFormacao(
    candidato: CandidatoViewDTO
): HTMLElement {
    const secao = document.createElement("div")
    secao.className = "candidate-section"

    const titulo = document.createElement("strong")
    titulo.textContent = "Formação"

    const formacao = document.createElement("p")

    secao.appendChild(titulo)
    secao.appendChild(formacao)

    return secao
}

function criarSecaoCompetencias(
    candidato: CandidatoViewDTO
): HTMLElement {
    const secao = document.createElement("div")
    secao.className = "candidate-section"

    const titulo = document.createElement("strong")
    titulo.textContent = "Competências"

    const tags = document.createElement("div")
    tags.className = "job-tags"

    candidato.competencias.forEach((competencia) => {
        const tag = document.createElement("span")
        tag.textContent = competencia.nome
        tags.appendChild(tag)
    })

    secao.appendChild(titulo)
    secao.appendChild(tags)

    return secao
}

function criarSecaoVaga(
    vaga: VagaEmpresaResponseDTO
): HTMLElement {
    const secao = document.createElement("div")
    secao.className = "candidate-section"

    const titulo = document.createElement("strong")
    titulo.textContent = "Vaga de interesse"

    const nomeVaga = document.createElement("p")
    nomeVaga.textContent = vaga.nome

    secao.appendChild(titulo)
    secao.appendChild(nomeVaga)

    return secao
}