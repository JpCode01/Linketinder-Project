package com.jpcode.api.servlet

import com.jpcode.api.SessaoHttp
import com.jpcode.controller.candidato.CandidatoController
import com.jpcode.controller.empresa.EmpresaController
import com.jpcode.controller.recrutador.RecrutadorController
import com.jpcode.controller.vaga.VagaController
import com.jpcode.dto.candidato.CandidatoViewDTO
import com.jpcode.dto.empresa.EmpresaViewDTO
import com.jpcode.dto.vaga.VagaAnonimaDTO
import com.jpcode.model.core.Vaga
import com.jpcode.model.referencia.Competencia
import groovy.json.JsonSlurper
import jakarta.servlet.http.*
import spock.lang.Specification

class VisibilidadeServletTest extends Specification {
    CandidatoController candidatos = Mock()
    EmpresaController empresas = Mock()
    RecrutadorController recrutadores = Mock()
    SessaoHttp sessao = new SessaoHttp(candidatos, empresas, recrutadores)
    HttpServletRequest request = Mock()
    HttpServletResponse response = Mock()
    HttpSession httpSession = Mock()
    StringWriter corpo = new StringWriter()

    def setup() { response.writer >> new PrintWriter(corpo) }

    def 'listagens identificaveis sao bloqueadas sem consultar perfis: #tipo'() {
        given:
        request.pathInfo >> caminho
        when:
        servlet(tipo).doGet(request, response)
        then:
        1 * response.setStatus(403)
        0 * candidatos._
        0 * empresas._
        json().keySet() == ['erro'] as Set
        where:
        tipo        | caminho
        'candidato' | null
        'candidato' | '/'
        'empresa'   | null
        'empresa'   | '/'
    }

    def 'perfil rejeita sessao ausente ou inativa inclusive na escrita: #tipo #metodo #inativa'() {
        given:
        request.pathInfo >> '/7'
        if (inativa) {
            request.getSession(false) >> httpSession
            httpSession.getAttribute('atorAutenticado') >> [id: 7L, tipo: tipo]
            candidatos.estaAtivo(7L) >> false
            empresas.estaAtivo(7L) >> false
        }
        when:
        servlet(tipo)."${metodo}"(request, response)
        then:
        1 * response.setStatus(401)
        (inativa ? 1 : 0) * httpSession.invalidate()
        0 * candidatos.buscarCandidato(_)
        0 * empresas.buscarEmpresa(_)
        0 * candidatos.atualizarCandidato(_)
        0 * empresas.atualizarEmpresa(_)
        json().keySet() == ['erro'] as Set
        where:
        [tipo, metodo, inativa] << [['candidato', 'empresa'], ['doGet', 'doPut'], [false, true]].combinations()
    }

    def 'perfil bloqueia outro ID ou perfil mesmo com sessao ativa: #tipo #ator #id #metodo'() {
        given:
        autenticar(ator, 7L)
        request.pathInfo >> "/${id}".toString()
        when:
        servlet(tipo)."${metodo}"(request, response)
        then:
        1 * response.setStatus(403)
        0 * candidatos.buscarCandidato(_)
        0 * empresas.buscarEmpresa(_)
        0 * candidatos.atualizarCandidato(_)
        0 * empresas.atualizarEmpresa(_)
        json().keySet() == ['erro'] as Set
        where:
        tipo        | ator         | id | metodo
        'candidato' | 'candidato'  | 8  | 'doGet'
        'empresa'   | 'empresa'    | 8  | 'doGet'
        'candidato' | 'empresa'    | 7  | 'doGet'
        'empresa'   | 'candidato'  | 7  | 'doGet'
        'candidato' | 'recrutador' | 7  | 'doGet'
        'empresa'   | 'recrutador' | 7  | 'doGet'
        'candidato' | 'candidato'  | 8  | 'doPut'
        'empresa'   | 'empresa'    | 8  | 'doPut'
    }

    def 'perfil proprio conserva contrato sem credenciais: #tipo'() {
        given:
        autenticar(tipo, 7L)
        request.pathInfo >> '/7'
        candidatos.buscarCandidato(7L) >> new CandidatoViewDTO(7L, 'Nome', 'Sobrenome', 'c@teste', null, 'cep', 'Desc', null, 30, null, [])
        empresas.buscarEmpresa(7L) >> new EmpresaViewDTO(7L, 'Empresa', 'e@teste', null, 'cep', 'Desc', null)
        when:
        servlet(tipo).doGet(request, response)
        then:
        1 * response.setStatus(200)
        json().keySet() == chaves as Set
        where:
        tipo        | chaves
        'candidato' | ['id', 'nome', 'sobrenome', 'email', 'estado', 'cep', 'descricao', 'pais', 'idade', 'dataNascimento', 'competencias']
        'empresa'   | ['id', 'nome', 'email', 'estado', 'cep', 'descricao', 'pais']
    }

    def 'ID invalido nao consulta perfil e erro inesperado nao revela detalhes: #tipo #caminho'() {
        given:
        autenticar(tipo, 7L)
        request.pathInfo >> caminho
        when:
        servlet(tipo).doGet(request, response)
        then:
        1 * response.setStatus(400)
        0 * candidatos.buscarCandidato(_)
        0 * empresas.buscarEmpresa(_)
        json().keySet() == ['erro'] as Set
        where:
        [tipo, caminho] << [['candidato', 'empresa'], ['/0', '/-1', '/7/extra', '/99999999999999999999999']].combinations()
    }

    def 'falha na validacao de sessao produz erro generico'() {
        given:
        request.pathInfo >> '/7'
        request.getSession(false) >> { throw new IllegalStateException('senha hash salt detalhe SQL') }
        when:
        servlet('candidato').doGet(request, response)
        then:
        1 * response.setStatus(500)
        json() == [erro: 'Erro interno ao validar acesso.']
    }

    def 'descoberta por lista ou ID conserva exatamente contrato anonimo: #caminho'() {
        given:
        VagaController vagas = Mock()
        request.pathInfo >> caminho
        def competencias = [new Competencia(1L, 'RH')]
        vagas.buscarTodasAsVagas() >> [new VagaAnonimaDTO(7L, 'Descrição', competencias)]
        def vaga = new Vaga(7L, 'Nome interno', 'Descrição', 'Local interno', 10L)
        vaga.idRecrutador = 20L
        vagas.buscarVaga(7L) >> vaga
        vagas.buscarCompetenciasDeVaga(7L) >> competencias
        when:
        new VagaServlet(vagas, Stub(SessaoHttp, constructorArgs: [null, null, null]) { recuperarAtor(_) >> [id: 1L, tipo: 'candidato'] }).doGet(request, response)
        then:
        1 * response.setStatus(200)
        def itens = json() instanceof List ? json() : [json()]
        itens == [[id: 7, descricao: 'Descrição', competencias: [[id: 1, nome: 'RH']]]]
        where:
        caminho << [null, '/7']
    }

    private def servlet(String tipo) {
        tipo == 'candidato' ? new CandidatoServlet(candidatos, sessao) : new EmpresaServlet(empresas, sessao)
    }

    private void autenticar(String tipo, Long id) {
        request.getSession(false) >> httpSession
        httpSession.getAttribute('atorAutenticado') >> [id: id, tipo: tipo]
        candidatos.estaAtivo(id) >> true
        empresas.estaAtivo(id) >> true
        recrutadores.estaAtivo(id) >> true
    }

    private def json() { new JsonSlurper().parseText(corpo.toString()) }
}
