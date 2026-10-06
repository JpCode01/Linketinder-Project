package com.jpcode.service

import com.jpcode.dao.empresa.contrato.EmpresaAutenticacao
import com.jpcode.dao.empresa.contrato.EmpresaDesativacao
import com.jpcode.dao.empresa.contrato.EmpresaRepository
import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.dao.referencia.contrato.ReferenciaRepository
import com.jpcode.dao.relacionamento.contrato.CandidatoCurtirConsulta
import com.jpcode.dao.relacionamento.contrato.EmpresaCurtirRepository
import com.jpcode.dao.vaga.contrato.CompetenciasVagaRepository
import com.jpcode.dto.candidato.CandidatoAnonimoDTO
import com.jpcode.dto.competencia.RemoverCompetenciaDTO
import com.jpcode.dto.empresa.AtualizarEmpresaDTO
import com.jpcode.dto.empresa.CadastrarEmpresaDTO
import com.jpcode.exception.empresa.EmpresaLoginException
import com.jpcode.exception.referencia.EstadoNaoEncontradoException
import com.jpcode.exception.referencia.PaisNaoEncontradoException
import com.jpcode.model.core.Empresa
import com.jpcode.model.referencia.Competencia
import com.jpcode.model.referencia.Estado
import com.jpcode.model.referencia.Pais
import spock.lang.Specification

class EmpresaServiceTest extends Specification {

    ReferenciaRepository<Pais> paisRepository = Mock()
    ReferenciaRepository<Estado> estadoRepository = Mock()

    EmpresaRepository empresaRepository = Mock()
    EmpresaAutenticacao empresaAutenticacao = Mock()
    EmpresaDesativacao empresaDesativacao = Mock()

    CandidatoCurtirConsulta candidatoCurtirConsulta = Mock()
    EmpresaCurtirRepository empresaCurtirRepository = Mock()
    CompetenciasVagaRepository competenciasVagaRepository = Mock()

    CompetenciaDAO competenciaDAO = Mock()

    EmpresaService service

    def setup() {
        service = new EmpresaService(
                paisRepository,
                estadoRepository,
                empresaRepository,
                empresaAutenticacao,
                empresaDesativacao,
                candidatoCurtirConsulta,
                empresaCurtirRepository,
                competenciasVagaRepository,
                competenciaDAO
        )
    }

    def "deve cadastrar empresa"() {
        given:
        CadastrarEmpresaDTO cadastrarEmpresaDTO = new CadastrarEmpresaDTO(
                "Empresa Teste",
                "empresa@email.com",
                "123",
                "12345678000100",
                "BRASIL",
                "SP",
                "12900000",
                "Empresa de tecnologia"
        )

        Empresa empresaSalva = new Empresa(
                1L,
                "Empresa Teste",
                "empresa@email.com",
                "123",
                "12345678000100",
                1L,
                2L,
                "12900000",
                "Empresa de tecnologia",
                true
        )

        paisRepository.buscarIdPorNome("BRASIL") >> Optional.of(1L)
        estadoRepository.buscarIdPorNome("SP") >> Optional.of(2L)

        when:
        Empresa resultado = service.cadastrarEmpresa(cadastrarEmpresaDTO)

        then:
        1 * empresaRepository.salvar(_) >> empresaSalva
        resultado == empresaSalva
    }

    def "deve lançar exceção quando país não existir"() {
        given:
        CadastrarEmpresaDTO cadastrarEmpresaDTO = new CadastrarEmpresaDTO(
                "Empresa Teste",
                "empresa@email.com",
                "123",
                "12345678000100",
                "BRASIL",
                "SP",
                "12900000",
                "Empresa de tecnologia"
        )

        paisRepository.buscarIdPorNome("BRASIL") >> Optional.empty()

        when:
        service.cadastrarEmpresa(cadastrarEmpresaDTO)

        then:
        thrown(PaisNaoEncontradoException)
        0 * empresaRepository.salvar(_)
    }

