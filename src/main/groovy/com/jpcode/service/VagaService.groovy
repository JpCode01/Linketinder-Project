package com.jpcode.service

import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.dao.relacionamento.contrato.CandidatoCurtirRepository
import com.jpcode.dao.vaga.contrato.CompetenciasVagaRepository
import com.jpcode.dao.vaga.contrato.VagaRepository
import com.jpcode.dto.vaga.AtualizarVagaDTO
import com.jpcode.dto.vaga.CadastrarVagaDTO
import com.jpcode.dto.vaga.VagaAnonimaDTO
import com.jpcode.dto.vaga.VagaEmpresaDTO
import com.jpcode.exception.referencia.CompetenciaNaoEncontradaException
import com.jpcode.exception.vaga.VagaNaoEncontradaException
import com.jpcode.model.core.Vaga
import com.jpcode.model.referencia.Competencia

class VagaService {

    final CompetenciaDAO competenciaDAO
    final CompetenciasVagaRepository competenciasVagaRepository
    final VagaRepository vagaRepository
    final CandidatoCurtirRepository candidatoCurtirRepository

    VagaService(CompetenciaDAO competenciaDAO, CompetenciasVagaRepository competenciasVagaRepository, VagaRepository vagaRepository, CandidatoCurtirRepository candidatoCurtirRepository) {
        this.competenciaDAO = competenciaDAO
        this.competenciasVagaRepository = competenciasVagaRepository
        this.vagaRepository = vagaRepository
        this.candidatoCurtirRepository = candidatoCurtirRepository
    }

    void criarVaga(CadastrarVagaDTO cadastrarVagaDTO) {
        Vaga vagaSalva = vagaRepository.salvar(
                new Vaga(
                        cadastrarVagaDTO.nome,
                        cadastrarVagaDTO.descricao,
                        cadastrarVagaDTO.local,
                        cadastrarVagaDTO.idEmpresa
                )
        )

        cadastrarVagaDTO.competencias.each { competencia ->
            Long idCompetencia = competenciaDAO
                    .buscarIdPorNome(competencia)
                    .orElseThrow(() ->
                            new CompetenciaNaoEncontradaException(competencia)
                    )

            competenciasVagaRepository.salvar(
                    vagaSalva.id,
                    idCompetencia
            )
        }
    }

    void curtir(Long idCandidato, Long idVaga) {
        candidatoCurtirRepository.salvar(idCandidato, idVaga)
    }

    List<VagaEmpresaDTO> listarVagas(Long idEmpresa) {
        return vagaRepository.buscarVagasEmpresa(idEmpresa)
    }

    Vaga buscarVaga(Long idVaga) {
        return vagaRepository.buscarPorId(idVaga)
                .orElseThrow(() ->
                        new VagaNaoEncontradaException(idVaga))
    }

    List<VagaAnonimaDTO> listarVagasCurtidas(long idCandidato) {
        return candidatoCurtirRepository.buscarVagasCurtidas(idCandidato)
    }

    List<VagaAnonimaDTO> buscarTodasAsVagas() {
        return vagaRepository.buscarTodasAsVagas()
    }

    List<Competencia> buscarCompetenciasDeVaga(Long idVaga) {
        return competenciasVagaRepository.buscarPorVaga(idVaga)
    }

    void deletarVaga(Long idVaga) {
        vagaRepository.deletar(idVaga)
    }

    void atualizarVaga(AtualizarVagaDTO atualizarVagaDTO) {
        vagaRepository.atualizarDados(
                new Vaga(
                        atualizarVagaDTO.id,
                        atualizarVagaDTO.nome,
                        atualizarVagaDTO.descricao,
                        atualizarVagaDTO.local,
                        atualizarVagaDTO.idEmpresa
                )
        )
    }

    List<String> competenciasEmString(Long idVaga) {
        return competenciasVagaRepository.buscarPorVagaString(idVaga)
    }

    void adicionarCompetencias(Long idVaga, List<String> competenciasNovas) {
        List<Competencia> converterParaCompetencia = []
        competenciasNovas.each { competenciaString ->
            converterParaCompetencia.add(
                    competenciaDAO.buscarPorNome(competenciaString))
        }
        competenciasVagaRepository.atualizar(idVaga, converterParaCompetencia)
    }

}
