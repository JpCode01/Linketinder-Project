import { Candidato } from "../models/Candidato";
import { Empresa } from "../models/Empresa";
import { Vaga } from "../models/Vaga";
import { EmpresaService} from "../services/EmpresaService"
import { VagaService } from "../services/VagaService"

const empresaService: EmpresaService = new EmpresaService
const vagaService: VagaService = new VagaService

export function exibirPageEmpresa(): void {
        const empresa = empresaService.buscarEmpresaLogada()
        if (empresa != null) {
            exibirDadosEmpresa(empresa)
            exibirVagas(empresa)
            exibirCandidatos(empresa)
        }
}

function exibirDadosEmpresa(empresa: Empresa) {
     const apelidoAvatar = empresa.nome.split(" ")

            let iniciais = apelidoAvatar[0].charAt(0)

            if (apelidoAvatar.length > 1) {
                iniciais += apelidoAvatar[1].charAt(0)
            }

            document.getElementById("profile-avatar")!.innerHTML = iniciais
            document.getElementById("user-avatar")!.innerHTML =
            iniciais
            document.getElementById("profile-name")!.innerHTML =
            empresa.nome
            document.getElementById("user-name")!.innerHTML =
            empresa.nome
            document.getElementById("profile-description")!.innerHTML =
            empresa.descricao
}

function exibirVagas(empresa: Empresa) {
    const vagas: Vaga[] = empresa.getVagas 

    const listaVagas = document.getElementById("job-list")!

    for (let vaga of vagas) {
        const card = document.createElement("article")
        card.classList.add("company-job-card")

        const informacoes = document.createElement("div")

        const tipo = document.createElement("span")
        tipo.classList.add("job-type")
        tipo.innerHTML = vaga.tipo

        const nome = document.createElement("h3")
        nome.innerHTML = vaga.nome

        const localizacao = document.createElement("p")
        localizacao.innerHTML = vaga.localização

        informacoes.appendChild(tipo)
        informacoes.appendChild(nome)
        informacoes.appendChild(localizacao)

        const estatistica = document.createElement("div")
        estatistica.classList.add("job-stat")

        const candidatos = document.createElement("strong")
        candidatos.innerHTML = "?"

        const textoCandidatos = document.createElement("span")
        textoCandidatos.innerHTML = "candidatos"

        estatistica.appendChild(candidatos)
        estatistica.appendChild(textoCandidatos)


        card.appendChild(informacoes)
        card.appendChild(estatistica)

        listaVagas.appendChild(card)
    }
}


