import { Candidato } from "../../models/Candidato"
import { Vaga } from "../../models/Vaga"
import { VagaService } from "../../services/VagaService"
import { CandidatoService } from "../../services/CandidatoService"
import { CurtidaVagaService } from "../../services/CurtidaVagaService"
import { CurtidaCandidatoService } from "../../services/CurtidaCandidatoService"
import { obterIniciais } from "./EmpresaPerfil"

export function exibirCandidatosCurtidos(
    idEmpresa: number,
    vagaService: VagaService,
    candidatoService: CandidatoService,
    curtidaVagaService: CurtidaVagaService,
    curtidaCandidatoService: CurtidaCandidatoService
): void {
    const lista =
        document.getElementById("liked-candidate-grid")

    const contador =
        document.getElementById("liked-candidate-count")

    if (lista == null) {
        return
    }

    const curtidasCandidatos =
        curtidaCandidatoService.buscarPorEmpresa(idEmpresa)

    const vagas =
        vagaService.buscarPorEmpresa(idEmpresa)

    lista.innerHTML = ""

    let quantidade = 0

    curtidasCandidatos.forEach((curtidaCandidato) => {
        const candidato =
            candidatoService.buscarPorId(
                curtidaCandidato.idCandidato
            )

        vagas.forEach((vaga) => {
            const curtidas =
                curtidaVagaService.buscarPorVaga(vaga.id)

            const candidatoCurtiuVaga =
                curtidas.some(
                    (curtida) =>
                        curtida.idCandidato === candidato.id
                )

            const empresaCurtiuCandidatoParaVaga =
                curtidaCandidato.idVaga === vaga.id

            if (
                candidatoCurtiuVaga &&
                empresaCurtiuCandidatoParaVaga
            ) {
                lista.appendChild(
                    criarCardCandidatoCurtido(
                        candidato,
                        vaga
                    )
                )

                quantidade++
            }
        })
    })

    if (contador != null) {
        contador.textContent =
            quantidade.toString()
    }
}

function criarCardCandidatoCurtido(
    candidato: Candidato,
    vaga: Vaga
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
    candidato: Candidato
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
    candidato: Candidato
): HTMLElement {
    const secao = document.createElement("div")
    secao.className = "candidate-section"

    const titulo = document.createElement("strong")
    titulo.textContent = "Formação"

    const formacao = document.createElement("p")
    formacao.textContent = candidato.formacao

    secao.appendChild(titulo)
    secao.appendChild(formacao)

    return secao
}

function criarSecaoCompetencias(
    candidato: Candidato
): HTMLElement {
    const secao = document.createElement("div")
    secao.className = "candidate-section"

    const titulo = document.createElement("strong")
    titulo.textContent = "Competências"

    const tags = document.createElement("div")
    tags.className = "job-tags"

    candidato.competencias.forEach((competencia) => {
        const tag = document.createElement("span")
        tag.textContent = competencia

        tags.appendChild(tag)
    })

    secao.appendChild(titulo)
    secao.appendChild(tags)

    return secao
}

function criarSecaoVaga(
    vaga: Vaga
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