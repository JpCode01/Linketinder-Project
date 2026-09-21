package com.jpcode.service

import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.dao.relacionamento.CandidatoCurtirDAO
import com.jpcode.dao.vaga.CompetenciasVagaDAO
import com.jpcode.dao.vaga.VagaDAO
import com.jpcode.dto.vaga.VagaAnonimaDTO
import com.jpcode.dto.vaga.VagaEmpresaDTO
import com.jpcode.model.core.Vaga
import com.jpcode.model.referencia.Competencia

class VagaService {

    final CompetenciaDAO competenciaDAO
    final CompetenciasVagaDAO competenciasVagaDAO
    final VagaDAO vagaDAO
    final CandidatoCurtirDAO candidatoCurtirDAO

    VagaService(CompetenciaDAO competenciaDAO, CompetenciasVagaDAO competenciasVagaDAO, VagaDAO vagaDAO, CandidatoCurtirDAO candidatoCurtirDAO) {
        this.competenciaDAO = competenciaDAO
        this.competenciasVagaDAO = competenciasVagaDAO
        this.vagaDAO = vagaDAO
        this.candidatoCurtirDAO = candidatoCurtirDAO
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

    void curtir(Long idCandidato, Long idVaga) {
        candidatoCurtirDAO.salvar(idCandidato, idVaga)
    }

    List<VagaEmpresaDTO>  listarVagas(Long idEmpresa) {
        return vagaDAO.buscarVagasEmpresa(idEmpresa)
    }

    Vaga buscarVaga(Long idVaga) {
        return vagaDAO.buscarPorId(idVaga)
    }

    List<VagaAnonimaDTO> listarVagasCurtidas(long idCandidato) {
        return candidatoCurtirDAO.buscarVagasCurtidas(idCandidato)
    }

    List<VagaAnonimaDTO> buscarTodasAsVagas() {
        return vagaDAO.buscarTodasAsVagas()
    }

    List<Competencia> buscarCompetenciasDeVaga(Long idVaga) {
        return competenciasVagaDAO.buscarPorVaga(idVaga)
    }

    void deletarVaga(Long idVaga) {
        vagaDAO.deletar(idVaga)
    }
}