function exibirCandidatos(empresa: Empresa) {

    const vagas: Vaga[] = empresa.getVagas

    const candidatosLista = document.getElementById("candidate-grid")

    if (candidatosLista == null) {
        return
    }

    candidatosLista.innerHTML = ""

    let count: number = 0

    for (let vaga of vagas) {
        count += vaga.getCandidatosQueCurtiram.length
    }

    document.getElementById("candidate-count")!.innerHTML =
        count.toString() + " candidatos"

    for (const vaga of vagas) {

        const candidatos = vaga.getCandidatosQueCurtiram

        for (const candidato of candidatos) {
            const candidatoJaCurtido = empresa.getCandidatosCurtidos.some(
                candidatoCurtido => candidatoCurtido.cpf === candidato.cpf
            )

            const card = document.createElement("article")
            card.classList.add("candidate-card")

            const topo = document.createElement("div")
            topo.classList.add("candidate-top")

            const perfilAnonimo = document.createElement("span")
            perfilAnonimo.classList.add("anonymous")
            perfilAnonimo.innerHTML = "PERFIL ANÔNIMO"

            topo.appendChild(perfilAnonimo)

            const nome = document.createElement("h3")
            nome.innerHTML = candidato.nome

            const secaoFormacao = document.createElement("div")
            secaoFormacao.classList.add("candidate-section")

            const tituloFormacao = document.createElement("span")
            tituloFormacao.innerHTML = "FORMAÇÃO"

            const formacao = document.createElement("p")
            formacao.innerHTML = candidato.formacao

            secaoFormacao.appendChild(tituloFormacao)
            secaoFormacao.appendChild(formacao)

            const secaoCompetencias = document.createElement("div")
            secaoCompetencias.classList.add("candidate-section")

            const tituloCompetencias = document.createElement("span")
            tituloCompetencias.innerHTML = "COMPETÊNCIAS"

            const listaCompetencias = document.createElement("div")
            listaCompetencias.classList.add("skills")

            for (const competencia of candidato.getCompetencias) {

                const competenciaElemento = document.createElement("span")
                competenciaElemento.classList.add("skill")
                competenciaElemento.innerHTML = competencia

                listaCompetencias.appendChild(competenciaElemento)
            }

            secaoCompetencias.appendChild(tituloCompetencias)
            secaoCompetencias.appendChild(listaCompetencias)

            const secaoVaga = document.createElement("div")
            secaoVaga.classList.add("candidate-section")

            const tituloVaga = document.createElement("span")
            tituloVaga.innerHTML = "VAGA"

            const nomeVagaElemento = document.createElement("p")
            nomeVagaElemento.innerHTML = vaga.nome

            secaoVaga.appendChild(tituloVaga)
            secaoVaga.appendChild(nomeVagaElemento)

            const rodape = document.createElement("div")
            rodape.classList.add("candidate-footer")

            const botao = document.createElement("button")
            botao.classList.add("btn-like")
            botao.innerHTML = "♥"

            if (candidatoJaCurtido) {
                botao.textContent = "Curtido"
                botao.classList.add("btn-success")
            } else {
                botao.textContent = "Curtir"
                botao.classList.add("btn-primary")
            }

            botao.addEventListener("click", () => {
                curtirCandidato(empresa, candidato, botao)
            })

            rodape.appendChild(botao)

            card.appendChild(topo)
            card.appendChild(nome)
            card.appendChild(secaoFormacao)
            card.appendChild(secaoCompetencias)
            card.appendChild(secaoVaga)
            card.appendChild(rodape)

            candidatosLista.appendChild(card)
        }
    }
}



export function criarVaga(): void {
    document.getElementById("criar-vaga")!.onclick = (): void => {
        const empresa = empresaService.buscarEmpresaLogada()
        if (empresa != null) {
            const nome: string = (document.getElementById("job-title") as HTMLInputElement).value
            const tipo: string = (document.getElementById("job-type") as HTMLInputElement).value
            const localizacao: string = (document.getElementById("job-location") as HTMLInputElement).value
            const inputCompetencias = document.getElementById("skills") as HTMLInputElement
            const descricao: string = (document.getElementById("job-description") as HTMLInputElement).value
            
            
            const competencias =
            vagaService.verificarCompetenciasEConverter(inputCompetencias.value)

            if (competencias === null) {
                inputCompetencias.classList.add("input-error")

                    document.getElementById("erro-competencias")?.remove()

                    const mensagemErro = document.createElement("span")
                    mensagemErro.id = "erro-competencias"
                    mensagemErro.classList.add("error-message")
                    mensagemErro.textContent = "Uma ou mais competências são inválidas."

                    inputCompetencias.parentElement!.appendChild(mensagemErro)

                    return
            }



            const vaga: Vaga  = 
            new Vaga(
                nome,
                descricao,
                empresa.nome,
                tipo,
                localizacao
            )

            for (const competencia of competencias) {
                vagaService.adicionarCompetencia(vaga,  competencia)
            }

            vagaService.cadastrar(vaga)
            empresaService.adicionarVaga(empresa, vaga);

            (document.getElementById("job-title") as HTMLInputElement).value = "";
            (document.getElementById("job-location") as HTMLInputElement).value = "";
            (document.getElementById("skills") as HTMLInputElement).value = "";
            (document.getElementById("job-description") as HTMLInputElement).value = "";
        }
    }
}

function curtirCandidato(empresa: Empresa, candidato: Candidato, botao: HTMLButtonElement): void {
    if (empresa == null || candidato == null) {
        return
    }

    const curtido = empresaService.curtirCandidato(empresa, candidato)

    if (curtido) {
        botao.textContent = "Curtido"
        botao.classList.remove("btn-primary")
        botao.classList.add("btn-success")

    }

    // if (curtido) {
    //     candidato.addVaga(vaga)

    //     botao.textContent = "Curtido"
    //     botao.classList.remove("btn-primary")
    //     botao.classList.add("btn-success")
    // }
} 

