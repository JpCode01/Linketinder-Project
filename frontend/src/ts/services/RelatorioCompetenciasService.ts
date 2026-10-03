
import { VagaService } from "./VagaService"
import { CurtidaVagaService } from "./CurtidaVagaService"
import { CandidatoService } from "./CandidatoService"

export class RelatorioCompetenciasService {

    constructor(
        private vagaService: VagaService,
        private curtidaVagaService: CurtidaVagaService,
        private candidatoService: CandidatoService
    ) {}

    contarCompetencias(idEmpresa: number): Map<string, number> {
        const competencias = new Map<string, number>()

        const vagas = this.vagaService.buscarPorEmpresa(idEmpresa)

        for (const vaga of vagas) {
            const curtidas = this.curtidaVagaService.buscarPorVaga(vaga.id)

            for (const curtida of curtidas) {
                const candidato = this.candidatoService.buscarPorId(
                    curtida.idCandidato
                )

                for (const competencia of candidato.competencias) {
                    const quantidade = competencias.get(competencia) ?? 0

                    competencias.set(competencia, quantidade + 1)
                }
            }
        }

        return competencias
    }
}