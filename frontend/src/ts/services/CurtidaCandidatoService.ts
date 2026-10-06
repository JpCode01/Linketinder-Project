import { CurtidaCandidato } from "../models/CurtidaCandidato"
import { CadastroRepository } from "../repository/CadastroRepository"
import { CurtidaCandidatoConsultaRepository } from "../repository/empresa/contrato/CurtidaCandidatoConsultaRepository"
import { EmpresaConsultaRepository } from "../repository/empresa/contrato/EmpresaConsultaRepository"
import { CandidatoConsultaRepository } from "../repository/candidato/contrato/CandidatoConsultaRepository"
import { EmpresaNaoEncontradaException } from "../exceptions/empresa/EmpresaNaoEncontradaException"
import { CandidatoNaoEncontradoException } from "../exceptions/candidato/CandidatoNaoEncontradoException"
import { CandidatoJaCurtidoException } from "../exceptions/candidato/CandidatoJaCurtidoException"

export class CurtidaCandidatoService {

    constructor(
        private curtidaCandidatoCadastroRepository: CadastroRepository<CurtidaCandidato>,
        private curtidaCandidatoConsultaRepository: CurtidaCandidatoConsultaRepository,
        private empresaConsultaRepository: EmpresaConsultaRepository,
        private candidatoConsultaRepository: CandidatoConsultaRepository
    ) {}

    curtir(idEmpresa: number, idCandidato: number, idVaga: number): void {
       const empresa = this.empresaConsultaRepository.buscarPorId(idEmpresa)

        if (empresa == undefined) {
            throw new EmpresaNaoEncontradaException(idEmpresa)
        }

        const candidato = this.candidatoConsultaRepository.buscarPorId(idCandidato)

        if (candidato == undefined) {
            throw new CandidatoNaoEncontradoException(idCandidato)
        }

        const curtidaExistente =
            this.curtidaCandidatoConsultaRepository.buscar(
                idEmpresa,
                idCandidato,
                idVaga
            )

        if (curtidaExistente != undefined) {
            throw new CandidatoJaCurtidoException(idCandidato)
        }

        const curtida = new CurtidaCandidato(
            idEmpresa,
            idCandidato,
            idVaga
        )

        this.curtidaCandidatoCadastroRepository.salvar(curtida)
    }

    buscarPorEmpresa(idEmpresa: number): CurtidaCandidato[] {
        const empresaProcurada = this.empresaConsultaRepository.buscarPorId(idEmpresa)

        if (empresaProcurada == undefined) {
            throw new EmpresaNaoEncontradaException(idEmpresa)
        }

        return this.curtidaCandidatoConsultaRepository.buscarPorEmpresa(idEmpresa)
    }

    buscarPorCandidato(idCandidato: number): CurtidaCandidato[] {
        const candidatoProcurado = this.candidatoConsultaRepository.buscarPorId(idCandidato)

        if (candidatoProcurado == undefined) {
            throw new CandidatoNaoEncontradoException(idCandidato)
        }

        return this.curtidaCandidatoConsultaRepository.buscarPorCandidato(idCandidato)
    }
}