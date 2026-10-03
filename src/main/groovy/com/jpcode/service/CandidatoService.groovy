package com.jpcode.service

import com.jpcode.dao.candidato.CandidatoDAO
import com.jpcode.dao.candidato.CompetenciasCandidatoDAO
import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.dao.referencia.EstadoDAO
import com.jpcode.dao.referencia.PaisDAO
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

class CandidatoService {
    final PaisDAO paisDAO
    final EstadoDAO estadoDAO
    final CompetenciaDAO competenciaDAO
    final CompetenciasCandidatoDAO competenciasCandidatoDAO
    final CandidatoDAO candidatoDAO

    CandidatoService(PaisDAO paisDAO, EstadoDAO estadoDAO, CompetenciaDAO competenciaDAO, CompetenciasCandidatoDAO competenciasCandidatoDAO, CandidatoDAO candidatoDAO) {
        this.paisDAO = paisDAO
        this.estadoDAO = estadoDAO
        this.competenciaDAO = competenciaDAO
        this.competenciasCandidatoDAO = competenciasCandidatoDAO
        this.candidatoDAO = candidatoDAO
    }
    
    void cadastrarCandidato(CadastrarCandidatoDTO cadastrarCandidatoDTO) {
        Long paisId = paisDAO.buscarIdPorNome(cadastrarCandidatoDTO.pais)
                .orElseThrow(() ->
                        new PaisNaoEncontradoException(cadastrarCandidatoDTO.pais)
                )
        Long estadoId = estadoDAO.buscarIdPorSigla(cadastrarCandidatoDTO.estado)
                .orElseThrow(() ->
                new EstadoNaoEncontradoException(cadastrarCandidatoDTO.estado)
        )

        Candidato candidatoSalvo = candidatoDAO.salvar(
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
                        .buscarIdPorNomeCompetencia(competencia)
                        .orElseThrow(() ->
                                new CompetenciaNaoEncontradaException(competencia)
                        )

                competenciasCandidatoDAO.salvar(
                        idCandidato,
                        idCompetencia
                )
        }
    }

    Candidato buscarCandidato(Long idCandidato) {
        return candidatoDAO.buscarPorId(idCandidato)
        .orElseThrow(() ->
                    new CandidatoNaoEncontradoPorIdException(idCandidato))
    }

    Candidato logar(String email, String senha) {
        return candidatoDAO.buscarPorEmailESenha(email, senha)
        .orElseThrow(() ->
        new CandidatoLoginException(email, senha))
    }

    void desativarCandidato(Long idCandidato) {
        candidatoDAO.desativar(idCandidato)
    }
    
    void atualizarCandidato(AtualizarCandidatoDTO atualizarCandidatoDTO) {
        Long paisId = paisDAO.buscarIdPorNome(atualizarCandidatoDTO.pais)
                .orElseThrow(() ->
                        new PaisNaoEncontradoException(atualizarCandidatoDTO.pais)
                )
        Long estadoId = estadoDAO.buscarIdPorSigla(atualizarCandidatoDTO.estado)
                .orElseThrow(() ->
                        new EstadoNaoEncontradoException(atualizarCandidatoDTO.estado)
                )

        candidatoDAO.atualizarDados(
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
        competenciasCandidatoDAO.atualizar(idCandidato, converterParaCompetencia)
    }

    void removerCompetencia(Long idCandidato, Long idCompetencia) {
        competenciasCandidatoDAO.removerCompetencia(idCandidato, idCompetencia)
    }

    List<RemoverCompetenciaDTO> listaParaRemover(Long idCandidato) {
        return competenciaDAO.converterCompetenciasParaDTO(competenciasCandidatoDAO.buscarPorCandidato(idCandidato))
    }

    List<String> competenciasEmString(Long idCandidato) {
        return competenciasCandidatoDAO.buscarPorCandidatoString(idCandidato)
    }
    
}
