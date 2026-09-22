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
import com.jpcode.model.core.Empresa
import com.jpcode.validation.CompetenciaValidation
import spock.lang.Specification
import spock.lang.Unroll

class EmpresaServiceTest extends Specification {

    CompetenciaValidation validation = Mock()
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
                validation,
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

        paisDAO.buscarIdPorNome("BRASIL") >> 1L
        estadoDAO.buscarIdPorSigla("SP") >> 2L

        when:
        def resultado = service.cadastrarEmpresa(
                "Empresa Teste",
                "empresa@email.com",
                "123",
                "12345678000100",
                "BRASIL",
                "SP",
                "12900000",
                "Empresa de tecnologia"
        )

        then:
        1 * empresaDAO.salvar(_) >> empresaSalva
        resultado == empresaSalva
    }

    @Unroll
    def "não deve cadastrar empresa quando país ou estado não existir"() {
        given:
        paisDAO.buscarIdPorNome(pais) >> idPais
        estadoDAO.buscarIdPorSigla(estado) >> idEstado

        when:
        def resultado = service.cadastrarEmpresa(
                "Empresa Teste",
                "empresa@email.com",
                "123",
                "12345678000100",
                pais,
                estado,
                "12900000",
                "Empresa de tecnologia"
        )

        then:
        resultado == null
        0 * empresaDAO.salvar(_)

        where:
        pais     | estado | idPais | idEstado
        "BRASIL" | "SP"   | null   | 2L
        "BRASIL" | "SP"   | 1L    | null
        "BRASIL" | "SP"   | null   | null
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
        ) >> empresa

        expect:
        service.logar("empresa@email.com", "123") == empresa
    }

    @Unroll
    def "não deve realizar login quando email ou senha estiver vazio"() {
        when:
        def resultado = service.logar(email, senha)

        then:
        resultado == null
        0 * empresaDAO.buscarPorEmailESenha(_, _)

        where:
        email               | senha
        ""                  | "123"
        "empresa@email.com" | ""
        ""                  | ""
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
        paisDAO.buscarIdPorNome("BRASIL") >> 1L
        estadoDAO.buscarIdPorSigla("SP") >> 2L

        when:
        service.atualizarEmpresa(
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