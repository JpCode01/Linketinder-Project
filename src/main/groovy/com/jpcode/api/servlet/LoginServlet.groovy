package com.jpcode.api.servlet

import com.jpcode.api.SessaoHttp
import com.jpcode.controller.candidato.CandidatoController
import com.jpcode.controller.empresa.EmpresaController
import com.jpcode.controller.recrutador.RecrutadorController
import com.jpcode.exception.LoginException
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class LoginServlet extends JsonServlet {
    private final CandidatoController candidatoController
    private final EmpresaController empresaController
    private final RecrutadorController recrutadorController
    private final SessaoHttp sessaoHttp

    LoginServlet(CandidatoController candidatoController, EmpresaController empresaController,
                 RecrutadorController recrutadorController, SessaoHttp sessaoHttp) {
        this.candidatoController = candidatoController
        this.empresaController = empresaController
        this.recrutadorController = recrutadorController
        this.sessaoHttp = sessaoHttp
    }

    protected List<String> metodosPermitidos() { ['POST'] }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        response.contentType = 'application/json'
        response.characterEncoding = 'UTF-8'
        sessaoHttp.invalidar(request)
        String tipo = request.servletPath?.tokenize('/')?.last()
        if (!(request.servletPath in ['/login/candidato', '/login/empresa', '/login/recrutador'])) {
            responderErro(response, 404, 'Rota inexistente.')
            return
        }
        if (request.getHeader('Origin') != 'http://127.0.0.1:5500') {
            responderErro(response, 403, 'Origem não permitida.')
            return
        }
        def dados
        try {
            if (request.contentType?.split(';')?.first()?.trim()?.toLowerCase(Locale.ROOT) != 'application/json') {
                throw new IllegalArgumentException()
            }
            request.characterEncoding = 'UTF-8'
            dados = new JsonSlurper().parse(request.reader)
            if (!(dados instanceof Map) || dados.keySet() != ['email', 'senha'] as Set ||
                    !(dados.email instanceof String) || !dados.email.trim() ||
                    !(dados.senha instanceof String) || !dados.senha) {
                throw new IllegalArgumentException()
            }
        } catch (Exception ignored) {
            responderErro(response, 400, 'JSON inválido ou campos em formato incorreto.')
            return
        }
        try {
            def conta
            switch (tipo) {
                case 'candidato': conta = candidatoController.logar(dados.email, dados.senha); break
                case 'empresa': conta = empresaController.logar(dados.email, dados.senha); break
                case 'recrutador': conta = recrutadorController.logar(dados.email, dados.senha); break
            }
            if (conta == null || !conta.ativo || conta.id == null) throw new SecurityException()
            sessaoHttp.iniciar(request, conta.id, tipo)
            response.status = 200
            response.writer.write(JsonOutput.toJson([id: conta.id, tipo: tipo]))
        } catch (LoginException | SecurityException ignored) {
            responderErro(response, 401, 'Credenciais inválidas.')
        } catch (Exception ignored) {
            sessaoHttp.invalidar(request)
            responderErro(response, 500, 'Erro interno ao autenticar.')
        }
    }

    private static void responderErro(HttpServletResponse response, int status, String mensagem) {
        response.status = status
        response.writer.write(JsonOutput.toJson([erro: mensagem]))
    }
}
