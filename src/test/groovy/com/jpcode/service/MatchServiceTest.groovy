package com.jpcode.service

import com.jpcode.dao.candidato.CompetenciasCandidatoDAO
import com.jpcode.dao.match.MatchDAO
import com.jpcode.dao.vaga.CompetenciasVagaDAO
import com.jpcode.dto.Match.MatchEncontradoDTO
import spock.lang.Specification

class MatchServiceTest extends Specification {

    CompetenciasCandidatoDAO competenciasCandidatoDAO = Mock()
    CompetenciasVagaDAO competenciasVagaDAO = Mock()

    MatchDAO matchDAO = Mock(
            constructorArgs: [
                    competenciasCandidatoDAO,
                    competenciasVagaDAO
            ]
    )

    MatchService service

    def setup() {
        service = new MatchService(matchDAO)
    }

    def "deve salvar match"() {
        when:
        service.salvar(1L, 2L, 3L)

        then:
        1 * matchDAO.salvar(1L, 2L, 3L)
    }

    def "deve buscar matches por empresa"() {
        given:
        List<MatchEncontradoDTO> matches = []

        matchDAO.buscarMatchesPorEmpresa(1L) >> matches

        expect:
        service.verMatchesPorEmpresa(1L) == matches
    }

    def "deve buscar matches por candidato"() {
        given:
        List<MatchEncontradoDTO> matches = []

        matchDAO.buscarMatchesPorCandidato(1L) >> matches

        expect:
        service.verMatchesPorCandidato(1L) == matches
    }
}