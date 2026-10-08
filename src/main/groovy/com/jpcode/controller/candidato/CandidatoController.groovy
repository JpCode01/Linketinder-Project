package com.jpcode.controller.candidato

import com.jpcode.dto.candidato.AtualizarCandidatoDTO
import com.jpcode.dto.candidato.CadastrarCandidatoDTO
import com.jpcode.dto.competencia.RemoverCompetenciaDTO
import com.jpcode.model.core.Candidato
import com.jpcode.service.CandidatoService

class CandidatoController {

    final CandidatoService candidatoService

    CandidatoController(CandidatoService candidatoService) {
        this.candidatoService = candidatoService
    }

    void cadastrarCandidato(CadastrarCandidatoDTO cadastrarCandidatoDTO) {
        candidatoService.cadastrarCandidato(cadastrarCandidatoDTO)
    }

    Candidato buscarCandidato(Long idCandidato) {
        return candidatoService.buscarCandidato(idCandidato)
    }

    Candidato logar(String email, String senha) {
        return candidatoService.logar(email, senha)
    }

    void desativarCandidato(Long idCandidato) {
        candidatoService.desativarCandidato(idCandidato)
    }

    void atualizarCandidato(AtualizarCandidatoDTO atualizarCandidatoDTO) {
        candidatoService.atualizarCandidato(atualizarCandidatoDTO)
    }

    void adicionarCompetencias(
            Long idCandidato,
            List<String> competenciasNovas
    ) {
        candidatoService.adicionarCompetencias(
                idCandidato,
                competenciasNovas
        )
    }

    void removerCompetencia(
            Long idCandidato,
            Long idCompetencia
    ) {
        candidatoService.removerCompetencia(
                idCandidato,
                idCompetencia
        )
    }

    List<RemoverCompetenciaDTO> listaParaRemover(Long idCandidato) {
        return candidatoService.listaParaRemover(idCandidato)
    }

    List<String> competenciasEmString(Long idCandidato) {
        return candidatoService.competenciasEmString(idCandidato)
    }
}