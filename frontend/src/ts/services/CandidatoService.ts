import { Candidato } from "../models/Candidato"
import { Competencia } from "../models/Competencia";
import { Vaga, IVagaJSON } from "../models/Vaga"
import { ICandidatoJSON } from "../models/Candidato"

export class CandidatoService {
    cadastrar(candidato: Candidato): void {
        const candidatosSalvos = localStorage.getItem("candidatos")
        

        // Pega os candidatos do LocalStorage e cadastra novamente

        const candidatos: Candidato[] =
            candidatosSalvos ? JSON.parse(candidatosSalvos)
            : []

        candidatos.push(candidato)

        localStorage.setItem(
            "candidatos",
            JSON.stringify(candidatos)
        )

    }

    curtirVaga(candidato: Candidato, vaga: Vaga): boolean {
        if (vaga == null || candidato == null) {
            throw "Candidato ou Vaga não podem ser nulos"
        }

        const candidatosSalvos = localStorage.getItem("candidatos")

        if (candidatosSalvos == null) {
            throw "Nenhum candidato cadastrado"
        }

        const candidatosJson: ICandidatoJSON[] = JSON.parse(candidatosSalvos)

        const candidatoEncontrado = candidatosJson.find(
            candidatoJson => candidatoJson._cpf === candidato.cpf
        )

        if (candidatoEncontrado == null) {
            throw "Candidato não encontrado"
        }

        const vagaJaCurtida = candidatoEncontrado.vagasCurtidas.some(
        vagaCurtida => vagaCurtida._nome === vaga.nome)

        if (vagaJaCurtida) {
            return false
        }

        const vagaJson: IVagaJSON = {
            _nome: vaga.nome,
            _descricao: vaga.descricao,
            _empresa: vaga.empresa,
            _tipo: vaga.tipo,
            _localizacao: vaga.localização
        }

        

        candidatoEncontrado.vagasCurtidas.push(vagaJson)

        localStorage.setItem(
            "candidatos",
            JSON.stringify(candidatosJson)
        )

        localStorage.setItem(
            "candidatoLogado",
            JSON.stringify(candidatoEncontrado)
        )
        return true
    } 

    adicionarCompetencia(candidato: Candidato, competencia: Competencia): void {
         if (competencia != null && candidato != null) {
            candidato.addCompetencia(competencia)
        } else {
            throw "Candidato ou competência não podem ser nulos"
        }
    }

    buscarCandidato(nomeProcurado: string): Candidato | undefined {


        const candidatosSalvos = localStorage.getItem("candidatos")

        if (candidatosSalvos == null) {
            return undefined
        }

        const candidatosJson: ICandidatoJSON[] = JSON.parse(candidatosSalvos)

        const candidatoEncontrado = candidatosJson.find(
        candidato => candidato._nome === nomeProcurado
        )

        if (candidatoEncontrado == null) {
            return undefined
        }

        const candidatoConvertido: Candidato = new Candidato(
            candidatoEncontrado._cpf,
            candidatoEncontrado._idade,
            candidatoEncontrado._formacao,
            candidatoEncontrado._nome,
            candidatoEncontrado._email,
            candidatoEncontrado._estado,
            candidatoEncontrado._cep,
            candidatoEncontrado._descricao
        )

        const vagasConvertidas: Vaga[] = 
        candidatoEncontrado.vagasCurtidas.map(
            vagaJson => new Vaga(
                vagaJson._nome,
                vagaJson._descricao,
                vagaJson._empresa,
                vagaJson._tipo,
                vagaJson._localizacao
            )
        )

        candidatoConvertido.setVagasCurtidas(vagasConvertidas)

        return candidatoConvertido
    }

    buscarCandidatoLogado(): Candidato | undefined {
        const candidatoSalvo = localStorage.getItem("candidatoLogado")

        if (candidatoSalvo == null) {
            return undefined
        }

        const candidatoJson: ICandidatoJSON =
            JSON.parse(candidatoSalvo)

        const candidatoConvertido: Candidato = new Candidato(
            candidatoJson._cpf,
            candidatoJson._idade,
            candidatoJson._formacao,
            candidatoJson._nome,
            candidatoJson._email,
            candidatoJson._estado,
            candidatoJson._cep,
            candidatoJson._descricao
        )

        const vagasConvertidas: Vaga[] = 
        candidatoJson.vagasCurtidas.map(
            vagaJSON => new Vaga(
                vagaJSON._nome,
                vagaJSON._descricao,
                vagaJSON._empresa,
                vagaJSON._tipo,
                vagaJSON._localizacao
            ) 
        )

        candidatoConvertido.setVagasCurtidas(vagasConvertidas)

        console.log("Candidato logado salvo:", candidatoJson)
        console.log("Vagas curtidas:", candidatoJson.vagasCurtidas)

        return candidatoConvertido

        
    }

    verificarCompetenciasEConverter(
    competenciasRecebidas: string): Competencia[] | null {

        const competencias = competenciasRecebidas
            .split(",")
            .map(competencia => competencia.trim().toUpperCase())

        const competenciasConvertidas: Competencia[] = []

        for (const competencia of competencias) {

            if (!(competencia in Competencia)) {
                return null
            }

            const competenciaConvertida =
                Competencia[competencia as keyof typeof Competencia]

            competenciasConvertidas.push(competenciaConvertida)
        }

        return competenciasConvertidas
    }
}