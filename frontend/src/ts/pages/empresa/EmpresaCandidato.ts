import { Candidato } from "../../models/Candidato"
import { Vaga } from "../../models/Vaga"
import { VagaService } from "../../services/VagaService"
import { CandidatoService } from "../../services/CandidatoService"
import { CurtidaVagaService } from "../../services/CurtidaVagaService"
import { CurtidaCandidatoService } from "../../services/CurtidaCandidatoService"
import { CandidatoJaCurtidoException } from "../../exceptions/candidato/CandidatoJaCurtidoException"
import { VagaEmpresaResponseDTO } from "../../api/dto/vaga/VagaEmpresaResponseDTO";
import { CandidatoAnonimoResponseDTO } from "../../api/dto/candidato/CandidatoAnonimoResponseDTO";

export async function exibirCandidatosEmpresa(
    idEmpresa: number,
    vagaService: VagaService,
    candidatoService: CandidatoService,
    curtidaVagaService: CurtidaVagaService,
    curtidaCandidatoService: CurtidaCandidatoService
): Promise<void> {
    const grid = document.getElementById("candidate-grid")

    if (grid == null) {
        return
    }

    const vagas = await vagaService.buscarPorEmpresa(idEmpresa)
    const curtidasCandidatos = curtidaCandidatoService.buscarPorEmpresa(idEmpresa)

    grid.innerHTML = ""
    let quantidadeCandidatos = 0

    for (const vaga of vagas) {
        const candidatos = await curtidaVagaService.buscarCandidatosQueCurtiram(vaga.id)

        for (const candidato of candidatos) {
            const candidatoJaCurtido = curtidasCandidatos.some(
                (curtidaCandidato) =>
                    curtidaCandidato.idCandidato === candidato.id &&
                    curtidaCandidato.idVaga === vaga.id
            )

            const card = criarCardCandidato(
                candidato,
                vaga,
                candidatoJaCurtido,
                () => curtirCandidato(
                    idEmpresa,
                    candidato.id,
                    vaga.id,
                    card,
                    curtidaCandidatoService
                )
            )

            grid.appendChild(card)
            quantidadeCandidatos++
        }
    }

    const contador = document.getElementById("candidate-count")

    if (contador != null) {
        contador.textContent = quantidadeCandidatos.toString()
    }
}


function criarCardCandidato(
    candidato: CandidatoAnonimoResponseDTO,
    vaga: VagaEmpresaResponseDTO,
    candidatoJaCurtido: boolean,
    aoCurtir: () => void
): HTMLElement {
    const article = document.createElement("article")
    article.className = "candidate-card"

    article.appendChild(criarTopoCandidato(candidato))
    article.appendChild(criarSecaoVaga(vaga))
    article.appendChild(criarRodapeCandidato(candidatoJaCurtido, aoCurtir))

    return article
}

function criarTopoCandidato(
    candidato: CandidatoAnonimoResponseDTO
): HTMLElement {
    const topo = document.createElement("div")
    topo.className = "candidate-top"

    const avatar = document.createElement("div")
    avatar.className = "candidate-avatar"
    avatar.textContent = "?"

    const descricao = document.createElement("h3")
    descricao.textContent = candidato.descricao || "Candidato"

    topo.appendChild(avatar)
    topo.appendChild(descricao)

    return topo
}

// function criarSecaoFormacao(
//     candidato: Candidato
// ): HTMLElement {
//     const secao = document.createElement("div")
//     secao.className = "candidate-section"

//     const titulo = document.createElement("strong")
//     titulo.textContent = "Formação"

//     const formacao = document.createElement("p")
//     formacao.textContent = candidato.formacao

//     secao.appendChild(titulo)
//     secao.appendChild(formacao)

//     return secao
// }

// function criarSecaoCompetencias(
//     candidato: Candidato
// ): HTMLElement {
//     const secao = document.createElement("div")
//     secao.className = "candidate-section"

//     const titulo = document.createElement("strong")
//     titulo.textContent = "Competências"

//     const tags = document.createElement("div")
//     tags.className = "job-tags"

//     candidato.competencias.forEach((competencia) => {
//         const tag = document.createElement("span")
//         tag.textContent = competencia.nome

//         tags.appendChild(tag)
//     })

//     secao.appendChild(titulo)
//     secao.appendChild(tags)

//     return secao
// }

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

function criarRodapeCandidato(
    candidatoJaCurtido: boolean,
    aoCurtir: () => void
): HTMLElement {
    const rodape = document.createElement("div")
    rodape.className = "candidate-footer"

    const botao = document.createElement("button")
    botao.className = "btn-like"

    if (candidatoJaCurtido) {
        botao.textContent = "♥"
        botao.title = "Candidato curtido"
        botao.disabled = true
    } else {
        botao.textContent = "♡"
        botao.title = "Curtir candidato"
        botao.addEventListener("click", aoCurtir)
    }

    rodape.appendChild(botao)

    return rodape
}

async function curtirCandidato(
    idEmpresa: number,
    idCandidato: number,
    idVaga: number,
    card: HTMLElement,
    curtidaCandidatoService: CurtidaCandidatoService
): Promise<void> {
    const botao = card.querySelector(".btn-like") as HTMLButtonElement | null

    if (botao == null || botao.disabled) {
        return
    }

    botao.disabled = true

    try {
        await curtidaCandidatoService.curtir(
            idEmpresa,
            idCandidato,
            idVaga
        )

        atualizarCardCandidatoCurtido(card)
    } catch (erro: unknown) {
        botao.disabled = false
        console.error("Erro ao curtir candidato:", erro)
        alert(
            erro instanceof Error
                ? erro.message
                : "Não foi possível registrar a curtida."
        )
    }
}

function atualizarCardCandidatoCurtido(
    card: HTMLElement
): void {
    const botao =
        card.querySelector(".btn-like") as HTMLButtonElement | null

    if (botao == null) {
        return
    }

    botao.textContent = "♥"
    botao.title = "Candidato curtido"
    botao.disabled = true
}