package com.jpcode.service

import com.jpcode.dao.contratos.MatchRepository
import com.jpcode.dao.contratos.UsuarioRepository
import com.jpcode.dao.contratos.RecrutadorRepository
import com.jpcode.model.core.Empresa
import com.jpcode.model.core.Candidato
import com.jpcode.model.core.Recrutador
import com.jpcode.dto.Match.MatchEncontradoDTO
import spock.lang.Specification

class MatchServiceTest extends Specification {

    MatchRepository matchRepository = Mock()
    UsuarioRepository candidatos = Mock()
    UsuarioRepository empresas = Mock()
    RecrutadorRepository recrutadores = Mock()

    MatchService service

    def setup() {
        service = new MatchService(matchRepository, candidatos, empresas, recrutadores)
        candidatos.buscarPorId(1L) >> Optional.of(new Candidato(1L, null, null, null, null, null, null, null, 0, null, null, null, true))
        empresas.buscarPorId(1L) >> Optional.of(new Empresa(1L, null, null, null, null, null, null, null, null, true))
        recrutadores.buscarPorId(1L) >> Optional.of(new Recrutador(id: 1L, idEmpresa: 1L, ativo: true))
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

    def 'recrutador consulta somente seu escopo'() {
        when:
        def resultado = service.consultarMatchesRecrutador(1L, 1L)
        then:
        1 * matchRepository.buscarMatchesPorRecrutador(1L) >> []
        resultado == []
        0 * matchRepository.buscarMatchesPorEmpresa(_)
    }

    def 'rejeita ID de outro titular para #tipo'() {
        when:
        service."consultarMatches${tipo}"(1L, 2L)
        then:
        thrown(SecurityException)
        0 * matchRepository._
        where:
        tipo << ['Empresa', 'Candidato', 'Recrutador']
    }

    def 'rejeita titular ausente ou inativo para #tipo'() {
        given:
        candidatos.buscarPorId(2L) >> Optional.of(new Candidato(2L, null, null, null, null, null, null, null, 0, null, null, null, false))
        empresas.buscarPorId(2L) >> Optional.of(new Empresa(2L, null, null, null, null, null, null, null, null, false))
        recrutadores.buscarPorId(2L) >> Optional.of(new Recrutador(id: 2L, ativo: false))
        when:
        service."consultarMatches${tipo}"(id, id)
        then:
        thrown(SecurityException)
        0 * matchRepository._
        where:
        tipo         | id
        'Empresa'    | 2L
        'Candidato'  | 2L
        'Recrutador' | 2L
        'Empresa'    | 3L
        'Candidato'  | 3L
        'Recrutador' | 3L
        'Empresa'    | null
        'Candidato'  | 0L
        'Recrutador' | -1L
    }

    def 'recrutador nao consulta matches com empresa inativa'() {
        given:
        recrutadores.buscarPorId(2L) >> Optional.of(new Recrutador(id: 2L, idEmpresa: 2L, ativo: true))
        empresas.buscarPorId(2L) >> Optional.of(new Empresa(2L, null, null, null, null, null, null, null, null, false))
        when:
        service.consultarMatchesRecrutador(2L, 2L)
        then:
        thrown(SecurityException)
        0 * matchRepository._
    }
}
