package com.jpcode.controller.vaga

import com.jpcode.dto.vaga.AtualizarVagaDTO
import com.jpcode.dto.vaga.CadastrarVagaDTO
import com.jpcode.dto.vaga.VagaAnonimaDTO
import com.jpcode.dto.vaga.VagaEmpresaDTO
import com.jpcode.model.core.Vaga
import com.jpcode.model.referencia.Competencia
import com.jpcode.service.VagaService

class VagaController {

    final VagaService vagaService

    VagaController(VagaService vagaService) {
        this.vagaService = vagaService
    }

    void criarVaga(CadastrarVagaDTO cadastrarVagaDTO) {
        vagaService.criarVaga(cadastrarVagaDTO)
    }

    void curtir(Long idCandidato, Long idVaga) {
        vagaService.curtir(idCandidato, idVaga)
    }

    List<VagaEmpresaDTO> listarVagas(Long idEmpresa) {
        return vagaService.listarVagas(idEmpresa)
    }

    Vaga buscarVaga(Long idVaga) {
        return vagaService.buscarVaga(idVaga)
    }

    List<VagaAnonimaDTO> listarVagasCurtidas(Long idCandidato) {
        return vagaService.listarVagasCurtidas(idCandidato)
    }

    List<VagaAnonimaDTO> buscarTodasAsVagas() {
        return vagaService.buscarTodasAsVagas()
    }

    List<Competencia> buscarCompetenciasDeVaga(Long idVaga) {
        return vagaService.buscarCompetenciasDeVaga(idVaga)
    }

    void deletarVaga(Long idVaga) {
        vagaService.deletarVaga(idVaga)
    }

    void atualizarVaga(AtualizarVagaDTO atualizarVagaDTO) {
        vagaService.atualizarVaga(atualizarVagaDTO)
    }

    List<String> competenciasEmString(Long idVaga) {
        return vagaService.competenciasEmString(idVaga)
    }

    void adicionarCompetencias(
            Long idVaga,
            List<String> competenciasNovas
    ) {
        vagaService.adicionarCompetencias(
                idVaga,
                competenciasNovas
        )
    }
}