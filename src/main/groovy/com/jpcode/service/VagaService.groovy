package com.jpcode.service

import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.dao.vaga.CompetenciasVagaDAO
import com.jpcode.dao.vaga.VagaDAO
import com.jpcode.model.core.Candidato
import com.jpcode.model.core.Vaga

class VagaService {

    final CompetenciaDAO competenciaDAO
    final CompetenciasVagaDAO competenciasVagaDAO
    final VagaDAO vagaDAO

    VagaService(CompetenciaDAO competenciaDAO, CompetenciasVagaDAO competenciasVagaDAO, VagaDAO vagaDAO) {
        this.competenciaDAO = competenciaDAO
        this.competenciasVagaDAO = competenciasVagaDAO
        this.vagaDAO = vagaDAO
    }

    Vaga criarVaga(
            String nome,
            String descricao,
            String local,
            Long idEmpresa,
            List<String> competencias
    ) {
        Vaga vaga = new Vaga(
                nome,
                descricao,
                local,
                idEmpresa
        )

        Vaga vagaSalva = vagaDAO.salvar(vaga)

        competencias.each {
            competencia ->
                Long idCompetenciaNormalizada = competenciaDAO.buscarIdPorNomeCompetencia(competencia)
                if (idCompetenciaNormalizada != null) {
                    competenciasVagaDAO.salvar(vagaSalva.id, idCompetenciaNormalizada)
                }
        }

        return vagaSalva
    }

    void curtir(Candidato candidato, Vaga vaga) {
        if (vaga && candidato && !candidato.vagasCurtidas.contains(vaga) &&
                !vaga.candidatosQueCurtiram.contains(candidato)
        ) {
            vaga.adicionarCandidatoQueCurtiu(candidato)
            candidato.adicionarVagaCurtida(vaga)
        }
    }

    List<Vaga> listarVagas(List<Vaga> vagas) {
        if (!vagas.isEmpty()) {
            vagas.eachWithIndex { vaga, index ->
                println "$index - ${vaga.nome}"
            }
        }
        return vagas

    } 
}
