package com.jpcode.api.servlet

import com.jpcode.api.SessaoHttp
import com.jpcode.controller.candidato.CandidatoController
import com.jpcode.controller.empresa.EmpresaController
import com.jpcode.controller.recrutador.RecrutadorController
import com.jpcode.exception.candidato.CandidatoLoginException
import com.jpcode.exception.empresa.EmpresaLoginException
import com.jpcode.model.core.*
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import jakarta.servlet.http.*
import spock.lang.Specification

class LoginServletTest extends Specification {
    CandidatoController candidatos = Mock()
    EmpresaController empresas = Mock()
    RecrutadorController recrutadores = Mock()
    SessaoHttp contexto = new SessaoHttp(candidatos, empresas, recrutadores)
    LoginServlet servlet = new LoginServlet(candidatos, empresas, recrutadores, contexto)
    HttpServletRequest request = Mock()
    HttpServletResponse response = Mock()
    HttpSession anterior = Mock()
    HttpSession nova = Mock()
    StringWriter saida = new StringWriter()
    Map atributos = [:]
    int status
    String tipoAtual = 'candidato'
    String origemAtual = 'http://127.0.0.1:5500'
    String formatoAtual = 'application/json; charset=UTF-8'
    String corpo = JsonOutput.toJson([email: 'conta@example.invalid', senha: 'segredo'])

    def setup() {
        request.getServletPath() >> { '/login/' + tipoAtual }
        request.getHeader('Origin') >> { origemAtual }
        request.getContentType() >> { formatoAtual }
        request.getReader() >> { new BufferedReader(new StringReader(corpo)) }
        request.getSession(true) >> nova
        nova.setAttribute(_, _) >> { String chave, Object valor -> atributos[chave] = valor }
        nova.getAttribute(_) >> { String chave -> atributos[chave] }
        response.getWriter() >> new PrintWriter(saida)
        response.setStatus(_) >> { int valor -> status = valor }
    }

    def 'login dos tres perfis renova sessao e retorna somente id e tipo'() {
        given:
        tipoAtual = tipo
        def controller = [candidato: candidatos, empresa: empresas, recrutador: recrutadores][tipo]
        def conta = conta(tipo)
        conta.id = 17L
        conta.ativo = true
        conta.senha = 'hash-salt-fixture'

        when:
        servlet.doPost(request, response)

        then:
        1 * request.getSession(false) >> anterior
        1 * anterior.invalidate()
        1 * request.getSession(false) >> null
        1 * controller.logar('conta@example.invalid', 'segredo') >> conta
        status == 200
        new JsonSlurper().parseText(saida.toString()) == [id: 17, tipo: tipo]
        atributos.values().first() == [id: 17L, tipo: tipo]
        !saida.toString().contains('segredo')
        !saida.toString().contains('hash-salt-fixture')

        where:
        tipo << ['candidato', 'empresa', 'recrutador']
    }

    def 'credenciais invalidas nao criam sessao e eliminam contexto anterior'() {
        given:
        tipoAtual = tipo
        def controller = [candidato: candidatos, empresa: empresas, recrutador: recrutadores][tipo]

        when:
        servlet.doPost(request, response)

        then:
        1 * request.getSession(false) >> anterior
        1 * anterior.invalidate()
        1 * controller.logar(_, _) >> { throw erro }
        0 * request.getSession(true)
        status == 401
        new JsonSlurper().parseText(saida.toString()) == [erro: 'Credenciais inválidas.']
        atributos.isEmpty()

        where:
        tipo         | erro
        'candidato'  | new CandidatoLoginException()
        'empresa'    | new EmpresaLoginException()
        'recrutador' | new SecurityException('detalhe privado')
    }

