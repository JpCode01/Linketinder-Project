package com.jpcode.service

import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.dao.relacionamento.CandidatoCurtirDAO
import com.jpcode.dao.vaga.CompetenciasVagaDAO
import com.jpcode.dao.vaga.VagaDAO
import com.jpcode.dto.vaga.CadastrarVagaDTO
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

    void criarVaga(CadastrarVagaDTO cadastrarVagaDTO) {
        Vaga vagaSalva =  vagaDAO.salvar(
                new Vaga(
                        cadastrarVagaDTO.nome,
                        cadastrarVagaDTO.descricao,
                        cadastrarVagaDTO.local,
                        cadastrarVagaDTO.idEmpresa
                )
        )

        cadastrarVagaDTO.competencias.each {
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

    void atualizarVaga(
            Long id,
            String nome,
            String descricao,
            String local,
            Long idEmpresa
    ) {
        vagaDAO.atualizarDados(
                new Vaga(
                        id,
                        nome,
                        descricao,
                        local,
                        idEmpresa
                )
        )
    }

    List<String> competenciasEmString(Long idVaga) {
        return competenciasVagaDAO.buscarPorVagaString(idVaga)
    }

    void adicionarCompetencias(Long idVaga, List<String> competenciasNovas) {
        List<Competencia> converterParaCompetencia = []
        competenciasNovas.each {competenciaString ->
            converterParaCompetencia.add(
                    competenciaDAO.buscarPorNome(competenciaString))
        }
        competenciasVagaDAO.atualizar(idVaga, converterParaCompetencia)
    }

}
