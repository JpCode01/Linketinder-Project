package com.jpcode.service

import com.jpcode.dao.candidato.CandidatoDAO
import com.jpcode.dao.candidato.CompetenciasCandidatoDAO
import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.dao.referencia.EstadoDAO
import com.jpcode.dao.referencia.PaisDAO
import com.jpcode.model.core.Candidato
import com.jpcode.model.referencia.Competencia
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

class CandidatoServiceTest extends Specification {

    PaisDAO paisDAO = Mock()
    EstadoDAO estadoDAO = Mock()
    CompetenciaDAO competenciaDAO = Mock()
    CompetenciasCandidatoDAO competenciasCandidatoDAO = Mock()
    CandidatoDAO candidatoDAO = Mock()

    CandidatoService service

    def setup() {
        service = new CandidatoService(
                paisDAO,
                estadoDAO,
                competenciaDAO,
                competenciasCandidatoDAO,
                candidatoDAO
        )
    }

    def "deve buscar candidato pelo id"() {
        given:
        Candidato candidato = new Candidato(
                "João",
                "Pedro",
                "joao@email.com",
                "123",
                "12345678900",
                LocalDate.of(2000, 10, 10),
                1L,
                25,
                2L,
                "12900000",
                "Desenvolvedor"
        )

        candidatoDAO.buscarPorId(1L) >> candidato

        expect:
        service.buscarCandidato(1L) == candidato
    }

    def "deve desativar candidato"() {
        when:
        service.desativarCandidato(1L)

        then:
        1 * candidatoDAO.desativar(1L)
    }

    @Unroll
    def "não deve realizar login quando email ou senha estiver vazio"() {
        when:
        def resultado = service.logar(email, senha)

        then:
        resultado == null
        0 * candidatoDAO.buscarPorEmailESenha(_, _)

        where:
        email            | senha
        ""               | "123"
        "joao@email.com" | ""
        ""               | ""
    }

    def "deve realizar login"() {
        given:
        Candidato candidato = new Candidato(
                "João",
                "Pedro",
                "joao@email.com",
                "123",
                "12345678900",
                LocalDate.of(2000, 10, 10),
                1L,
                25,
                2L,
                "12900000",
                "Desenvolvedor"
        )

        candidatoDAO.buscarPorEmailESenha("joao@email.com", "123") >> candidato

        expect:
        service.logar("joao@email.com", "123") == candidato
    }

    def "deve cadastrar candidato"() {
        given:
        Candidato candidatoSalvo = new Candidato(
                1L,
                "João",
                "Pedro",
                "joao@email.com",
                "123",
                "12345678900",
                LocalDate.of(2000, 10, 10),
                1L,
                25,
                2L,
                "12900000",
                "Desenvolvedor",
                true
        )

        paisDAO.buscarIdPorNome("BRASIL") >> 1L
        estadoDAO.buscarIdPorSigla("SP") >> 2L
        candidatoDAO.salvar(_) >> candidatoSalvo

        when:
        def resultado = service.cadastrarCandidato(
                "João",
                "Pedro",
                "joao@email.com",
                "123",
                "12345678900",
                LocalDate.of(2000, 10, 10),
                "BRASIL",
                25,
                "SP",
                "12900000",
                "Desenvolvedor",
                []
        )

        then:
        1 * candidatoDAO.salvar(_) >> candidatoSalvo
        resultado == candidatoSalvo
    }

    def "deve cadastrar competencias do candidato"() {
        given:
        Candidato candidatoSalvo = new Candidato(
                1L,
                "João",
                "Pedro",
                "joao@email.com",
                "123",
                "12345678900",
                LocalDate.of(2000, 10, 10),
                1L,
                25,
                2L,
                "12900000",
                "Desenvolvedor",
                true
        )

        paisDAO.buscarIdPorNome("BRASIL") >> 1L
        estadoDAO.buscarIdPorSigla("SP") >> 2L
        candidatoDAO.salvar(_) >> candidatoSalvo

        competenciaDAO.buscarIdPorNomeCompetencia("JAVA") >> 10L
        competenciaDAO.buscarIdPorNomeCompetencia("SPRING") >> 20L

        when:
        service.cadastrarCandidato(
                "João",
                "Pedro",
                "joao@email.com",
                "123",
                "12345678900",
                LocalDate.of(2000, 10, 10),
                "BRASIL",
                25,
                "SP",
                "12900000",
                "Desenvolvedor",
                ["JAVA", "SPRING"]
        )

        then:
        1 * competenciasCandidatoDAO.salvar(1L, 10L)
        1 * competenciasCandidatoDAO.salvar(1L, 20L)
    }

    def "deve desativar candidato pelo id"() {
        when:
        service.desativarCandidato(5L)

        then:
        1 * candidatoDAO.desativar(5L)
    }

    def "deve adicionar competencias ao candidato"() {
        given:
        Competencia java = new Competencia(1L, "JAVA")
        Competencia spring = new Competencia(2L, "SPRING")

        competenciaDAO.buscarPorNome("JAVA") >> java
        competenciaDAO.buscarPorNome("SPRING") >> spring

        when:
        service.adicionarCompetencias(
                1L,
                ["JAVA", "SPRING"]
        )

        then:
        1 * competenciasCandidatoDAO.atualizar(
                1L,
                [java, spring]
        )
    }

    def "deve remover competencia do candidato"() {
        when:
        service.removerCompetencia(1L, 5L)

        then:
        1 * competenciasCandidatoDAO.removerCompetencia(1L, 5L)
    }

    def "deve buscar competencias do candidato"() {
        given:
        List<String> competencias = ["JAVA", "SPRING"]

        competenciasCandidatoDAO.buscarPorCandidatoString(1L) >> competencias

        expect:
        service.competenciasEmString(1L) == competencias
    }
}