    def 'conta inativa nunca cria contexto mesmo se devolvida pelo controller'() {
        given:
        tipoAtual = tipo
        def controller = [candidato: candidatos, empresa: empresas, recrutador: recrutadores][tipo]
        def conta = conta(tipo)
        conta.id = 17L
        conta.ativo = false

        when:
        servlet.doPost(request, response)

        then:
        1 * controller.logar(_, _) >> conta
        0 * request.getSession(true)
        status == 401

        where:
        tipo << ['candidato', 'empresa', 'recrutador']
    }

    def 'rejeita JSON invalido e campos que tentem escolher identidade'() {
        given:
        corpo = json

        when:
        servlet.doPost(request, response)

        then:
        status == 400
        0 * candidatos._
        0 * empresas._
        0 * recrutadores._
        0 * request.getSession(true)

        where:
        json << ['{', '[]', 'null', '{}', '{"email":4,"senha":"segredo"}',
                 '{"email":"a","senha":""}', '{"email":"a","senha":[]}',
                 '{"email":"a","senha":"b","id":999}',
                 '{"email":"a","senha":"b","tipo":"empresa"}']
    }

    def 'rejeita origem ou content type nao autorizado'() {
        given:
        origemAtual = origem
        formatoAtual = formato

        when:
        servlet.doPost(request, response)

        then:
        status == esperado
        0 * candidatos._
        0 * request.getSession(true)

        where:
        origem                         | formato            | esperado
        'https://outro.example.invalid' | 'application/json' | 403
        null                           | 'application/json' | 403
        'http://127.0.0.1:5500'        | 'text/plain'       | 400
    }

    def 'erro inesperado nao revela detalhes nem cria sessao'() {
        when:
        servlet.doPost(request, response)

        then:
        1 * candidatos.logar(_, _) >> { throw new RuntimeException('SQL senha hash salt') }
        0 * request.getSession(true)
        status == 500
        new JsonSlurper().parseText(saida.toString()) == [erro: 'Erro interno ao autenticar.']
    }

    def 'recupera somente ator do servidor e revalida atividade em cada operacao'() {
        given:
        contexto.iniciar(request, 17L, tipo)
        request.getSession(false) >> nova
        def controller = [candidato: candidatos, empresa: empresas, recrutador: recrutadores][tipo]

        when:
        def ator = contexto.recuperarAtor(request)

        then:
        1 * controller.estaAtivo(17L) >> true
        ator == [id: 17L, tipo: tipo]
        0 * request.getParameter(_)
        0 * request.getHeader(_)

        when:
        contexto.recuperarAtor(request)

        then:
        1 * controller.estaAtivo(17L) >> false
        1 * nova.invalidate()
        thrown(SecurityException)

        where:
        tipo << ['candidato', 'empresa', 'recrutador']
    }

    def 'ausencia ou contexto invalido nao autentica nem cria sessao'() {
        given:
        request.getSession(false) >> (temSessao ? anterior : null)
        anterior.getAttribute(_) >> ator

        when:
        contexto.recuperarAtor(request)

        then:
        thrown(SecurityException)
        0 * request.getSession(true)
        0 * candidatos._
        0 * empresas._
        0 * recrutadores._

        where:
        temSessao | ator
        false     | null
        true      | null
        true      | [id: 17L, tipo: 'admin']
        true      | [id: '17', tipo: 'empresa']
        true      | [id: -1L, tipo: 'candidato']
    }

    private static def conta(String tipo) {
        switch (tipo) {
            case 'candidato': return new Candidato(17L, 'Nome', 'Sobrenome', 'conta@example.invalid',
                    'hash-salt-fixture', 'cpf', java.time.LocalDate.of(1990, 1, 1), 1L, 36, 1L, 'cep', 'Descricao', true)
            case 'empresa': return new Empresa(17L, 'Empresa', 'conta@example.invalid',
                    'hash-salt-fixture', 'cnpj', 1L, 1L, 'cep', 'Descricao', true)
            case 'recrutador': return new Recrutador(id: 17L, senha: 'hash-salt-fixture')
        }
    }
}
