package com.jpcode.service

import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.dao.relacionamento.contrato.CandidatoCurtirRepository
import com.jpcode.dao.vaga.contrato.CompetenciasVagaRepository
import com.jpcode.dao.vaga.contrato.VagaRepository
import com.jpcode.dto.vaga.AtualizarVagaDTO
import com.jpcode.dto.vaga.CadastrarVagaDTO
import com.jpcode.dto.vaga.VagaAnonimaDTO
import com.jpcode.dto.vaga.VagaEmpresaDTO
import com.jpcode.exception.referencia.CompetenciaNaoEncontradaException
import com.jpcode.exception.vaga.VagaNaoEncontradaException
import com.jpcode.model.core.Vaga
import com.jpcode.model.referencia.Competencia
import spock.lang.Specification

class VagaServiceTest extends Specification {

    CompetenciaDAO competenciaDAO = Mock()
    CompetenciasVagaRepository competenciasVagaRepository = Mock()
    VagaRepository vagaRepository = Mock()
    CandidatoCurtirRepository candidatoCurtirRepository = Mock()

    VagaService service

    def setup() {
        service = new VagaService(
                competenciaDAO,
                competenciasVagaRepository,
                vagaRepository,
                candidatoCurtirRepository
        )
    }

    def "deve criar vaga"() {
        given:
        CadastrarVagaDTO cadastrarVagaDTO = new CadastrarVagaDTO(
                "Desenvolvedor Java",
                "Desenvolvimento de APIs",
                "São Paulo",
                10L,
                []
        )

        Vaga vagaSalva = new Vaga(
                1L,
                "Desenvolvedor Java",
                "Desenvolvimento de APIs",
                "São Paulo",
                10L
        )

        when:
        service.criarVaga(cadastrarVagaDTO)

        then:
        1 * vagaRepository.salvar(_) >> vagaSalva
        0 * competenciasVagaRepository.salvar(_, _)
    }

    def "deve criar vaga e cadastrar competencias"() {
        given:
        CadastrarVagaDTO cadastrarVagaDTO = new CadastrarVagaDTO(
                "Desenvolvedor Java",
                "Desenvolvimento de APIs",
                "São Paulo",
                10L,
                ["JAVA", "SPRING"]
        )

        Vaga vagaSalva = new Vaga(
                1L,
                "Desenvolvedor Java",
                "Desenvolvimento de APIs",
                "São Paulo",
                10L
        )

        competenciaDAO.buscarIdPorNome("JAVA") >> Optional.of(1L)
        competenciaDAO.buscarIdPorNome("SPRING") >> Optional.of(2L)

        when:
        service.criarVaga(cadastrarVagaDTO)

        then:
        1 * vagaRepository.salvar(_) >> vagaSalva
        1 * competenciasVagaRepository.salvar(1L, 1L)
        1 * competenciasVagaRepository.salvar(1L, 2L)
    }

    def "deve lançar exceção quando competencia não existir"() {
        given:
        CadastrarVagaDTO cadastrarVagaDTO = new CadastrarVagaDTO(
                "Desenvolvedor Java",
                "Desenvolvimento de APIs",
                "São Paulo",
                10L,
                ["JAVA"]
        )

        Vaga vagaSalva = new Vaga(
                1L,
                "Desenvolvedor Java",
                "Desenvolvimento de APIs",
                "São Paulo",
                10L
        )

        competenciaDAO.buscarIdPorNome("JAVA") >> Optional.empty()

        when:
        service.criarVaga(cadastrarVagaDTO)

        then:
        1 * vagaRepository.salvar(_) >> vagaSalva
        thrown(CompetenciaNaoEncontradaException)
        0 * competenciasVagaRepository.salvar(_, _)
    }

    def "deve curtir vaga"() {
        when:
        service.curtir(1L, 5L)

        then:
        1 * candidatoCurtirRepository.salvar(1L, 5L)
    }

    def "deve listar vagas da empresa"() {
        given:
        List<VagaEmpresaDTO> vagas = []

        vagaRepository.buscarVagasEmpresa(1L) >> vagas

        expect:
        service.listarVagas(1L) == vagas
    }

    def "deve buscar vaga pelo id"() {
        given:
        Vaga vaga = new Vaga(
                1L,
                "Desenvolvedor Java",
                "Desenvolvimento de APIs",
                "São Paulo",
                10L
        )

        vagaRepository.buscarPorId(1L) >> Optional.of(vaga)

        expect:
        service.buscarVaga(1L) == vaga
    }

    def "deve lançar exceção quando vaga não for encontrada pelo id"() {
        given:
        vagaRepository.buscarPorId(1L) >> Optional.empty()

        when:
        service.buscarVaga(1L)

        then:
        thrown(VagaNaoEncontradaException)
    }

    def "deve listar vagas curtidas pelo candidato"() {
        given:
        List<VagaAnonimaDTO> vagas = []

        candidatoCurtirRepository.buscarVagasCurtidas(1L) >> vagas

        expect:
        service.listarVagasCurtidas(1L) == vagas
    }

    def "deve buscar todas as vagas"() {
        given:
        List<VagaAnonimaDTO> vagas = []

        vagaRepository.buscarTodasAsVagas() >> vagas

        expect:
        service.buscarTodasAsVagas() == vagas
    }

    def "deve buscar competencias da vaga"() {
        given:
        List<Competencia> competencias = [
                new Competencia(1L, "JAVA"),
                new Competencia(2L, "SPRING")
        ]

        competenciasVagaRepository.buscarPorVaga(1L) >> competencias

        expect:
        service.buscarCompetenciasDeVaga(1L) == competencias
    }

    def "deve deletar vaga"() {
        when:
        service.deletarVaga(1L)

        then:
        1 * vagaRepository.deletar(1L)
    }

    def "deve atualizar vaga"() {
        given:
        AtualizarVagaDTO atualizarVagaDTO = new AtualizarVagaDTO(
                1L,
                "Desenvolvedor Java Senior",
                "Desenvolvimento de APIs REST",
                "São Paulo",
                10L
        )

        when:
        service.atualizarVaga(atualizarVagaDTO)

        then:
        1 * vagaRepository.atualizarDados(_)
    }

    def "deve buscar competencias da vaga em String"() {
        given:
        List<String> competencias = ["JAVA", "SPRING"]

        competenciasVagaRepository.buscarPorVagaString(1L) >> competencias

        expect:
        service.competenciasEmString(1L) == competencias
    }

    def "deve adicionar competencias a vaga"() {
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
        1 * competenciasVagaRepository.atualizar(
                1L,
                [java, spring]
        )
    }
}