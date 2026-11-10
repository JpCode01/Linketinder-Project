package com.jpcode.api.servlet

import com.jpcode.controller.referencia.ReferenciaController
import jakarta.servlet.http.*

class ReferenciaServlet extends JsonServlet {
    private final ReferenciaController referencias
    ReferenciaServlet(ReferenciaController referencias) { this.referencias = referencias }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        executar(response) {
            rota(request.pathInfo in ['/paises', '/estados'])
            json(response, request.pathInfo == '/paises' ? referencias.listarPaises().collect { [id: it.id, nome: it.nome] } : referencias.listarEstados().collect { [id: it.id, sigla: it.sigla] })
        }
    }
}
