package com.jpcode.service

import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.dao.vaga.CompetenciasVagaDAO
import com.jpcode.dao.vaga.VagaDAO
import com.jpcode.dto.vaga.VagaEmpresaDTO
import com.jpcode.model.core.Candidato
import com.jpcode.model.core.Vaga
import com.jpcode.model.referencia.Competencia

class VagaService {

    final CompetenciaDAO competenciaDAO
    final CompetenciasVagaDAO competenciasVagaDAO
    final VagaDAO vagaDAO

    VagaService(CompetenciaDAO competenciaDAO, CompetenciasVagaDAO competenciasVagaDAO, VagaDAO vagaDAO) {
        this.competenciaDAO = competenciaDAO
        this.competenciasVagaDAO = competenciasVagaDAO
        this.vagaDAO = vagaDAO
    }

    void criarVaga(
            String nome,
            String descricao,
            String local,
            Long idEmpresa,
            List<String> competencias
    ) {
        Vaga vagaSalva =  vagaDAO.salvar(
                new Vaga(
                        nome,
                        descricao,
                        local,
                        idEmpresa
                )
        )

        List<Competencia> competenciasNormalizadas = []

        competencias.each {
            competencia ->
            Long idCompetenciaNormalizada =
                    competenciaDAO.buscarIdPorNomeCompetencia(competencia)
            if (idCompetenciaNormalizada != null) {
                competenciasVagaDAO.salvar(vagaSalva.id, idCompetenciaNormalizada)
            }
        }
    }

    void curtir(Candidato candidato, Vaga vaga) {
        if (vaga && candidato && !candidato.vagasCurtidas.contains(vaga) &&
                !vaga.candidatosQueCurtiram.contains(candidato)
        ) {
            vaga.adicionarCandidatoQueCurtiu(candidato)
            candidato.adicionarVagaCurtida(vaga)
        }
    }

    List<VagaEmpresaDTO>  listarVagas(Long idEmpresa) {
        return vagaDAO.buscarVagasEmpresa(idEmpresa)
    }

    Vaga buscarVaga(Long idVaga) {
        return vagaDAO.buscarPorId(idVaga)
    }
}
