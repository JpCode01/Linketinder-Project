
import { VagaService } from "./VagaService"
import { CurtidaVagaService } from "./CurtidaVagaService"
import { CandidatoService } from "./CandidatoService"

export class RelatorioCompetenciasService {

    constructor(
        private vagaService: VagaService,
        private curtidaVagaService: CurtidaVagaService,
        private candidatoService: CandidatoService
    ) {}


    async contarCompetencias(
        idEmpresa: number
    ): Promise<Map<string, number>> {
        const competencias = new Map<string, number>()

        const vagas =
            await this.vagaService.buscarPorEmpresa(idEmpresa)

        for (const vaga of vagas) {
            const curtidas =
                this.curtidaVagaService.buscarPorVaga(vaga.id)

            for (const curtida of curtidas) {
                const candidato =
                    this.candidatoService.buscarPorId(
                        curtida.idCandidato
                    )

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