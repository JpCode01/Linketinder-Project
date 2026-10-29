import { CurtidaCandidato } from "../models/CurtidaCandidato"
import { CadastroRepository } from "../repository/CadastroRepository"
import { CurtidaCandidatoConsultaRepository } from "../repository/empresa/contrato/CurtidaCandidatoConsultaRepository"
import { EmpresaConsultaRepository } from "../repository/empresa/contrato/EmpresaConsultaRepository"
import { CandidatoConsultaRepository } from "../repository/candidato/contrato/CandidatoConsultaRepository"
import { EmpresaNaoEncontradaException } from "../exceptions/empresa/EmpresaNaoEncontradaException"
import { CandidatoNaoEncontradoException } from "../exceptions/candidato/CandidatoNaoEncontradoException"
import { CandidatoJaCurtidoException } from "../exceptions/candidato/CandidatoJaCurtidoException"
import { CurtidaCandidatoApiClient } from "../api/clientes/CurtidaCandidatoApiClient";

export class CurtidaCandidatoService {

    constructor(
        private curtidaCandidatoCadastroRepository: CadastroRepository<CurtidaCandidato>,
        private curtidaCandidatoConsultaRepository: CurtidaCandidatoConsultaRepository,
        private empresaConsultaRepository: EmpresaConsultaRepository,
        private candidatoConsultaRepository: CandidatoConsultaRepository,
        private curtidaCandidatoApiClient: CurtidaCandidatoApiClient
    ) {}

    async curtir(
        idEmpresa: number,
        idCandidato: number,
        idVaga: number
    ): Promise<void> {
        await this.curtidaCandidatoApiClient.curtir(
            idEmpresa,
            idCandidato,
            idVaga
        )
    }

    buscarPorEmpresa(idEmpresa: number): CurtidaCandidato[] {
        return this.curtidaCandidatoConsultaRepository.buscarPorEmpresa(
            idEmpresa
        )
    }

    buscarPorCandidato(idCandidato: number): CurtidaCandidato[] {
        const candidatoProcurado = this.candidatoConsultaRepository.buscarPorId(idCandidato)

        if (candidatoProcurado == undefined) {
            throw new CandidatoNaoEncontradoException(idCandidato)
        }

        return this.curtidaCandidatoConsultaRepository.buscarPorCandidato(idCandidato)
    }
}