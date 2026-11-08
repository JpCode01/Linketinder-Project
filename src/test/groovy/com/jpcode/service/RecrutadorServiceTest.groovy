package com.jpcode.service

import com.jpcode.dao.contratos.*
import com.jpcode.dto.recrutador.CadastrarRecrutadorDTO
import com.jpcode.model.core.*
import com.jpcode.model.referencia.AreaRecrutamento
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Specification
import java.time.LocalDate

class RecrutadorServiceTest extends Specification {
    def 'login verifica hash do recrutador ativo sem fallback'() {
        when:
        def conta = service.logar('rh@example.invalid', 'segredo')

        then:
        1 * recrutadores.buscarAtivoPorEmail('rh@example.invalid') >> Optional.of(new Recrutador(id: 11L, senha: 'hash-fixture'))
        1 * senhas.verificar('segredo', 'hash-fixture') >> true
        conta.id == 11L
    }

    def 'login rejeita senha incorreta hash legado conta ausente ou inativa'() {
        given:
        recrutadores.buscarAtivoPorEmail(_) >> conta
        senhas.verificar(_, _) >> false

        when:
        service.logar('rh@example.invalid', 'segredo')

        then:
        def erro = thrown(SecurityException)
        erro.message == 'Credenciais inválidas.'

        where:
        conta << [Optional.empty(), Optional.of(new Recrutador(ativo: false)),
                  Optional.of(new Recrutador(senha: 'hash-fixture')), Optional.of(new Recrutador(senha: 'segredo'))]
    }
    RecrutadorRepository recrutadores = Mock()
    AreaRecrutamentoRepository areas = Mock()
    UsuarioRepository empresas = Mock()
    SenhaService senhas = Mock()
    RecrutadorService service = new RecrutadorService(recrutadores, areas, empresas, senhas)
    AreaRecrutamento area = new AreaRecrutamento(3L, 'Recursos Humanos')

    def 'cadastra ativo com empresa operante e hash antes de persistir sem retornar credenciais'() {
        given:
        def dados = dados()

        when:
        def resultado = service.cadastrarRecrutador(7L, dados)
        def json = new JsonSlurper().parseText(JsonOutput.toJson(resultado))

        then:
        1 * empresas.buscarPorId(7L) >> Optional.of(new Empresa(7L, 'Empresa', 'empresa@example.invalid', 'hash', 'cnpj', 1L, 1L, 'cep', 'descricao', true))
        1 * areas.buscarPorId(3L) >> Optional.of(area)
        1 * senhas.derivar('segredo') >> 'hash-fixture'
        1 * recrutadores.salvar({ Recrutador r ->
            r.ativo && r.idEmpresa == 7L && r.idAreaRecrutamento == 3L &&
                    r.senha == 'hash-fixture' && r.email == dados.email && r.cpf == dados.cpf
        }) >> { Recrutador r -> r.id = 11L; r }
        resultado.id == 11L
        json.keySet() == ['id','nome','cpf','email','dataNascimento','descricao','idEmpresa','areaRecrutamento'] as Set
        !JsonOutput.toJson(resultado).contains('hash-fixture')
    }

    def 'rejeita empresa ausente ou inativa antes de derivar e persistir'() {
        when:
        service.cadastrarRecrutador(7L, dados())

        then:
        1 * empresas.buscarPorId(7L) >> empresa
        thrown(IllegalArgumentException)
        0 * areas._
        0 * senhas._
        0 * recrutadores._

        where:
        empresa << [Optional.empty(), Optional.of(new Empresa(7L, 'Empresa', 'empresa@example.invalid', 'hash', 'cnpj', 1L, 1L, 'cep', 'descricao', false))]
    }

    def 'rejeita area ausente ou nao informada'() {
        given:
        def dto = dados()
        dto.idAreaRecrutamento = idArea
        empresas.buscarPorId(7L) >> Optional.of(new Empresa(7L, 'Empresa', 'empresa@example.invalid', 'hash', 'cnpj', 1L, 1L, 'cep', 'descricao', true))
        areas.buscarPorId(_) >> Optional.empty()

        when:
        service.cadastrarRecrutador(7L, dto)

        then:
        thrown(IllegalArgumentException)
        0 * senhas._
        0 * recrutadores._

        where:
        idArea << [3L, null]
    }

    def 'consulta propria retorna somente DTO e consulta de outro titular nao chega ao repositorio'() {
        given:
        recrutadores.buscarPorId(11L) >> Optional.of(new Recrutador(id: 11L, idEmpresa: 7L,
                idAreaRecrutamento: 3L, senha: 'hash-fixture'))
        areas.buscarPorId(3L) >> Optional.of(area)

        expect:
        !JsonOutput.toJson(service.buscarRecrutador(11L, 11L)).contains('hash-fixture')

        when:
        service.buscarRecrutador(12L, 11L)

        then:
        thrown(IllegalArgumentException)
        0 * recrutadores.buscarPorId(_)
    }

    def 'consulta rejeita conta ausente ou inativa'() {
        given:
        recrutadores.buscarPorId(11L) >> conta

        when:
        service.buscarRecrutador(11L, 11L)

        then:
        thrown(RuntimeException)
        0 * areas._

        where:
        conta << [Optional.empty(), Optional.of(new Recrutador(id: 11L, ativo: false))]
    }

    def 'le areas do repositorio sem catalogo fixo'() {
        when:
        def resultado = service.buscarAreas()

        then:
        1 * areas.buscar() >> [area]
        resultado == [area]
    }

    private static CadastrarRecrutadorDTO dados() {
        new CadastrarRecrutadorDTO(nome: 'Nome', cpf: 'cpf', email: 'corporativo@example.invalid',
                dataNascimento: LocalDate.of(1990, 1, 1), descricao: 'Descricao', senha: 'segredo', idAreaRecrutamento: 3L)
    }
}
