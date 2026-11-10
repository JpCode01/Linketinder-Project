package com.jpcode.api.servlet

import com.jpcode.api.SessaoHttp
import com.jpcode.config.*
import com.jpcode.controller.candidato.CandidatoController
import com.jpcode.controller.empresa.EmpresaController
import com.jpcode.controller.recrutador.RecrutadorController
import com.jpcode.controller.vaga.VagaController
import com.jpcode.controller.match.MatchController
import com.jpcode.controller.referencia.ReferenciaController
import com.jpcode.controller.competencia.CompetenciaController
import com.jpcode.dto.candidato.CandidatoAnonimoDTO
import com.jpcode.dto.vaga.*
import com.jpcode.exception.vaga.VagaNaoEncontradaException
import com.jpcode.model.core.*
import com.jpcode.model.referencia.*
import groovy.json.*
import org.apache.catalina.startup.Tomcat
import spock.lang.Specification

import java.net.http.*
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

class ContratoHttpTest extends Specification {
    CandidatoController candidatos = Mock()
    EmpresaController empresas = Mock()
    RecrutadorController recrutadores = Mock()
    VagaController vagas = Mock()
    MatchController matches = Mock(constructorArgs: [null, null])
    ReferenciaController referencias = Mock()
    CompetenciaController competencias = Mock()
    Tomcat tomcat
    HttpClient cliente
    String base
    String origem = 'http://127.0.0.1:5500'

    def setup() {
        def sessao = new SessaoHttp(candidatos, empresas, recrutadores)
        ControllerConfig config = Stub(constructorArgs: [new ServiceConfig(new DaoConfig())]) {
            getCandidatoController() >> candidatos
            getEmpresaController() >> empresas
            getRecrutadorController() >> recrutadores
            getVagaController() >> vagas
            getMatchController() >> matches
            getReferenciaController() >> referencias
            getCompetenciaController() >> competencias
            getSessaoHttp() >> sessao
        }
        candidatos.estaAtivo(7L) >> true
        empresas.estaAtivo(7L) >> true
        recrutadores.estaAtivo(7L) >> true
        candidatos.logar('c@teste', 'senha-teste') >> new Candidato(7L, 'Nome', 'Sobrenome', 'c@teste', 'hash', 'cpf', null, 1L, 30, 1L, 'cep', 'd', true)
        empresas.logar('e@teste', 'senha-teste') >> new Empresa(7L, 'Empresa', 'e@teste', 'hash', 'cnpj', 1L, 1L, 'cep', 'd', true)
        recrutadores.logar('r@teste', 'senha-teste') >> new Recrutador(id: 7L, ativo: true)
        tomcat = new Tomcat()
        def pasta = Files.createTempDirectory(Path.of('build'), 'http-t11-')
        tomcat.baseDir = pasta.toAbsolutePath().toString()
        tomcat.port = 0
        tomcat.connector
        def contexto = tomcat.addContext('', pasta.toAbsolutePath().toString())
        new ApiServletConfig(tomcat, contexto, config).registrarServlets()
        tomcat.start()
        base = "http://127.0.0.1:${tomcat.connector.localPort}"
        cliente = HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .connectTimeout(Duration.ofSeconds(5)).build()
    }

    def cleanup() {
        cliente?.close()
        tomcat?.stop()
        tomcat?.destroy()
    }

    def 'login em transporte real configura cookie e contexto: #tipo'() {
        when:
        def resposta = login(tipo)
        then:
        resposta.statusCode() == 200
        json(resposta) == [id: 7, tipo: tipo]
        def cookie = resposta.headers().firstValue('set-cookie').orElse('')
        cookie.contains('HttpOnly')
        cookie.contains('SameSite=Lax')
        resposta.headers().firstValue('access-control-allow-origin').orElse('') == origem
        resposta.headers().firstValue('access-control-allow-credentials').orElse('') == 'true'
        where:
        tipo << ['candidato', 'empresa', 'recrutador']
    }

