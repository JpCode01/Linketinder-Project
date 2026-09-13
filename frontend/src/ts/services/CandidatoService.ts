import { Candidato } from "../models/Candidato"
import { Competencia } from "../models/Competencia";
import { Vaga, IVagaJSON } from "../models/Vaga"
import { ICandidatoCurtidaJSON, ICandidatoJSON } from "../models/Candidato"
import { Empresa, IEmpresaJSON } from "../models/Empresa"

export class CandidatoService {
    cadastrar(candidato: Candidato): void {
        const candidatosSalvos = localStorage.getItem("candidatos")

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

        const candidatosJson: ICandidatoJSON[] =
            JSON.parse(candidatosSalvos)

        const candidatoEncontrado = candidatosJson.find(
            candidatoJson => candidatoJson._cpf === candidato.cpf
        )

        if (candidatoEncontrado == null) {
            throw "Candidato não encontrado"
        }

        const vagaJaCurtida = candidatoEncontrado.vagasCurtidas.some(
            vagaCurtida =>
                vagaCurtida._nome === vaga.nome &&
                vagaCurtida._empresa === vaga.empresa
        )

        if (vagaJaCurtida) {
            return false
        }

        const candidatoCurtida: ICandidatoCurtidaJSON = {
            _cpf: candidato.cpf,
            _nome: candidato.nome,
            competencias: candidato.getCompetencias,
            _formacao: candidato.formacao,
            _descricao: candidato.descricao
        }

        const vagaJson: IVagaJSON = {
            _nome: vaga.nome,
            _descricao: vaga.descricao,
            _empresa: vaga.empresa,
            _tipo: vaga.tipo,
            _localizacao: vaga.localização,
            competencias: vaga.getCompetencias,
            candidatosQueCurtiram: [candidatoCurtida]
        }

        candidatoEncontrado.vagasCurtidas.push(vagaJson)

        localStorage.setItem(
            "candidatos",
            JSON.stringify(candidatosJson)
        )

        const vagasSalvas = localStorage.getItem("vagas")

        if (vagasSalvas == null) {
            throw "Nenhuma vaga cadastrada"
        }

        const vagasJson: IVagaJSON[] =
            JSON.parse(vagasSalvas)

        const vagaEncontrada = vagasJson.find(
            vagaJson =>
                vagaJson._nome === vaga.nome &&
                vagaJson._empresa === vaga.empresa
        )

        if (vagaEncontrada == null) {
            throw "Vaga não encontrada"
        }

        const candidatoJaCurtiu = vagaEncontrada.candidatosQueCurtiram.some(
            candidatoJson =>
                candidatoJson._cpf === candidato.cpf
        )

        if (!candidatoJaCurtiu) {
            vagaEncontrada.candidatosQueCurtiram.push(candidatoCurtida)
        }

        const empresasSalvas = localStorage.getItem("empresas")

        if (empresasSalvas == null) {
            throw "Nenhuma empresa cadastrada"
        }

        const empresaJson: IEmpresaJSON[] =
            JSON.parse(empresasSalvas)

        const empresaEncontrada = empresaJson.find(
            empresaJson =>
                empresaJson._nome === vaga.empresa
        )

        if (empresaEncontrada == null) {
            throw "Empresa da vaga não encontrada"
        }

        const vagaDaEmpresa = empresaEncontrada.vagas.find(
            vagaJson =>
                vagaJson._nome === vaga.nome &&
                vagaJson._empresa === vaga.empresa
        )

        if (vagaDaEmpresa == null) {
            throw "Vaga não encontrada na empresa"
        }

        const candidatoJaCurtiuNaEmpresa =
            vagaDaEmpresa.candidatosQueCurtiram.some(
                candidatoJson =>
                    candidatoJson._cpf === candidato.cpf
            )

        if (!candidatoJaCurtiuNaEmpresa) {
            vagaDaEmpresa.candidatosQueCurtiram.push(
                candidatoCurtida
            )
        }

        localStorage.setItem(
            "empresas",
            JSON.stringify(empresaJson)
        )

        localStorage.setItem(
            "empresaLogada",
            JSON.stringify(empresaEncontrada)
        )

        localStorage.setItem(
            "vagas",
            JSON.stringify(vagasJson)
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

        candidatoConvertido.setCompetencias(candidatoEncontrado.competencias)

        const vagasConvertidas: Vaga[] = 
            candidatoEncontrado.vagasCurtidas.map(
                vagaJson => {
                    const vaga = new Vaga(
                        vagaJson._nome,
                        vagaJson._descricao,
                        vagaJson._empresa,
                        vagaJson._tipo,
                        vagaJson._localizacao
                    )

                    vaga.setCompetencias(vagaJson.competencias)

                    return vaga
                }
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

        candidatoConvertido.setCompetencias(candidatoJson.competencias)

        const vagasConvertidas: Vaga[] = 
        candidatoJson.vagasCurtidas.map(
            vagaJSON => {
                const vaga = new Vaga(
                    vagaJSON._nome,
                    vagaJSON._descricao,
                    vagaJSON._empresa,
                    vagaJSON._tipo,
                    vagaJSON._localizacao
                )

                vaga.setCompetencias(vagaJSON.competencias)

                return vaga
            }
        )

        candidatoConvertido.setVagasCurtidas(vagasConvertidas)

        console.log("Candidato logado salvo:", candidatoJson)
        console.log("Vagas curtidas:", candidatoJson.vagasCurtidas)

        return candidatoConvertido

        
    }

    verificarCompetenciasEConverter(competenciasRecebidas: string): Competencia[] | null {

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