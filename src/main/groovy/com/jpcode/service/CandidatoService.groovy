package com.jpcode.service

import com.jpcode.dao.candidato.contrato.CandidatoAutenticacao
import com.jpcode.dao.candidato.contrato.CandidatoDesativacao
import com.jpcode.dao.candidato.contrato.CandidatoRepository
import com.jpcode.dao.candidato.contrato.CompetenciasCandidatoRepository
import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.dao.referencia.contrato.ReferenciaRepository
import com.jpcode.dto.candidato.AtualizarCandidatoDTO
import com.jpcode.dto.candidato.CadastrarCandidatoDTO
import com.jpcode.dto.competencia.RemoverCompetenciaDTO
import com.jpcode.exception.candidato.CandidatoLoginException
import com.jpcode.exception.candidato.CandidatoNaoEncontradoPorIdException
import com.jpcode.exception.referencia.CompetenciaNaoEncontradaException
import com.jpcode.exception.referencia.EstadoNaoEncontradoException
import com.jpcode.exception.referencia.PaisNaoEncontradoException
import com.jpcode.model.core.Candidato
import com.jpcode.model.referencia.Competencia
import com.jpcode.model.referencia.Estado
import com.jpcode.model.referencia.Pais

class CandidatoService {
    final ReferenciaRepository<Pais> paisRepository
    final ReferenciaRepository<Estado> estadoRepository
    final CompetenciaDAO competenciaDAO
    final CompetenciasCandidatoRepository competenciasCandidatoRepository
    final CandidatoRepository candidatoRepository
    final CandidatoAutenticacao candidatoAutenticacao
    final CandidatoDesativacao candidatoDesativacao

    CandidatoService(ReferenciaRepository<Pais> paisRepository, ReferenciaRepository<Estado> estadoRepository, CompetenciaDAO competenciaDAO, CompetenciasCandidatoRepository competenciasCandidatoRepository, CandidatoRepository candidatoRepository, CandidatoAutenticacao candidatoAutenticacao, CandidatoDesativacao candidatoDesativacao) {
        this.paisRepository = paisRepository
        this.estadoRepository = estadoRepository
        this.competenciaDAO = competenciaDAO
        this.competenciasCandidatoRepository = competenciasCandidatoRepository
        this.candidatoRepository = candidatoRepository
        this.candidatoAutenticacao = candidatoAutenticacao
        this.candidatoDesativacao = candidatoDesativacao
    }

    void cadastrarCandidato(CadastrarCandidatoDTO cadastrarCandidatoDTO) {
        Long paisId = paisRepository.buscarIdPorNome(cadastrarCandidatoDTO.pais)
                .orElseThrow(() ->
                        new PaisNaoEncontradoException(cadastrarCandidatoDTO.pais)
                )
        Long estadoId = estadoRepository.buscarIdPorNome(cadastrarCandidatoDTO.estado)
                .orElseThrow(() ->
                new EstadoNaoEncontradoException(cadastrarCandidatoDTO.estado)
        )

        Candidato candidatoSalvo = candidatoRepository.salvar(
                    new Candidato(
                            cadastrarCandidatoDTO.nome,
                            cadastrarCandidatoDTO.sobrenome,
                            cadastrarCandidatoDTO.email,
                            cadastrarCandidatoDTO.senha,
                            cadastrarCandidatoDTO.cpf,
                            cadastrarCandidatoDTO.dataNascimento,
                            paisId,
                            cadastrarCandidatoDTO.idade,
                            estadoId,
                            cadastrarCandidatoDTO.cep,
                            cadastrarCandidatoDTO.descricao
                    )
        )
        salvarCompetencias(cadastrarCandidatoDTO.competencias, candidatoSalvo.id)
    }

    private salvarCompetencias(List<String> competencias, Long idCandidato) {
        competencias.each {
            competencia ->
                Long idCompetencia = competenciaDAO
                        .buscarIdPorNome(competencia)
                        .orElseThrow(() ->
                                new CompetenciaNaoEncontradaException(competencia)
                        )

                competenciasCandidatoRepository.salvar(
                        idCandidato,
                        idCompetencia
                )
        }
    }

    Candidato buscarCandidato(Long idCandidato) {
        return candidatoRepository.buscarPorId(idCandidato)
        .orElseThrow(() ->
                    new CandidatoNaoEncontradoPorIdException(idCandidato))
    }

    Candidato logar(String email, String senha) {
        return candidatoAutenticacao.buscarPorEmailESenha(email, senha)
        .orElseThrow(() ->
        new CandidatoLoginException(email, senha))
    }

    void desativarCandidato(Long idCandidato) {
        candidatoDesativacao.desativar(idCandidato)
    }
    
    void atualizarCandidato(AtualizarCandidatoDTO atualizarCandidatoDTO) {
        Long paisId = paisRepository.buscarIdPorNome(atualizarCandidatoDTO.pais)
                .orElseThrow(() ->
                        new PaisNaoEncontradoException(atualizarCandidatoDTO.pais)
                )
        Long estadoId = estadoRepository.buscarIdPorNome(atualizarCandidatoDTO.estado)
                .orElseThrow(() ->
                        new EstadoNaoEncontradoException(atualizarCandidatoDTO.estado)
                )

        candidatoRepository.atualizarDados(
                new Candidato(
                        atualizarCandidatoDTO.id,
                        atualizarCandidatoDTO.nome,
                        atualizarCandidatoDTO.sobrenome,
                        atualizarCandidatoDTO.email,
                        atualizarCandidatoDTO.senha,
                        atualizarCandidatoDTO.cpf,
                        atualizarCandidatoDTO.dataNascimento,
                        paisId,
                        atualizarCandidatoDTO.idade,
                        estadoId,
                        atualizarCandidatoDTO.cep,
                        atualizarCandidatoDTO.descricao,
                        atualizarCandidatoDTO.ativo
                ))

    }


    void adicionarCompetencias(Long idCandidato, List<String> competenciasNovas) {
        List<Competencia> converterParaCompetencia = []
        competenciasNovas.each {competenciaString ->
            converterParaCompetencia.add(
                    competenciaDAO.buscarPorNome(competenciaString))
        }
        competenciasCandidatoRepository.atualizar(idCandidato, converterParaCompetencia)
    }

    void removerCompetencia(Long idCandidato, Long idCompetencia) {
        competenciasCandidatoRepository.removerCompetencia(idCandidato, idCompetencia)
    }

    List<RemoverCompetenciaDTO> listaParaRemover(Long idCandidato) {
        return competenciaDAO.converterCompetenciasParaDTO(competenciasCandidatoRepository.buscarPorCandidato(idCandidato))
    }

    List<String> competenciasEmString(Long idCandidato) {
        return competenciasCandidatoRepository.buscarPorCandidatoString(idCandidato)
    }
    
}