    def 'preflight nao exige sessao e aceita somente origem metodo e cabecalhos previstos'() {
        when:
        def resposta = enviar('OPTIONS', '/curtidas/candidatos', null, origem,
                ['Access-Control-Request-Method': 'POST', 'Access-Control-Request-Headers': 'content-type'])
        then:
        resposta.statusCode() == 204
        resposta.body().empty
        resposta.headers().firstValue('access-control-allow-origin').get() == origem
        when:
        def rejeitada = enviar('OPTIONS', '/curtidas/candidatos', null, 'https://outro.test', ['Access-Control-Request-Method': 'POST'])
        then:
        rejeitada.statusCode() == 403
        !rejeitada.headers().firstValue('access-control-allow-origin').present
        when:
        def cabecalho = enviar('OPTIONS', '/matches/empresa/7', null, origem,
                ['Access-Control-Request-Method': 'GET', 'Access-Control-Request-Headers': 'X-Ator'])
        then:
        cabecalho.statusCode() == 403
    }

    def 'mutacoes rejeitam origem ausente ou divergente e conteudo sem JSON antes do dominio'() {
        expect:
        enviar('POST', '/empresas', '{}', origemRequisicao, headers).statusCode() == status
        where:
        origemRequisicao       | headers                         | status
        null                   | [:]                             | 403
        'http://localhost:5500' | [:]                             | 403
        'https://outro.test'    | [:]                             | 403
        'http://127.0.0.1:5500'| ['Content-Type': 'text/plain']   | 400
    }

    def 'todas as superficies protegidas rejeitam sessao ausente: #metodo #rota'() {
        expect:
        enviar(metodo, rota, metodo in ['POST', 'PUT'] ? '{}' : null).statusCode() == 401
        where:
        metodo | rota
        'GET'  | '/vagas'
        'GET'  | '/vagas/1'
        'GET'  | '/vagas/empresa/7'
        'GET'  | '/vagas/recrutador/7'
        'GET'  | '/recrutadores/7'
        'GET'  | '/curtidas/vagas/candidato/7'
        'GET'  | '/curtidas/candidatos/vaga/1'
        'GET'  | '/matches/candidato/7'
        'GET'  | '/matches/empresa/7'
        'GET'  | '/matches/recrutador/7'
        'POST' | '/vagas'
        'POST' | '/empresas/7/recrutadores'
        'POST' | '/recrutadores/7/vagas'
        'POST' | '/curtidas/vagas'
        'POST' | '/curtidas/candidatos'
        'PUT'  | '/vagas/1'
    }

    def 'referencias publicas preservam seus formatos: #rota'() {
        given:
        referencias.listarPaises() >> [new Pais(1L, 'BRASIL')]
        referencias.listarEstados() >> [new Estado(1L, 'SP')]
        recrutadores.buscarAreas() >> [new AreaRecrutamento(1L, 'RH')]
        competencias.listarCompetenciasEmString() >> ['RH', 'Administração']
        when:
        def resposta = enviar('GET', rota)
        then:
        resposta.statusCode() == 200
        json(resposta) == esperado
        where:
        rota                  | esperado
        '/referencias/paises' | [[id: 1, nome: 'BRASIL']]
        '/referencias/estados'| [[id: 1, sigla: 'SP']]
        '/areas-recrutamento' | [[id: 1, nome: 'RH']]
        '/competencias'       | ['RH', 'Administração']
    }

    def 'descoberta e curtidas mantem lista exata de propriedades anonimas inclusive por ID'() {
        given:
        login('candidato')
        def anonima = new VagaAnonimaDTO(1L, 'Descrição', [new Competencia(1L, 'RH')])
        vagas.buscarTodasAsVagas() >> [anonima]
        vagas.listarVagasCurtidas(7L) >> [anonima]
        vagas.buscarVaga(1L) >> new Vaga(1L, 'Privado', 'Descrição', 'Privado', 10L)
        vagas.buscarCompetenciasDeVaga(1L) >> [new Competencia(1L, 'RH')]
        expect:
        ['/vagas', '/curtidas/vagas/candidato/7'].every { json(enviar('GET', it)) == [[id: 1, descricao: 'Descrição', competencias: [[id: 1, nome: 'RH']]]] }
        json(enviar('GET', '/vagas/1')) == [id: 1, descricao: 'Descrição', competencias: [[id: 1, nome: 'RH']]]
    }

