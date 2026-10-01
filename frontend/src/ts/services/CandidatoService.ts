import { Candidato } from "../models/Candidato"
import { CandidatoRepository } from "../repository/CandidatoRepository"

export class CandidatoService {

    constructor(
        private candidatoRepository: CandidatoRepository
    ) {}

    salvar(candidato: Candidato): void {
        this.candidatoRepository.salvar(candidato)
    }

    buscarTodos(): Candidato[] {
        return this.candidatoRepository.buscarTodos()
    }

    buscarPorId(id: number): Candidato | undefined {
        return this.candidatoRepository.buscarPorId(id)
    }

    buscarPorEmail(email: string): Candidato | undefined {
        return this.candidatoRepository.buscarPorEmail(email)
    }
}