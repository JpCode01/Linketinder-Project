package com.jpcode.service

import com.jpcode.dao.empresa.EmpresaDAO
import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.dao.referencia.EstadoDAO
import com.jpcode.dao.referencia.PaisDAO
import com.jpcode.dao.relacionamento.CandidatoCurtirDAO
import com.jpcode.dao.relacionamento.EmpresaCurtirDAO
import com.jpcode.dao.vaga.CompetenciasVagaDAO
import com.jpcode.dto.candidato.CandidatoAnonimoDTO
import com.jpcode.dto.competencia.RemoverCompetenciaDTO
import com.jpcode.dto.empresa.AtualizarEmpresaDTO
import com.jpcode.dto.empresa.CadastrarEmpresaDTO
import com.jpcode.model.core.Empresa
import spock.lang.Specification
import com.jpcode.exception.referencia.EstadoNaoEncontradoException
import com.jpcode.exception.referencia.PaisNaoEncontradoException
import com.jpcode.exception.empresa.EmpresaLoginException

class EmpresaServiceTest extends Specification {

    PaisDAO paisDAO = Mock()
    EstadoDAO estadoDAO = Mock()
    EmpresaDAO empresaDAO = Mock()
    CandidatoCurtirDAO candidatoCurtirDAO = Mock()
    EmpresaCurtirDAO empresaCurtirDAO = Mock()
    CompetenciasVagaDAO competenciasVagaDAO = Mock()
    CompetenciaDAO competenciaDAO = Mock()

    EmpresaService service

    def setup() {
        service = new EmpresaService(
                paisDAO,
                estadoDAO,
                empresaDAO,
                candidatoCurtirDAO,
                empresaCurtirDAO,
                competenciasVagaDAO,
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

        paisDAO.buscarIdPorNome("BRASIL") >> Optional.of(1L)
        estadoDAO.buscarIdPorNome("SP") >> Optional.of(2L)

        when:
        Empresa resultado = service.cadastrarEmpresa(cadastrarEmpresaDTO)

        then:
        1 * empresaDAO.salvar(_) >> empresaSalva
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

        paisDAO.buscarIdPorNome("BRASIL") >> Optional.empty()

        when:
        service.cadastrarEmpresa(cadastrarEmpresaDTO)

        then:
        thrown(PaisNaoEncontradoException)
        0 * empresaDAO.salvar(_)
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

        paisDAO.buscarIdPorNome("BRASIL") >> Optional.of(1L)
        estadoDAO.buscarIdPorNome("SP") >> Optional.empty()

        when:
        service.cadastrarEmpresa(cadastrarEmpresaDTO)

        then:
        thrown(EstadoNaoEncontradoException)
        0 * empresaDAO.salvar(_)
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

        empresaDAO.buscarPorEmailESenha(
                "empresa@email.com",
                "123"
        ) >> Optional.of(empresa)

        expect:
        service.logar("empresa@email.com", "123") == empresa
    }

    def "deve lançar exceção quando login da empresa não for encontrado"() {
        given:
        empresaDAO.buscarPorEmailESenha(
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

        candidatoCurtirDAO.buscarCandidatosQueCurtiram(1L) >> candidatos

        expect:
        service.buscarCandidatosQueCurtiram(1L) == candidatos
    }

    def "deve curtir candidato pelo id"() {
        when:
        service.curtirCandidato(1L, 2L)

        then:
        1 * empresaCurtirDAO.salvar(1L, 2L)
    }

    def "deve buscar candidatos curtidos pela empresa"() {
        given:
        List<CandidatoAnonimoDTO> candidatos = []

        empresaCurtirDAO.buscarCandidatosCurtidos(1L) >> candidatos

        expect:
        service.buscarCandidatosCurtidos(1L) == candidatos
    }

    def "deve desativar empresa"() {
        when:
        service.desativarEmpresa(1L)

        then:
        1 * empresaDAO.desativar(1L)
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

        paisDAO.buscarIdPorNome("BRASIL") >> Optional.of(1L)
        estadoDAO.buscarIdPorNome("SP") >> Optional.of(2L)

        when:
        service.atualizarEmpresa(atualizarEmpresaDTO)

        then:
        1 * empresaDAO.atualizarDados(_)
    }

    def "deve remover competencia da vaga"() {
        when:
        service.removerCompetencia(1L, 5L)

        then:
        1 * competenciasVagaDAO.removerCompetencia(1L, 5L)
    }

    def "deve buscar competencias da vaga para remover"() {
        given:
        List<String> competencias = ["JAVA", "SPRING"]
        List<RemoverCompetenciaDTO> resultadoDTO = []

        competenciasVagaDAO.buscarPorVaga(1L) >> competencias
        competenciaDAO.converterCompetenciasParaDTO(competencias) >> resultadoDTO

        expect:
        service.listaParaRemover(1L) == resultadoDTO
    }
}