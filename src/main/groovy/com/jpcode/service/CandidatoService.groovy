package com.jpcode.service

import com.jpcode.dao.candidato.CandidatoDAO
import com.jpcode.dao.candidato.CompetenciasCandidatoDAO
import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.dao.referencia.EstadoDAO
import com.jpcode.dao.referencia.PaisDAO
import com.jpcode.model.core.Candidato
import com.jpcode.validation.CompetenciaValidation

import java.time.LocalDate

class CandidatoService {
    final CompetenciaValidation validation
    final PaisDAO paisDAO
    final EstadoDAO estadoDAO
    final CompetenciaDAO competenciaDAO
    final CompetenciasCandidatoDAO competenciasCandidatoDAO
    final CandidatoDAO candidatoDAO

    CandidatoService(CompetenciaValidation validation, PaisDAO paisDAO, EstadoDAO estadoDAO, CompetenciaDAO competenciaDAO, CompetenciasCandidatoDAO competenciasCandidatoDAO, CandidatoDAO candidatoDAO) {
        this.validation = validation
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
}
