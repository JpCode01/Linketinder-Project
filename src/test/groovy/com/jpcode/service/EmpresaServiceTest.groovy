package com.jpcode.service

import com.jpcode.dao.contratos.*
import com.jpcode.dto.candidato.CandidatoAnonimoDTO
import com.jpcode.dto.competencia.RemoverCompetenciaDTO
import com.jpcode.dto.empresa.AtualizarEmpresaDTO
import com.jpcode.dto.empresa.CadastrarEmpresaDTO
import com.jpcode.dto.empresa.EmpresaComReferenciasDTO
import com.jpcode.dto.empresa.EmpresaViewListDTO
import com.jpcode.exception.LoginException
import com.jpcode.exception.empresa.EmpresaNaoEncontradaPorIdException
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

    UsuarioRepository<Empresa, EmpresaComReferenciasDTO, EmpresaViewListDTO> empresaRepository = Mock()

    RetribuicaoRepository empresaCurtirRepository = Mock()
    CompetenciaRepository competenciasVagaRepository = Mock()

    EmpresaService service

    def setup() {
        service = new EmpresaService(
                paisRepository,
                estadoRepository,
                empresaRepository,
                empresaCurtirRepository,
                competenciasVagaRepository
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
        1 * empresaRepository.salvar({ Empresa nova ->
            nova.ativo && nova.senha != cadastrarEmpresaDTO.senha &&
                    new SenhaService().verificar(cadastrarEmpresaDTO.senha, nova.senha)
        }) >> empresaSalva
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

        empresa.senha = new SenhaService().derivar("123")

        empresaRepository.buscarAtivoPorEmail(
                "empresa@email.com"
        ) >> Optional.of(empresa)

        expect:
        service.logar("empresa@email.com", "123") == empresa
    }

    def "deve lançar exceção quando login da empresa não for encontrado"() {
        given:
        empresaRepository.buscarAtivoPorEmail(
                "empresa@email.com"
        ) >> Optional.empty()

        when:
        service.logar("empresa@email.com", "123")

        then:
        thrown(LoginException)
    }

    def "consulta sem ator nao acessa interessados"() {
        when:
        service.buscarCandidatosQueCurtiram(1L)
        then:
        thrown(UnsupportedOperationException)
        0 * empresaCurtirRepository._
    }

    def "deve buscar candidatos curtidos pela empresa"() {
        given:
        List<CandidatoAnonimoDTO> candidatos = []

        empresaCurtirRepository.buscarCurtidas(1L) >> candidatos

        expect:
        service.buscarCandidatosCurtidos(1L) == candidatos
    }

    def "deve desativar empresa"() {
        when:
        service.desativarEmpresa(1L)

        then:
        1 * empresaRepository.desativar(1L)
    }

    def "deve atualizar empresa"() {
        given:
        empresaRepository.buscarPorId(1L) >> Optional.of(empresaParaAtualizacao())
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
                false
        )

        paisRepository.buscarIdPorNome("BRASIL") >> Optional.of(1L)
        estadoRepository.buscarIdPorNome("SP") >> Optional.of(2L)

        when:
        service.atualizarEmpresa(atualizarEmpresaDTO)

        then:
        1 * empresaRepository.atualizarDados({ Empresa nova ->
            nova.ativo && nova.senha != atualizarEmpresaDTO.senha &&
                    new SenhaService().verificar(atualizarEmpresaDTO.senha, nova.senha)
        })
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

        competenciasVagaRepository.buscarPorId(1L) >> competencias

        expect:
        List<RemoverCompetenciaDTO> resultado = service.listaParaRemover(1L)

        resultado.size() == 2
        resultado[0].id == 1L
        resultado[0].nome == "JAVA"
        resultado[1].id == 2L
        resultado[1].nome == "SPRING"
    }

    def 'rejeita login incorreto ou inativo sem revelar credenciais'() {
        given:
        Empresa empresa = empresaParaAtualizacao()
        empresa.ativo = ativo
        empresa.senha = new SenhaService().derivar('segredo armazenado')
        empresaRepository.buscarAtivoPorEmail(empresa.email) >> Optional.of(empresa)

        when:
        service.logar(empresa.email, senha)

        then:
        LoginException erro = thrown()
        erro.message == 'Credenciais inválidas.'

        where:
        ativo | senha
        true  | 'incorreta'
        false | 'segredo armazenado'
    }

    def 'nao atualiza empresa ausente ou inativa mesmo com DTO ativo'() {
        given:
        Empresa atual = empresaParaAtualizacao()
        atual.ativo = false
        empresaRepository.buscarPorId(1L) >> (ausente ? Optional.empty() : Optional.of(atual))

        when:
        service.atualizarEmpresa(new AtualizarEmpresaDTO(1L, 'Nome', atual.email, 'nova senha',
                'cnpj', 'BRASIL', 'SP', '00000000', 'Descricao', true))

        then:
        thrown(tipo)
        0 * empresaRepository.atualizarDados(_)
        0 * paisRepository._

        where:
        ausente | tipo
        true    | EmpresaNaoEncontradaPorIdException
        false   | IllegalStateException
    }

    private static Empresa empresaParaAtualizacao() {
        new Empresa(1L, 'Nome', 'empresa@example.invalid', 'antiga', 'cnpj',
                1L, 2L, '00000000', 'Descricao', true)
    }
}
