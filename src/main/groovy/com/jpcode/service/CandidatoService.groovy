package com.jpcode.service

import com.jpcode.enums.CompetenciasEnum
import com.jpcode.model.Candidato
import com.jpcode.validation.CompetenciaValidation

class CandidatoService {
    final CompetenciaValidation validation

    CandidatoService(CompetenciaValidation validation) {
        this.validation = validation
    }
    
    Candidato cadastrarCandidato(String nome, 
                                 String email,
                                 String cpf,
                                 int idade,
                                 String estado,
                                 String cep,
                                 String descricao,
                                 List<String> competencias) {
        Candidato candidato = new Candidato(nome, email, cpf, idade, estado, cep, descricao)

        competencias.each { competencia ->
            String competenciaNormalizada = competencia.toUpperCase()

            if (validation.validarCompetencia(
                    competenciaNormalizada,
                    candidato.competencias
            )) {
                candidato.adicionarCompetencia(
                        CompetenciasEnum.valueOf(competenciaNormalizada)
                )
            }
        }
        return candidato
    }
}
