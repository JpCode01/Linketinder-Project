package com.jpcode.api.filter

import groovy.json.JsonOutput
import jakarta.servlet.*
import jakarta.servlet.http.*

class CorsFilter implements Filter {
    static final String ORIGEM = 'http://127.0.0.1:5500'

    void doFilter(ServletRequest entrada, ServletResponse saida, FilterChain chain) {
        HttpServletRequest request = (HttpServletRequest) entrada
        HttpServletResponse response = (HttpServletResponse) saida
        String origem = request.getHeader('Origin')
        response.addHeader('Vary', 'Origin')
        if ((origem != null && origem != ORIGEM) ||
                (request.method in ['POST', 'PUT', 'PATCH', 'DELETE'] && origem != ORIGEM)) {
            rejeitar(response, 403, 'Origem não permitida.')
            return
        }
        if (origem == ORIGEM) {
            response.setHeader('Access-Control-Allow-Origin', ORIGEM)
            response.setHeader('Access-Control-Allow-Credentials', 'true')
        }
        if (request.method == 'OPTIONS') {
            String metodo = request.getHeader('Access-Control-Request-Method')
            String cabecalhos = request.getHeader('Access-Control-Request-Headers')
            if (origem != ORIGEM || !(metodo in ['GET', 'POST', 'PUT']) ||
                    (cabecalhos && cabecalhos.split(',').any { it.trim().toLowerCase(Locale.ROOT) != 'content-type' })) {
                rejeitar(response, 403, 'Preflight não permitido.')
                return
            }
            response.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT')
            response.setHeader('Access-Control-Allow-Headers', 'Content-Type')
            response.status = 204
            return
        }
        if (request.method in ['POST', 'PUT', 'PATCH', 'DELETE'] &&
                request.contentType?.split(';')?.first()?.trim()?.toLowerCase(Locale.ROOT) != 'application/json') {
            rejeitar(response, 400, 'Conteúdo JSON obrigatório.')
            return
        }
        chain.doFilter(request, response)
    }

    private static void rejeitar(HttpServletResponse response, int status, String mensagem) {
        response.contentType = 'application/json'
        response.characterEncoding = 'UTF-8'
        response.status = status
        response.writer.write(JsonOutput.toJson([erro: mensagem]))
    }
}
