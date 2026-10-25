package com.jpcode.service

import com.jpcode.dao.contratos.*
import com.jpcode.dto.vaga.AtualizarVagaDTO
import com.jpcode.dto.vaga.CadastrarVagaDTO
import com.jpcode.dto.vaga.VagaAnonimaDTO
import com.jpcode.dto.vaga.VagaEmpresaDTO
import com.jpcode.exception.referencia.CompetenciaNaoEncontradaException
import com.jpcode.exception.vaga.VagaNaoEncontradaException
import com.jpcode.model.core.Vaga
import com.jpcode.model.referencia.Competencia

class VagaService {

    final VagaRepository vagaRepository
    final ReferenciaRepository<Competencia> referenciaRepository
    final ReferenciaCompetenciaRepository referenciaCompetencia
    final CompetenciaRepository competenciaRepository
    final CurtidaRepository<VagaAnonimaDTO> curtidaRepository

    VagaService(VagaRepository vagaRepository, ReferenciaRepository<Competencia> referenciaRepository, ReferenciaCompetenciaRepository referenciaCompetencia, CompetenciaRepository competenciaRepository, CurtidaRepository<VagaAnonimaDTO> curtidaRepository) {
        this.vagaRepository = vagaRepository
        this.referenciaRepository = referenciaRepository
        this.referenciaCompetencia = referenciaCompetencia
        this.competenciaRepository = competenciaRepository
        this.curtidaRepository = curtidaRepository
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
        salvarCompetencias(cadastrarVagaDTO.competencias, vagaSalva.id)
    }

    private salvarCompetencias(List<String> competencias, Long idVaga) {
        competencias.each {
            competencia ->
                Long idCompetencia = referenciaRepository
                        .buscarIdPorNome(competencia)
                        .orElseThrow(() ->
                                new CompetenciaNaoEncontradaException(competencia)
                        )

                competenciaRepository.salvar(
                        idVaga,
                        idCompetencia
                )
        }
    }

    void curtir(Long idCandidato, Long idVaga) {
        curtidaRepository.salvar(idCandidato, idVaga)
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
        return curtidaRepository.buscarCurtidas(idCandidato)
    }

    List<VagaAnonimaDTO> buscarTodasAsVagas() {
        return vagaRepository.buscarTodasAsVagas()
    }

    List<Competencia> buscarCompetenciasDeVaga(Long idVaga) {
        return competenciaRepository.buscarPorId(idVaga)
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
        return competenciaRepository.buscarEmString(idVaga)
    }

    void adicionarCompetencias(Long idVaga, List<String> competenciasNovas) {
        List<Competencia> converterParaCompetencia = []
        competenciasNovas.each { competenciaString ->
            converterParaCompetencia.add(
                    referenciaCompetencia.buscarPorNome(competenciaString))
        }
        competenciaRepository.atualizar(idVaga, converterParaCompetencia)
    }
}