    def "deve lançar exceção quando estado não existir"() {
        given:
        CadastrarEmpresaDTO cadastrarEmpresaDTO = new CadastrarEmpresaDTO(
                "Empresa Teste",
                "empresa@email.com",
                "123",
                "12345678000100",
                "BRASIL",
                "SP",
                "12900000",
                "Empresa de tecnologia"
        )

        paisRepository.buscarIdPorNome("BRASIL") >> Optional.of(1L)
        estadoRepository.buscarIdPorNome("SP") >> Optional.empty()

        when:
        service.cadastrarEmpresa(cadastrarEmpresaDTO)

        then:
        thrown(EstadoNaoEncontradoException)
        0 * empresaRepository.salvar(_)
    }

    def "deve realizar login"() {
        given:
        Empresa empresa = new Empresa(
                "Empresa Teste",
                "empresa@email.com",
                "123",
                "12345678000100",
                1L,
                2L,
                "12900000",
                "Empresa de tecnologia"
        )

        empresaAutenticacao.buscarPorEmailESenha(
                "empresa@email.com",
                "123"
        ) >> Optional.of(empresa)

        expect:
        service.logar("empresa@email.com", "123") == empresa
    }

    def "deve lançar exceção quando login da empresa não for encontrado"() {
        given:
        empresaAutenticacao.buscarPorEmailESenha(
                "empresa@email.com",
                "123"
        ) >> Optional.empty()

        when:
        service.logar("empresa@email.com", "123")

        then:
        thrown(EmpresaLoginException)
    }

    def "deve buscar candidatos que curtiram uma vaga"() {
        given:
        List<CandidatoAnonimoDTO> candidatos = []

        candidatoCurtirConsulta.buscarCandidatosQueCurtiram(1L) >> candidatos

        expect:
        service.buscarCandidatosQueCurtiram(1L) == candidatos
    }

    def "deve curtir candidato pelo id"() {
        when:
        service.curtirCandidato(1L, 2L)

        then:
        1 * empresaCurtirRepository.salvar(1L, 2L)
    }

    def "deve buscar candidatos curtidos pela empresa"() {
        given:
        List<CandidatoAnonimoDTO> candidatos = []

        empresaCurtirRepository.buscarCandidatosCurtidos(1L) >> candidatos

        expect:
        service.buscarCandidatosCurtidos(1L) == candidatos
    }

    def "deve desativar empresa"() {
        when:
        service.desativarEmpresa(1L)

        then:
        1 * empresaDesativacao.desativar(1L)
    }

    def "deve atualizar empresa"() {
        given:
        AtualizarEmpresaDTO atualizarEmpresaDTO = new AtualizarEmpresaDTO(
                1L,
                "Empresa Atualizada",
                "empresa@email.com",
                "123",
                "12345678000100",
                "BRASIL",
                "SP",
                "12900000",
                "Nova descricao",
                true
        )

        paisRepository.buscarIdPorNome("BRASIL") >> Optional.of(1L)
        estadoRepository.buscarIdPorNome("SP") >> Optional.of(2L)

        when:
        service.atualizarEmpresa(atualizarEmpresaDTO)

        then:
        1 * empresaRepository.atualizarDados(_)
    }

    def "deve remover competencia da vaga"() {
        when:
        service.removerCompetencia(1L, 5L)

        then:
        1 * competenciasVagaRepository.removerCompetencia(1L, 5L)
    }

    def "deve buscar competencias da vaga para remover"() {
        given:
        List<Competencia> competencias = [
                new Competencia(1L, "JAVA"),
                new Competencia(2L, "SPRING")
        ]

        List<RemoverCompetenciaDTO> resultadoDTO = []

        competenciasVagaRepository.buscarPorVaga(1L) >> competencias
        competenciaDAO.converterCompetenciasParaDTO(competencias) >> resultadoDTO

        expect:
        service.listaParaRemover(1L) == resultadoDTO
    }
}