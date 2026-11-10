package com.jpcode.integracao

import com.jpcode.config.*
import com.jpcode.dao.candidato.*
import com.jpcode.dao.empresa.EmpresaDAO
import com.jpcode.dao.match.MatchDAO
import com.jpcode.dao.recrutador.RecrutadorDAO
import com.jpcode.dao.referencia.*
import com.jpcode.dao.relacionamento.*
import com.jpcode.dao.vaga.*
import groovy.json.*
import org.apache.catalina.startup.Tomcat
import spock.lang.Specification

import java.net.http.*
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import static org.junit.jupiter.api.Assumptions.assumeTrue

abstract class SuporteHttpPostgres extends Specification {
    SuporteContasPostgres banco
    Tomcat tomcat
    String base
    List<HttpClient> clientes = []

    def setup() {
        assumeTrue(SuportePostgres.configurado(), 'Integração NÃO EXECUTADA: configure o banco dedicado de testes.')
        banco = new SuporteContasPostgres()
        def factory = banco.factory
        def pais = new PaisDAO(factory)
        def estados = new EstadoDAO(factory)
        def areas = new AreaRecrutamentoDAO(factory)
        def candidatos = new CandidatoDAO(factory)
        def empresas = new EmpresaDAO(factory)
        def recrutadores = new RecrutadorDAO(factory)
        def competencias = new CompetenciaDAO(factory)
        def competenciasCandidato = new CompetenciasCandidatoDAO(factory)
        def competenciasVaga = new CompetenciasVagaDAO(factory)
        def vagas = new VagaDAO(factory, competenciasVaga)
        def interesses = new CandidatoCurtirDAO(factory)
        def retribuicoes = new EmpresaCurtirDAO(factory)
        def matches = new MatchDAO(factory, competenciasCandidato, competenciasVaga)
        DaoConfig daos = Stub() {
            getConnectionFactory() >> factory
            getPaisDAO() >> pais
            getEstadoDAO() >> estados
            getAreaRecrutamentoDAO() >> areas
            getCandidatoDAO() >> candidatos
            getEmpresaDAO() >> empresas
            getRecrutadorDAO() >> recrutadores
            getCompetenciaDAO() >> competencias
            getCompetenciasCandidatoDAO() >> competenciasCandidato
            getCompetenciasVagaDAO() >> competenciasVaga
            getVagaDAO() >> vagas
            getCandidatoCurtirDAO() >> interesses
            getEmpresaCurtirDAO() >> retribuicoes
            getMatchDAO() >> matches
        }
        try (def conexao = factory.getConnection(); def comando = conexao.createStatement()) {
            comando.execute("INSERT INTO competencias (nome_competencia) VALUES ('RH')")
        }
        def pasta = Files.createTempDirectory(Path.of('build'), 'http-postgres-')
        tomcat = new Tomcat()
        tomcat.baseDir = pasta.toAbsolutePath().toString()
        tomcat.port = portaHttp()
        tomcat.connector
        def contexto = tomcat.addContext('', pasta.toAbsolutePath().toString())
        new ApiServletConfig(tomcat, contexto, new ControllerConfig(new ServiceConfig(daos))).registrarServlets()
        tomcat.start()
        base = "http://127.0.0.1:${tomcat.connector.localPort}"
    }

    def cleanup() {
        clientes.each { it.close() }
        tomcat?.stop()
        tomcat?.destroy()
        banco?.close()
    }

    protected int portaHttp() { 0 }

    protected static Set<String> chavesMatch() {
        ['id', 'nomeCandidato', 'sobrenomeCandidato', 'emailCandidato', 'cpfCandidato', 'descricaoCandidato',
         'idadeCandidato', 'estadoCandidato', 'paisCandidato', 'competenciasCandidato', 'nomeEmpresa', 'cnpjEmpresa',
         'emailCorporativo', 'descricaoEmpresa', 'paisEmpresa', 'estadoEmpresa', 'nome', 'descricao', 'competencias',
         'nomeRecrutador', 'areaRecrutamento'] as Set
    }

    protected long quantidade(String tabela) {
        assert tabela in ['vagas', 'vagas_competencias', 'candidatos_competencias', 'vagas_curtidas_candidato', 'candidatos_curtidos_empresa', 'match']
        try (def conexao = banco.factory.getConnection(); def comando = conexao.createStatement();
             def resultado = comando.executeQuery("SELECT count(*) FROM ${tabela}".toString())) {
            resultado.next()
            resultado.getLong(1)
        }
    }

    protected HttpClient cliente() {
        def novo = HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).connectTimeout(Duration.ofSeconds(5)).build()
        clientes.add(novo)
        novo
    }

    protected HttpResponse<String> login(HttpClient cliente, String tipo, String email) {
        enviar(cliente, 'POST', "/login/${tipo}", [email: email, senha: 'senha-t11'])
    }

    protected HttpResponse<String> enviar(HttpClient cliente, String metodo, String rota, Map dados = null) {
        def builder = HttpRequest.newBuilder(URI.create(base + rota)).timeout(Duration.ofSeconds(15))
                .header('Origin', 'http://127.0.0.1:5500')
        if (dados != null) builder.header('Content-Type', 'application/json')
        builder.method(metodo, dados == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(JsonOutput.toJson(dados)))
        cliente.send(builder.build(), HttpResponse.BodyHandlers.ofString())
    }

    protected static def json(HttpResponse<String> resposta) { new JsonSlurper().parseText(resposta.body()) }
}
