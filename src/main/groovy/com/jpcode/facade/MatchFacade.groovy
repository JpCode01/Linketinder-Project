package com.jpcode.facade

import com.jpcode.dao.contratos.CandidatoCurtidasConsultaRepository
import com.jpcode.dao.contratos.RetribuicaoRepository
import com.jpcode.database.ConnectionFactory
import com.jpcode.dto.candidato.CandidatoAnonimoDTO
import com.jpcode.exception.vaga.VagaNaoEncontradaException
import com.jpcode.service.MatchService
import java.sql.Connection

class MatchFacade {

    final ConnectionFactory connectionFactory
    final RetribuicaoRepository retribuicaoRepository
    final CandidatoCurtidasConsultaRepository consultaRepository
    final MatchService matchService

    MatchFacade(
            ConnectionFactory connectionFactory,
            RetribuicaoRepository retribuicaoRepository,
            CandidatoCurtidasConsultaRepository consultaRepository,
            MatchService matchService
    ) {
        this.connectionFactory = connectionFactory
        this.retribuicaoRepository = retribuicaoRepository
        this.consultaRepository = consultaRepository
        this.matchService = matchService
    }

    void curtirCandidato(
            Long idEmpresa,
            Long idCandidato,
            Long idVaga
    ) {
        retribuirInteresseEmpresa(idEmpresa, idCandidato, idVaga)
    }

    void retribuirInteresseEmpresa(Long idEmpresaOperante, Long idCandidato, Long idVaga) {
        exigirId(idEmpresaOperante)
        retribuir(idEmpresaOperante, null, idCandidato, idVaga)
    }

    void retribuirInteresseRecrutador(Long idRecrutadorOperante, Long idCandidato, Long idVaga) {
        exigirId(idRecrutadorOperante)
        retribuir(null, idRecrutadorOperante, idCandidato, idVaga)
    }

    List<CandidatoAnonimoDTO> buscarInteressadosEmpresa(Long idEmpresaOperante, Long idVaga) {
        exigirId(idEmpresaOperante)
        return consultar(idEmpresaOperante, null, idVaga)
    }

    List<CandidatoAnonimoDTO> buscarInteressadosRecrutador(Long idRecrutadorOperante, Long idVaga) {
        exigirId(idRecrutadorOperante)
        return consultar(null, idRecrutadorOperante, idVaga)
    }

    private void retribuir(Long empresa, Long recrutador, Long candidato, Long vaga) {
        exigirId(candidato)
        emTransacao { Connection conexao ->
            Long proprietaria = validarContexto(conexao, empresa, recrutador, candidato, vaga)
            if (!retribuicaoRepository.bloquearInteresse(conexao, candidato, vaga)) {
                throw new IllegalStateException('Retribuição exige interesse prévio naquela vaga.')
            }
            retribuicaoRepository.salvar(conexao, proprietaria, candidato, vaga, recrutador)
            matchService.salvarNaTransacao(conexao, candidato, proprietaria, vaga)
        }
    }

    private List<CandidatoAnonimoDTO> consultar(Long empresa, Long recrutador, Long vaga) {
        return emTransacao { Connection conexao ->
            validarContexto(conexao, empresa, recrutador, null, vaga)
            consultaRepository.buscarCandidatosQueCurtiram(conexao, vaga)
        }
    }

    private Long validarContexto(Connection conexao, Long empresa, Long recrutador, Long candidato, Long idVaga) {
        exigirId(idVaga)
        Long proprietaria = retribuicaoRepository.buscarEmpresaDaVaga(conexao, idVaga)
        if (proprietaria == null) throw new VagaNaoEncontradaException(idVaga)
        if (empresa != null && empresa != proprietaria) throw new SecurityException('Vaga fora do escopo da empresa.')
        // Ordem comum: empresa, recrutador operante, candidato, vaga, interesse.
        // SHARE protege atividade/vínculo; UPDATE da vaga serializa retribuições
        // e exclusão. Revalidar a leitura preliminar depois de obter o lock.
        if (!retribuicaoRepository.bloquearEmpresaAtiva(conexao, proprietaria)) {
            throw new IllegalStateException('Empresa inexistente ou inativa.')
        }
        if (recrutador != null && retribuicaoRepository.bloquearRecrutadorAtivo(conexao, recrutador) != proprietaria) {
            throw new SecurityException('Recrutador indisponível ou fora do escopo da empresa.')
        }
        if (candidato != null && !retribuicaoRepository.bloquearCandidatoAtivo(conexao, candidato)) {
            throw new IllegalStateException('Candidato inexistente ou inativo.')
        }
        def vaga = retribuicaoRepository.bloquearVaga(conexao, idVaga)
        if (vaga == null) throw new VagaNaoEncontradaException(idVaga)
        if (vaga.idEmpresa != proprietaria || (recrutador != null && vaga.idRecrutador != recrutador)) {
            throw new SecurityException('Vaga fora do escopo do ator.')
        }
        return proprietaria
    }

    private def emTransacao(Closure operacao) {
        try (Connection conexao = connectionFactory.getConnection()) {
            conexao.transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
            conexao.autoCommit = false
            try {
                def resultado = operacao.call(conexao)
                conexao.commit()
                return resultado
            } catch (Exception erro) {
                conexao.rollback()
                throw erro
            }
        }
    }

    private static void exigirId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException('Identificador obrigatório e positivo.')
    }
}
