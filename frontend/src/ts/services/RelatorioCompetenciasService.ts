
import { VagaService } from "./VagaService"
import { CurtidaVagaService } from "./CurtidaVagaService"

export class RelatorioCompetenciasService {

    constructor(
        private vagaService: VagaService,
        private curtidaVagaService: CurtidaVagaService
    ) {}

    async contarCompetencias(
        idEmpresa: number
    ): Promise<Map<string, number>> {
        const competencias = new Map<string, number>()

        const vagas =
            await this.vagaService.buscarPorEmpresa(idEmpresa)

        for (const vaga of vagas) {
            const candidatos =
                await this.curtidaVagaService.buscarCandidatosQueCurtiram(
                    vaga.id
                )

            for (const candidato of candidatos) {
                for (const competencia of candidato.competencias) {
                    const nomeCompetencia = competencia.nome
                    const quantidade =
                        competencias.get(nomeCompetencia) ?? 0

                    competencias.set(
                        nomeCompetencia,
                        quantidade + 1
                    )
                }
            }
        }

        return competencias
    }
}
