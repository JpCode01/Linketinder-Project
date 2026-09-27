package com.jpcode.service

import com.jpcode.dao.candidato.CandidatoDAO
import com.jpcode.dao.candidato.CompetenciasCandidatoDAO
import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.dao.referencia.EstadoDAO
import com.jpcode.dao.referencia.PaisDAO
import com.jpcode.dto.candidato.AtualizarCandidatoDTO
import com.jpcode.dto.candidato.CadastrarCandidatoDTO
import com.jpcode.dto.competencia.RemoverCompetenciaDTO
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
    
    Candidato cadastrarCandidato(CadastrarCandidatoDTO cadastrarCandidatoDTO) {
        Long paisId = paisDAO.buscarIdPorNome(cadastrarCandidatoDTO.pais)
        Long estadoId = estadoDAO.buscarIdPorSigla(cadastrarCandidatoDTO.estado)
        Candidato candidato = new Candidato(
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

        Candidato candidatoSalvo = candidatoDAO.salvar(candidato)
        
        cadastrarCandidatoDTO.competencias.each {
            competencia ->
                Long idCompetenciaNormalizada = competenciaDAO.buscarIdPorNomeCompetencia(competencia)
                if (idCompetenciaNormalizada != null) {
                    competenciasCandidatoDAO.salvar(candidatoSalvo.id, idCompetenciaNormalizada)
                }
        }

        return candidatoSalvo
    }

    Candidato buscarCandidato(Long idCandidato) {
        return candidatoDAO.buscarPorId(idCandidato)
    }

    Candidato logar(String email, String senha) {
        Candidato candidatoEncontrado = null
        if ((!email.isBlank()) && (!senha.isBlank())) {
            candidatoEncontrado = candidatoDAO.buscarPorEmailESenha(email, senha)
        }
        return candidatoEncontrado
    }

    void desativarCandidato(Long idCandidato) {
        candidatoDAO.desativar(idCandidato)
    }
    

    void atualizarCandidato(AtualizarCandidatoDTO atualizarCandidatoDTO) {
        Long paisId = paisDAO.buscarIdPorNome(atualizarCandidatoDTO.pais)
        Long estadoId = estadoDAO.buscarIdPorSigla(atualizarCandidatoDTO.estado)

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
