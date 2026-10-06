package com.jpcode.service

import com.jpcode.dao.match.contrato.MatchRepository
import com.jpcode.dto.Match.MatchEncontradoDTO
import spock.lang.Specification

class MatchServiceTest extends Specification {

    MatchRepository matchRepository = Mock()

    MatchService service

    def setup() {
        service = new MatchService(matchRepository)
    }

    def "deve salvar match"() {
        when:
        service.salvar(1L, 2L, 3L)

        then:
        1 * matchRepository.salvar(1L, 2L, 3L)
    }

    def "deve buscar matches por empresa"() {
        given:
        List<MatchEncontradoDTO> matches = []

        matchRepository.buscarMatchesPorEmpresa(1L) >> matches

        expect:
        service.verMatchesPorEmpresa(1L) == matches
    }

    def "deve buscar matches por candidato"() {
        given:
        List<MatchEncontradoDTO> matches = []

        matchRepository.buscarMatchesPorCandidato(1L) >> matches

        expect:
        service.verMatchesPorCandidato(1L) == matches
    }
}