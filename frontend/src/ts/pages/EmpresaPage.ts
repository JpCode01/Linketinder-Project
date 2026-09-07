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

    console.log("VAGAS:", vagas)
    console.log("QUANTIDADE:", vagas.length)

    for (let vaga of vagas) {
        console.log("VAGA:", vaga)
        console.log("NOME:", vaga.nome)
        console.log("TIPO:", vaga.tipo)
        console.log("LOCALIZAÇÃO:", vaga.localização)
    }

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