    def 'titular ou perfil alheio nao acessa outro escopo: #tipo #rota'() {
        given:
        login(tipo)
        expect:
        enviar('GET', rota).statusCode() == 403
        where:
        tipo         | rota
        'candidato'  | '/matches/candidato/8'
        'candidato'  | '/matches/empresa/7'
        'candidato'  | '/curtidas/vagas/candidato/8'
        'candidato'  | '/vagas/empresa/7'
        'empresa'    | '/matches/empresa/8'
        'empresa'    | '/vagas/empresa/8'
        'empresa'    | '/vagas'
        'recrutador' | '/recrutadores/8'
        'recrutador' | '/recrutadores'
        'recrutador' | '/vagas/recrutador/8'
        'recrutador' | '/matches/recrutador/8'
        'recrutador' | '/vagas/empresa/7'
    }

    def 'interessados e retribuicao usam ator da sessao e metodos especificos: #tipo'() {
        given:
        login(tipo)
        when:
        def consulta = enviar('GET', '/curtidas/candidatos/vaga/1')
        def resposta = enviar('POST', '/curtidas/candidatos', '{"idCandidato":3,"idVaga":1}')
        then:
        (tipo == 'empresa' ? 1 : 0) * matches.buscarInteressadosEmpresa(7L, 1L) >> [new CandidatoAnonimoDTO(3L, 'Anônimo', [])]
        (tipo == 'recrutador' ? 1 : 0) * matches.buscarInteressadosRecrutador(7L, 1L) >> [new CandidatoAnonimoDTO(3L, 'Anônimo', [])]
        (tipo == 'empresa' ? 1 : 0) * matches.retribuirInteresseEmpresa(7L, 3L, 1L)
        (tipo == 'recrutador' ? 1 : 0) * matches.retribuirInteresseRecrutador(7L, 3L, 1L)
        consulta.statusCode() == 200
        json(consulta) == [[id: 3, descricao: 'Anônimo', competencias: []]]
        resposta.statusCode() == 204
        resposta.body().empty
        where:
        tipo << ['empresa', 'recrutador']
    }

    def 'JSON invalido e campos de identidade ou autoria nao chegam a escrita: #rota #corpo'() {
        given:
        login(tipo)
        when:
        def resposta = enviar('POST', rota, corpo)
        then:
        resposta.statusCode() == 400
        json(resposta).keySet() == ['erro'] as Set
        0 * vagas.curtir(_, _)
        0 * vagas.criarVagaEmpresa(_, _)
        0 * vagas.criarVagaRecrutador(_, _)
        0 * matches.retribuirInteresseEmpresa(_, _, _)
        0 * recrutadores.cadastrarRecrutador(_, _)
        where:
        tipo         | rota                         | corpo
        'candidato'  | '/curtidas/vagas'             | '[]'
        'candidato'  | '/curtidas/vagas'             | '{'
        'candidato'  | '/curtidas/vagas'             | '{"idVaga":1,"idCandidato":8}'
        'candidato'  | '/curtidas/vagas'             | '{"idVaga":1.5}'
        'candidato'  | '/curtidas/vagas'             | '{"idVaga":0}'
        'candidato'  | '/curtidas/vagas'             | '{"idVaga":true}'
        'empresa'    | '/curtidas/candidatos'        | '{"idVaga":1,"idCandidato":3,"idEmpresa":8}'
        'empresa'    | '/vagas'                     | '{"nome":"n","descricao":"d","local":"l","competencias":[],"idRecrutador":1}'
        'empresa'    | '/vagas'                     | '{"nome":"n","descricao":"d","local":"l","competencias":[1]}'
        'empresa'    | '/vagas'                     | '{"nome":"n","descricao":"d","local":"l","competencias":[],"idEmpresa":8}'
        'recrutador' | '/recrutadores/7/vagas'       | '{"nome":"n","descricao":"d","local":"l","competencias":[],"idEmpresa":8}'
        'empresa'    | '/empresas/7/recrutadores'    | '{"nome":"n","cpf":"c","email":"e","dataNascimento":"inválida","descricao":"d","senha":"s","idAreaRecrutamento":1}'
    }

