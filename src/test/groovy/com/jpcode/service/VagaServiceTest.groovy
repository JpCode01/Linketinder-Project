package com.jpcode.service

import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.dao.relacionamento.CandidatoCurtirDAO
import com.jpcode.dao.vaga.CompetenciasVagaDAO
import com.jpcode.dao.vaga.VagaDAO
import com.jpcode.dto.vaga.AtualizarVagaDTO
import com.jpcode.dto.vaga.CadastrarVagaDTO
import com.jpcode.dto.vaga.VagaAnonimaDTO
import com.jpcode.dto.vaga.VagaEmpresaDTO
import com.jpcode.model.core.Vaga
import com.jpcode.model.referencia.Competencia
import spock.lang.Specification
import com.jpcode.exception.referencia.CompetenciaNaoEncontradaException
import com.jpcode.exception.referencia.VagaNaoEncontradaException

class VagaServiceTest extends Specification {

    CompetenciaDAO competenciaDAO = Mock()
    CompetenciasVagaDAO competenciasVagaDAO = Mock()
    VagaDAO vagaDAO = Mock()
    CandidatoCurtirDAO candidatoCurtirDAO = Mock()

    VagaService service

    def setup() {
        service = new VagaService(
                competenciaDAO,
                competenciasVagaDAO,
                vagaDAO,
                candidatoCurtirDAO
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
        1 * vagaDAO.salvar(_) >> vagaSalva
        0 * competenciasVagaDAO.salvar(_, _)
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

        competenciaDAO.buscarIdPorNomeCompetencia("JAVA") >> Optional.of(1L)
        competenciaDAO.buscarIdPorNomeCompetencia("SPRING") >> Optional.of(2L)

        when:
        service.criarVaga(cadastrarVagaDTO)

        then:
        1 * vagaDAO.salvar(_) >> vagaSalva
        1 * competenciasVagaDAO.salvar(1L, 1L)
        1 * competenciasVagaDAO.salvar(1L, 2L)
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

        competenciaDAO.buscarIdPorNomeCompetencia("JAVA") >> Optional.empty()

        when:
        service.criarVaga(cadastrarVagaDTO)

        then:
        1 * vagaDAO.salvar(_) >> vagaSalva
        thrown(CompetenciaNaoEncontradaException)
        0 * competenciasVagaDAO.salvar(_, _)
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

        competenciaDAO.buscarIdPorNomeCompetencia("JAVA") >> Optional.empty()

        when:
        service.criarVaga(cadastrarVagaDTO)

        then:
        1 * vagaDAO.salvar(_) >> vagaSalva
        thrown(CompetenciaNaoEncontradaException)
        0 * competenciasVagaDAO.salvar(_, _)
    }

    def "deve curtir vaga"() {
        when:
        service.curtir(1L, 5L)

        then:
        1 * candidatoCurtirDAO.salvar(1L, 5L)
    }

    def "deve listar vagas da empresa"() {
        given:
        List<VagaEmpresaDTO> vagas = []

        vagaDAO.buscarVagasEmpresa(1L) >> vagas

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

        vagaDAO.buscarPorId(1L) >> Optional.of(vaga)

        expect:
        service.buscarVaga(1L) == vaga
    }

    def "deve lançar exceção quando vaga não for encontrada pelo id"() {
        given:
        vagaDAO.buscarPorId(1L) >> Optional.empty()

        when:
        service.buscarVaga(1L)

        then:
        thrown(VagaNaoEncontradaException)
    }

    def "deve listar vagas curtidas pelo candidato"() {
        given:
        List<VagaAnonimaDTO> vagas = []

        candidatoCurtirDAO.buscarVagasCurtidas(1L) >> vagas

        expect:
        service.listarVagasCurtidas(1L) == vagas
    }

    def "deve buscar todas as vagas"() {
        given:
        List<VagaAnonimaDTO> vagas = []

        vagaDAO.buscarTodasAsVagas() >> vagas

        expect:
        service.buscarTodasAsVagas() == vagas
    }

    def "deve buscar competencias da vaga"() {
        given:
        List<Competencia> competencias = [
                new Competencia(1L, "JAVA"),
                new Competencia(2L, "SPRING")
        ]

        competenciasVagaDAO.buscarPorVaga(1L) >> competencias

        expect:
        service.buscarCompetenciasDeVaga(1L) == competencias
    }

    def "deve deletar vaga"() {
        when:
        service.deletarVaga(1L)

        then:
        1 * vagaDAO.deletar(1L)
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
        1 * vagaDAO.atualizarDados(_)
    }

    def "deve buscar competencias da vaga em String"() {
        given:
        List<String> competencias = ["JAVA", "SPRING"]

        competenciasVagaDAO.buscarPorVagaString(1L) >> competencias

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
        1 * competenciasVagaDAO.atualizar(
                1L,
                [java, spring]
        )
    }
}