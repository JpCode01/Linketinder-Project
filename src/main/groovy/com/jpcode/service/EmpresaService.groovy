package com.jpcode.service

import com.jpcode.enums.CompetenciasEnum
import com.jpcode.model.Candidato
import com.jpcode.model.Empresa
import com.jpcode.model.Vaga
import com.jpcode.validation.CompetenciaValidation

class EmpresaService {
    final CompetenciaValidation validation

    EmpresaService(CompetenciaValidation validation) {
        this.validation = validation
    }

    Empresa cadastrarEmpresa(
            String nome,
            String email,
            String cnpj,
            String pais,
            String estado,
            String cep,
            String descricao,
            List<String> competencias
    ) {
        Empresa empresa = new Empresa(
                nome,
                email,
                cnpj,
                pais,
                estado,
                cep,
                descricao
        )

        competencias.each { competencia ->
            String competenciaNormalizada = competencia.toUpperCase()

            if (validation.validarCompetencia(
                    competenciaNormalizada,
                    empresa.competencias
            )) {
                empresa.adicionarCompetencia(
                        CompetenciasEnum.valueOf(competenciaNormalizada)
                )
            }
        }

        return empresa
    }
    void curtirCandidato(Candidato candidato, Empresa empresa) {
        empresa.adicionarCandidatoCurtido(candidato)
    }

    void ListarVagasPorEmpresa(Empresa empresa) {
        println(empresa.vagas)
    }

    void ListarCandidatosPorVaga(Vaga vaga) {
        vaga.candidatosQueCurtiram.eachWithIndex { candidato, index ->
            println "$index - Candidato anônimo ${index + 1}"
        }
    }
}