    def 'status de dominio e falha inesperada nao vazam detalhes: #status'() {
        given:
        login('empresa')
        matches.retribuirInteresseEmpresa(7L, 3L, 1L) >> { throw falha }
        when:
        def resposta = enviar('POST', '/curtidas/candidatos', '{"idCandidato":3,"idVaga":1}')
        then:
        resposta.statusCode() == status
        json(resposta).keySet() == ['erro'] as Set
        !resposta.body().contains('SEGREDO')
        where:
        falha                                     | status
        new SecurityException('SEGREDO')           | 403
        new IllegalStateException('SEGREDO')       | 409
        new VagaNaoEncontradaException(123L)        | 404
        new RuntimeException('SEGREDO SQL senha')  | 500
    }

    def 'metodos opcionais e gravacao direta de match nao estao disponiveis'() {
        expect:
        enviar('PUT', '/recrutadores/7', '{}').statusCode() == 405
        enviar('POST', '/matches/empresa/7', '{}').statusCode() == 405
        enviar('GET', '/matches/empresa/7/extra').statusCode() == 404
        enviar('POST', '/candidatos/7', '{}').statusCode() == 404
        enviar('POST', '/empresas/7', '{}').statusCode() == 404
    }

    def 'ID malformado em rota conhecida retorna 400: #tipo #rota'() {
        given:
        login(tipo)
        expect:
        enviar('GET', rota).statusCode() == 400
        where:
        tipo         | rota
        'empresa'    | '/matches/empresa/abc'
        'empresa'    | '/vagas/empresa/0'
        'recrutador' | '/matches/recrutador/-1'
        'recrutador' | '/recrutadores/99999999999999999999999'
        'candidato'  | '/curtidas/vagas/candidato/0'
        'candidato'  | '/vagas/abc'
    }

    def 'sessao de conta inativada e invalidada em operacao protegida'() {
        given:
        login('recrutador')
        when:
        def resposta = enviar('GET', '/matches/recrutador/7')
        then:
        1 * recrutadores.estaAtivo(7L) >> false
        0 * matches.consultarMatchesRecrutador(_, _)
        resposta.statusCode() == 401
        when:
        def repeticao = enviar('GET', '/matches/recrutador/7')
        then:
        0 * recrutadores.estaAtivo(_)
        repeticao.statusCode() == 401
    }

    private HttpResponse<String> login(String tipo) {
        def email = [candidato: 'c@teste', empresa: 'e@teste', recrutador: 'r@teste'][tipo]
        enviar('POST', "/login/${tipo}", JsonOutput.toJson([email: email, senha: 'senha-teste']))
    }

    private HttpResponse<String> enviar(String metodo, String rota, String corpo = null,
                                       String origemRequisicao = origem, Map<String, String> headers = [:]) {
        def builder = HttpRequest.newBuilder(URI.create(base + rota)).timeout(Duration.ofSeconds(10))
        if (origemRequisicao != null) builder.header('Origin', origemRequisicao)
        if (corpo != null && !headers.containsKey('Content-Type')) builder.header('Content-Type', 'application/json')
        headers.each { chave, valor -> builder.header(chave, valor) }
        builder.method(metodo, corpo == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(corpo))
        cliente.send(builder.build(), HttpResponse.BodyHandlers.ofString())
    }

    private static def json(HttpResponse<String> resposta) { new JsonSlurper().parseText(resposta.body()) }
}
