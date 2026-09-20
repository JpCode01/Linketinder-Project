package com.jpcode.service

import com.jpcode.dao.candidato.CandidatoDAO
import com.jpcode.dao.candidato.CompetenciasCandidatoDAO
import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.dao.referencia.EstadoDAO
import com.jpcode.dao.referencia.PaisDAO
import com.jpcode.model.core.Candidato

import java.time.LocalDate

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
    
    Candidato cadastrarCandidato(String nome, 
                                 String sobrenome,
                                 String email,
                                 String senha,
                                 String cpf,
                                 LocalDate dataNascimento,
                                 String pais,
                                 int idade,
                                 String estado,
                                 String cep,
                                 String descricao,
                                 List<String> competencias) {
        Long paisId = paisDAO.buscarIdPorNome(pais)
        Long estadoId = estadoDAO.buscarIdPorSigla(estado)
        Candidato candidato = new Candidato(
                nome,
                sobrenome,
                email,
                senha,
                cpf,
                dataNascimento,
                paisId,
                idade,
                estadoId,
                cep,
                descricao

        )

        Candidato candidatoSalvo = candidatoDAO.salvar(candidato)
        
        competencias.each {
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
}
