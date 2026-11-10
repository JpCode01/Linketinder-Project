package com.jpcode.service

import com.jpcode.dto.candidato.AtualizarCandidatoDTO

import com.jpcode.dao.contratos.CompetenciaRepository
import com.jpcode.dao.contratos.ReferenciaCompetenciaRepository
import com.jpcode.dao.contratos.ReferenciaRepository
import com.jpcode.dao.contratos.UsuarioRepository
import com.jpcode.dto.candidato.CadastrarCandidatoDTO
import com.jpcode.dto.candidato.CandidatoComReferenciasDTO
import com.jpcode.dto.candidato.CandidatoViewDTO
import com.jpcode.dto.candidato.CandidatoViewListDTO
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
        candidatoRepository.buscarAtivoPorEmail(email) >> Optional.empty()

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

        candidato.senha = new SenhaService().derivar("123")

        candidatoRepository.buscarAtivoPorEmail(
                "joao@email.com"
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
        1 * candidatoRepository.salvar({ Candidato novo ->
            novo.ativo && novo.senha != cadastrarCandidatoDTO.senha &&
                    new SenhaService().verificar(cadastrarCandidatoDTO.senha, novo.senha)
        }) >> candidatoSalvo
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

    def 'rejeita login incorreto ou inativo sem revelar credenciais'() {
        given:
        Candidato candidato = candidatoParaAtualizacao()
        candidato.ativo = ativo
        candidato.senha = new SenhaService().derivar('segredo armazenado')
        candidatoRepository.buscarAtivoPorEmail(candidato.email) >> Optional.of(candidato)

        when:
        service.logar(candidato.email, senha)

        then:
        CandidatoLoginException erro = thrown()
        erro.message == 'Credenciais inválidas para candidato.'

        where:
        ativo | senha
        true  | 'incorreta'
        false | 'segredo armazenado'
    }

    def 'atualiza candidato com hash novo e preserva atividade sem confiar no DTO'() {
        given:
        Candidato atual = candidatoParaAtualizacao()
        candidatoRepository.buscarPorId(1L) >> Optional.of(atual)
        paisRepository.buscarIdPorNome('BRASIL') >> Optional.of(1L)
        estadoRepository.buscarIdPorNome('SP') >> Optional.of(2L)
        AtualizarCandidatoDTO dto = atualizacao(false)

        when:
        service.atualizarCandidato(dto)

        then:
        1 * candidatoRepository.atualizarDados({ Candidato novo ->
            novo.ativo && novo.senha != dto.senha &&
                    new SenhaService().verificar(dto.senha, novo.senha)
        })
    }

    def 'nao atualiza candidato ausente ou inativo mesmo com DTO ativo'() {
        given:
        Candidato atual = candidatoParaAtualizacao()
        atual.ativo = false
        candidatoRepository.buscarPorId(1L) >> (ausente ? Optional.empty() : Optional.of(atual))

        when:
        service.atualizarCandidato(atualizacao(true))

        then:
        thrown(tipo)
        0 * candidatoRepository.atualizarDados(_)
        0 * paisRepository._

        where:
        ausente | tipo
        true    | CandidatoNaoEncontradoPorIdException
        false   | IllegalStateException
    }

    private static Candidato candidatoParaAtualizacao() {
        new Candidato(1L, 'Nome', 'Sobrenome', 'candidato@example.invalid', 'antiga',
                'cpf', LocalDate.of(2000, 1, 1), 1L, 26, 2L, '00000000', 'Descricao', true)
    }

    private static AtualizarCandidatoDTO atualizacao(boolean ativo) {
        new AtualizarCandidatoDTO(1L, 'Nome', 'Sobrenome', 'candidato@example.invalid', 'nova senha',
                'cpf', LocalDate.of(2000, 1, 1), 'BRASIL', 26, 'SP', '00000000', 'Descricao', ativo)
    }
}
