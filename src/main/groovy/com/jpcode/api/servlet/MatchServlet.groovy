package com.jpcode.api.servlet

import com.jpcode.api.SessaoHttp
import com.jpcode.controller.match.MatchController
import jakarta.servlet.http.*

class MatchServlet extends JsonServlet {
    private final MatchController matches
    private final SessaoHttp sessao
    MatchServlet(MatchController matches, SessaoHttp sessao) { this.matches = matches; this.sessao = sessao }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        executar(response) {
            def caminho = request.pathInfo =~ /^\/(candidato|empresa|recrutador)\/([^\/]+)$/
            rota(caminho.matches())
            String tipo = caminho.group(1)
            def atual = ator(request, sessao, [tipo])
            Long destino = id(caminho.group(2))
            titular(atual, destino)
            switch (tipo) {
                case 'candidato': json(response, matches.consultarMatchesCandidato(atual.id, destino)); break
                case 'empresa': json(response, matches.consultarMatchesEmpresa(atual.id, destino)); break
                case 'recrutador': json(response, matches.consultarMatchesRecrutador(atual.id, destino)); break
            }
        }
    }
}
