package com.jpcode.api.servlet

import com.jpcode.controller.recrutador.RecrutadorController
import jakarta.servlet.http.*

class AreaRecrutamentoServlet extends JsonServlet {
    private final RecrutadorController recrutadores
    AreaRecrutamentoServlet(RecrutadorController recrutadores) { this.recrutadores = recrutadores }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        executar(response) { json(response, recrutadores.buscarAreas().collect { [id: it.id, nome: it.nome] }) }
    }
}
