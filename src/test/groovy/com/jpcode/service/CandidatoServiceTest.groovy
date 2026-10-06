package com.jpcode.service

import com.jpcode.dao.candidato.contrato.CandidatoAutenticacao
import com.jpcode.dao.candidato.contrato.CandidatoDesativacao
import com.jpcode.dao.candidato.contrato.CandidatoRepository
import com.jpcode.dao.candidato.contrato.CompetenciasCandidatoRepository
import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.dao.referencia.contrato.ReferenciaRepository
import com.jpcode.dto.candidato.CadastrarCandidatoDTO
import com.jpcode.exception.candidato.CandidatoLoginException
import com.jpcode.exception.candidato.CandidatoNaoEncontradoPorIdException
import com.jpcode.model.core.Candidato
import com.jpcode.model.referencia.Competencia
import com.jpcode.model.referencia.Estado
import com.jpcode.model.referencia.Pais
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

class CandidatoServiceTest extends Specification {

    ReferenciaRepository<Pais> paisRepository = Mock()
    ReferenciaRepository<Estado> estadoRepository = Mock()
    CompetenciaDAO competenciaDAO = Mock()

    CompetenciasCandidatoRepository competenciasCandidatoRepository = Mock()

    CandidatoRepository candidatoRepository = Mock()
    CandidatoAutenticacao candidatoAutenticacao = Mock()
    CandidatoDesativacao candidatoDesativacao = Mock()

    CandidatoService service

    def setup() {
        service = new CandidatoService(
                paisRepository,
                estadoRepository,
                competenciaDAO,
                competenciasCandidatoRepository,
                candidatoRepository,
                candidatoAutenticacao,
                candidatoDesativacao
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

        candidatoRepository.buscarPorId(1L) >> Optional.of(candidato)

        expect:
        service.buscarCandidato(1L) == candidato
    }

    def "deve lançar exceção quando candidato não for encontrado pelo id"() {
        given:
        candidatoRepository.buscarPorId(1L) >> Optional.empty()

        when:
        service.buscarCandidato(1L)

        then:
        thrown(CandidatoNaoEncontradoPorIdException)
    }

    def "deve desativar candidato"() {
        when:
        service.desativarCandidato(1L)

        then:
        1 * candidatoDesativacao.desativar(1L)
    }

    @Unroll
    def "deve lançar exceção quando login não for encontrado para email '#email' e senha informados"() {
        given:
        candidatoAutenticacao.buscarPorEmailESenha(email, senha) >> Optional.empty()

        when:
        service.logar(email, senha)

        then:
        thrown(CandidatoLoginException)

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

        candidatoAutenticacao.buscarPorEmailESenha(
                "joao@email.com",
                "123"
        ) >> Optional.of(candidato)

        expect:
        service.logar("joao@email.com", "123") == candidato
    }

    def "deve cadastrar o candidato"() {
        given:
        CadastrarCandidatoDTO cadastrarCandidatoDTO = new CadastrarCandidatoDTO(
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

        paisRepository.buscarIdPorNome("BRASIL") >> Optional.of(1L)
        estadoRepository.buscarIdPorNome("SP") >> Optional.of(2L)

        competenciaDAO.buscarIdPorNome("JAVA") >> Optional.of(10L)
        competenciaDAO.buscarIdPorNome("SPRING") >> Optional.of(20L)

        when:
        service.cadastrarCandidato(cadastrarCandidatoDTO)

        then:
        1 * candidatoRepository.salvar(_) >> candidatoSalvo
        1 * competenciasCandidatoRepository.salvar(1L, 10L)
        1 * competenciasCandidatoRepository.salvar(1L, 20L)
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
        1 * competenciasCandidatoRepository.atualizar(
                1L,
                [java, spring]
        )
    }

    def "deve remover competencia do candidato"() {
        when:
        service.removerCompetencia(1L, 5L)

        then:
        1 * competenciasCandidatoRepository.removerCompetencia(1L, 5L)
    }

    def "deve buscar competencias do candidato"() {
        given:
        List<String> competencias = ["JAVA", "SPRING"]

        competenciasCandidatoRepository.buscarPorCandidatoString(1L) >> competencias

        expect:
        service.competenciasEmString(1L) == competencias
    }
}