package com.jpcode.api.servlet

import com.jpcode.api.SessaoHttp
import com.jpcode.controller.vaga.VagaController
import com.jpcode.controller.match.MatchController
import jakarta.servlet.http.*

class CurtidaServlet extends JsonServlet {
    private final VagaController vagas
    private final MatchController matches
    private final SessaoHttp sessao
    CurtidaServlet(VagaController vagas, MatchController matches, SessaoHttp sessao) {
        this.vagas = vagas; this.matches = matches; this.sessao = sessao
    }
    protected List<String> metodosPermitidos() { ['GET', 'POST'] }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        executar(response) {
            String caminho = request.pathInfo
            rota(caminho in ['/vagas', '/candidatos'])
            if (caminho == '/vagas') {
                def atual = ator(request, sessao, ['candidato'])
                Map dados = lerJson(request, ['idVaga'])
                Long destino = id(dados.idVaga)
                vagas.buscarVaga(destino)
                vagas.curtir(atual.id, destino)
            } else {
                def atual = ator(request, sessao, ['empresa', 'recrutador'])
                Map dados = lerJson(request, ['idCandidato', 'idVaga'])
                Long candidato = id(dados.idCandidato), vaga = id(dados.idVaga)
                if (atual.tipo == 'empresa') matches.retribuirInteresseEmpresa(atual.id, candidato, vaga)
                else matches.retribuirInteresseRecrutador(atual.id, candidato, vaga)
            }
            response.status = 204
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        executar(response) {
            def curtidas = request.pathInfo =~ /^\/vagas\/candidato\/([^\/]+)$/
            def interessados = request.pathInfo =~ /^\/candidatos\/vaga\/([^\/]+)$/
            rota(curtidas.matches() || interessados.matches())
            if (curtidas.matches()) {
                def atual = ator(request, sessao, ['candidato'])
                Long destino = id(curtidas.group(1))
                titular(atual, destino)
                json(response, vagas.listarVagasCurtidas(atual.id))
            } else {
                def atual = ator(request, sessao, ['empresa', 'recrutador'])
                Long vaga = id(interessados.group(1))
                json(response, atual.tipo == 'empresa' ? matches.buscarInteressadosEmpresa(atual.id, vaga) : matches.buscarInteressadosRecrutador(atual.id, vaga))
            }
        }
    }
}
