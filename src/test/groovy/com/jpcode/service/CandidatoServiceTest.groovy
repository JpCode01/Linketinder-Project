package com.jpcode.service

import com.jpcode.dao.contratos.CompetenciaRepository
import com.jpcode.dao.contratos.ReferenciaCompetenciaRepository
import com.jpcode.dao.contratos.ReferenciaRepository
import com.jpcode.dao.contratos.UsuarioRepository
import com.jpcode.dto.candidato.CadastrarCandidatoDTO
import com.jpcode.dto.candidato.CandidatoComReferenciasDTO
import com.jpcode.dto.candidato.CandidatoViewDTO
import com.jpcode.dto.candidato.CandidatoViewListDTO
import com.jpcode.exception.LoginException
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
    ReferenciaRepository<Competencia> competenciaRepository = Mock()

    ReferenciaCompetenciaRepository referenciaCompetenciaRepository = Mock()

    CompetenciaRepository competenciasCandidatoRepository = Mock()

    UsuarioRepository<Candidato, CandidatoComReferenciasDTO, CandidatoViewListDTO> candidatoRepository = Mock()

    CandidatoService service

    def setup() {
        service = new CandidatoService(
                paisRepository,
                estadoRepository,
                competenciaRepository,
                referenciaCompetenciaRepository,
                competenciasCandidatoRepository,
                candidatoRepository
        )
    }

    def "deve buscar candidato pelo id"() {
        given:
        Candidato candidato = new Candidato(
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

        Estado estado = new Estado(2L, "SP")
        Pais pais = new Pais(1L, "BRASIL")

        CandidatoComReferenciasDTO resultado =
                new CandidatoComReferenciasDTO(candidato, estado, pais)

        Competencia java = new Competencia(10L, "JAVA")
        Competencia spring = new Competencia(20L, "SPRING")

        candidatoRepository.buscarPorIdComReferencias(1L) >>
                Optional.of(resultado)

        competenciasCandidatoRepository.buscarPorId(1L) >>
                [java, spring]

        when:
        CandidatoViewDTO candidatoEncontrado = service.buscarCandidato(1L)

        then:
        candidatoEncontrado.id == 1L
        candidatoEncontrado.nome == "João"
        candidatoEncontrado.sobrenome == "Pedro"
        candidatoEncontrado.email == "joao@email.com"
        candidatoEncontrado.estado == estado
        candidatoEncontrado.pais == pais
        candidatoEncontrado.cep == "12900000"
        candidatoEncontrado.descricao == "Desenvolvedor"
        candidatoEncontrado.idade == 25
        candidatoEncontrado.dataNascimento == LocalDate.of(2000, 10, 10)
        candidatoEncontrado.competencias == [java, spring]
    }

    def "deve lançar exceção quando candidato não for encontrado pelo id"() {
        given:
        candidatoRepository.buscarPorIdComReferencias(1L) >>
                Optional.empty()

        when:
        service.buscarCandidato(1L)

        then:
        thrown(CandidatoNaoEncontradoPorIdException)
    }

    def "deve desativar candidato"() {
        when:
        service.desativarCandidato(1L)

        then:
        1 * candidatoRepository.desativar(1L)
    }

    @Unroll
    def "deve lançar exceção quando login não for encontrado para email '#email' e senha informados"() {
        given:
        candidatoRepository.buscarPorEmailESenha(email, senha) >> Optional.empty()

        when:
        service.logar(email, senha)

        then:
        thrown(LoginException)

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

        candidatoRepository.buscarPorEmailESenha(
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

        competenciaRepository.buscarIdPorNome("JAVA") >> Optional.of(10L)
        competenciaRepository.buscarIdPorNome("SPRING") >> Optional.of(20L)

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

        referenciaCompetenciaRepository.buscarPorNome("JAVA") >> java
        referenciaCompetenciaRepository.buscarPorNome("SPRING") >> spring

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

        competenciasCandidatoRepository.buscarEmString(1L) >> competencias

        expect:
        service.competenciasEmString(1L) == competencias
    }
}