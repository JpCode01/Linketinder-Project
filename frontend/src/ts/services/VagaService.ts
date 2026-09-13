import { Candidato } from "../models/Candidato";
import { IVagaJSON, Vaga } from "../models/Vaga"
import { Competencia } from "../models/Competencia"

export class VagaService {
    cadastrar(vaga: Vaga): void {
        const vagasSalvas = localStorage.getItem("vagas")

        const vagas: Vaga[] =
            vagasSalvas ? JSON.parse(vagasSalvas)
            : []

        vagas.push(vaga)

        localStorage.setItem(
            "vagas",
            JSON.stringify(vagas)
        )

    }

    adicionarCompetencia(vaga: Vaga, competencia: Competencia): void {
         if (vaga != null && competencia != null) {
            vaga.addCompetencia(competencia)
        } else {
            throw "Vaga ou competência não podem ser nulos"
        }
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

    exibirVagasEConverter(): Vaga[] {
        const vagasSalvas = localStorage.getItem("vagas")
        if (vagasSalvas == null) {
            return []
        }

        const vagasJSON: IVagaJSON[] = JSON.parse(vagasSalvas)

        const vagas: Vaga[] = vagasJSON.map(
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
        })

        return vagas
    }

}