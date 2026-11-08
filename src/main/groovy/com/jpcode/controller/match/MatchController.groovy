package com.jpcode.controller.match

import com.jpcode.dto.Match.MatchEncontradoDTO
import com.jpcode.dto.candidato.CandidatoAnonimoDTO
import com.jpcode.facade.MatchFacade
import com.jpcode.service.MatchService

class MatchController {

    final MatchService matchService
    final MatchFacade matchFacade

    MatchController(MatchService matchService, MatchFacade matchFacade) {
        this.matchService = matchService
        this.matchFacade = matchFacade
    }

    void retribuirInteresseEmpresa(Long idEmpresaOperante, Long idCandidato, Long idVaga) {
        matchFacade.retribuirInteresseEmpresa(idEmpresaOperante, idCandidato, idVaga)
    }

    void retribuirInteresseRecrutador(Long idRecrutadorOperante, Long idCandidato, Long idVaga) {
        matchFacade.retribuirInteresseRecrutador(idRecrutadorOperante, idCandidato, idVaga)
    }

    List<CandidatoAnonimoDTO> buscarInteressadosEmpresa(Long idEmpresaOperante, Long idVaga) {
        return matchFacade.buscarInteressadosEmpresa(idEmpresaOperante, idVaga)
    }

    List<CandidatoAnonimoDTO> buscarInteressadosRecrutador(Long idRecrutadorOperante, Long idVaga) {
        return matchFacade.buscarInteressadosRecrutador(idRecrutadorOperante, idVaga)
    }

    void salvar(
            Long idCandidato,
            Long idEmpresa,
            Long idVaga
    ) {
        matchService.salvar(
                idCandidato,
                idEmpresa,
                idVaga
        )
    }

    List<MatchEncontradoDTO> verMatchesPorEmpresa(Long idEmpresa) {
        return matchService.verMatchesPorEmpresa(idEmpresa)
    }

    List<MatchEncontradoDTO> verMatchesPorCandidato(Long idCandidato) {
        return matchService.verMatchesPorCandidato(idCandidato)
    }

    List<MatchEncontradoDTO> consultarMatchesEmpresa(Long idEmpresaOperante, Long idEmpresa) {
        return matchService.consultarMatchesEmpresa(idEmpresaOperante, idEmpresa)
    }

    List<MatchEncontradoDTO> consultarMatchesCandidato(Long idCandidatoOperante, Long idCandidato) {
        return matchService.consultarMatchesCandidato(idCandidatoOperante, idCandidato)
    }

    List<MatchEncontradoDTO> consultarMatchesRecrutador(Long idRecrutadorOperante, Long idRecrutador) {
        return matchService.consultarMatchesRecrutador(idRecrutadorOperante, idRecrutador)
    }

    void curtirCandidato(
            Long idEmpresa,
            Long idCandidato,
            Long idVaga
    ) {
        matchFacade.curtirCandidato(
                idEmpresa,
                idCandidato,
                idVaga
        )
    }
}
