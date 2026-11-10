package com.jpcode.api.servlet

import com.jpcode.api.SessaoHttp
import com.jpcode.exception.conta.IdentificadorEmUsoException
import com.jpcode.exception.vaga.VagaNaoEncontradaException
import com.jpcode.exception.referencia.*
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import jakarta.servlet.http.*

abstract class JsonServlet extends HttpServlet {
    protected List<String> metodosPermitidos() { ['GET'] }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) {
        if (!(request.method in metodosPermitidos())) {
            response.setHeader('Allow', metodosPermitidos().join(', '))
            erro(response, 405, 'Método não permitido.')
            return
        }
        super.service(request, response)
    }

    protected static void executar(HttpServletResponse response, Closure operacao) {
        response.contentType = 'application/json'
        response.characterEncoding = 'UTF-8'
        try { operacao.call() }
        catch (FalhaHttp e) { erro(response, e.status, e.message) }
        catch (IdentificadorEmUsoException ignored) { erro(response, 409, 'Identificador já utilizado por conta ativa.') }
        catch (VagaNaoEncontradaException ignored) { erro(response, 404, 'Recurso não encontrado.') }
        catch (SecurityException ignored) { erro(response, 403, 'Acesso fora do escopo do ator.') }
        catch (IllegalStateException ignored) { erro(response, 409, 'Operação indisponível no estado atual.') }
        catch (IllegalArgumentException | java.time.DateTimeException | PaisNaoEncontradoException | EstadoNaoEncontradoException | CompetenciaNaoEncontradaException ignored) {
            erro(response, 400, 'JSON, campos ou referências inválidos.')
        }
        catch (Exception ignored) { erro(response, 500, 'Erro interno ao processar requisição.') }
    }

    protected static Map ator(HttpServletRequest request, SessaoHttp sessao, List<String> tipos) {
        Map atual
        try { atual = sessao.recuperarAtor(request) }
        catch (SecurityException ignored) { throw new FalhaHttp(401, 'Sessão ausente ou conta inativa.') }
        catch (Exception ignored) { throw new FalhaHttp(500, 'Erro interno ao validar acesso.') }
        if (!(atual.tipo in tipos)) throw new FalhaHttp(403, 'Perfil não permitido.')
        atual
    }

    protected static Long id(Object valor) {
        if (!(valor instanceof String || valor instanceof Number) || !(valor.toString() ==~ /[0-9]+/)) throw new IllegalArgumentException()
        Long numero = valor.toString().toLong()
        if (numero <= 0) throw new IllegalArgumentException()
        numero
    }

    protected static void titular(Map atual, Long recurso) {
        if (atual.id != recurso) throw new FalhaHttp(403, 'Acesso restrito ao próprio titular.')
    }

    protected static Map lerJson(HttpServletRequest request, List<String> obrigatorios, List<String> opcionais = []) {
        try {
            request.characterEncoding = 'UTF-8'
            def dados = new JsonSlurper().parse(request.reader)
            if (!(dados instanceof Map) || !dados.keySet().containsAll(obrigatorios) ||
                    !((obrigatorios + opcionais) as Set).containsAll(dados.keySet())) throw new IllegalArgumentException()
            obrigatorios.each { campo -> if (dados[campo] == null) throw new IllegalArgumentException() }
            return dados
        } catch (Exception ignored) { throw new IllegalArgumentException('JSON inválido.') }
    }

    protected static String texto(Map dados, String campo) {
        if (!(dados[campo] instanceof String) || !dados[campo].trim()) throw new IllegalArgumentException()
        dados[campo]
    }

    protected static List<String> competencias(Map dados) {
        if (!(dados.competencias instanceof List) || dados.competencias.any { !(it instanceof String) || !it.trim() }) throw new IllegalArgumentException()
        dados.competencias
    }

    protected static void json(HttpServletResponse response, Object dados, int status = 200) {
        response.status = status
        response.writer.write(JsonOutput.toJson(dados))
    }

    protected static void erro(HttpServletResponse response, int status, String mensagem) {
        response.contentType = 'application/json'
        response.characterEncoding = 'UTF-8'
        json(response, [erro: mensagem], status)
    }

    protected static void rota(boolean valida) {
        if (!valida) throw new FalhaHttp(404, 'Rota inexistente.')
    }

    protected static class FalhaHttp extends RuntimeException {
        final int status
        FalhaHttp(int status, String mensagem) { super(mensagem); this.status = status }
    }
}
