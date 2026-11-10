package com.jpcode.api.servlet

import com.jpcode.api.SessaoHttp
import com.jpcode.controller.recrutador.RecrutadorController
import com.jpcode.controller.vaga.VagaController
import com.jpcode.dto.recrutador.CadastrarRecrutadorDTO
import com.jpcode.dto.vaga.CadastrarVagaDTO
import jakarta.servlet.http.*
import java.time.LocalDate

class RecrutadorServlet extends JsonServlet {
    private final RecrutadorController recrutadores
    private final VagaController vagas
    private final SessaoHttp sessao

    RecrutadorServlet(RecrutadorController recrutadores, VagaController vagas, SessaoHttp sessao) {
        this.recrutadores = recrutadores; this.vagas = vagas; this.sessao = sessao
    }
    protected List<String> metodosPermitidos() { ['GET', 'POST'] }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        executar(response) {
            rota(request.servletPath == '/recrutadores')
            def atual = ator(request, sessao, ['recrutador'])
            if (!request.pathInfo || request.pathInfo == '/') throw new FalhaHttp(403, 'Listagem de perfis não permitida.')
            Long destino = id(request.pathInfo.substring(1))
            titular(atual, destino)
            json(response, recrutadores.buscarRecrutador(atual.id, destino))
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        executar(response) {
            if (request.servletPath == '/empresas') {
                def caminho = request.pathInfo =~ /^\/([^\/]+)\/recrutadores$/
                rota(caminho.matches())
                def atual = ator(request, sessao, ['empresa'])
                titular(atual, id(caminho.group(1)))
                Map dados = lerJson(request, ['nome', 'cpf', 'email', 'dataNascimento', 'descricao', 'senha', 'idAreaRecrutamento'])
                def dto = new CadastrarRecrutadorDTO(nome: texto(dados, 'nome'), cpf: texto(dados, 'cpf'), email: texto(dados, 'email'),
                        dataNascimento: LocalDate.parse(texto(dados, 'dataNascimento')), descricao: texto(dados, 'descricao'),
                        senha: texto(dados, 'senha'), idAreaRecrutamento: id(dados.idAreaRecrutamento))
                recrutadores.cadastrarRecrutador(atual.id, dto)
                response.status = 201
            } else {
                rota(request.servletPath == '/recrutadores')
                def caminho = request.pathInfo =~ /^\/([^\/]+)\/vagas$/
                rota(caminho.matches())
                def atual = ator(request, sessao, ['recrutador'])
                titular(atual, id(caminho.group(1)))
                Map dados = lerJson(request, ['nome', 'descricao', 'local', 'competencias'])
                vagas.criarVagaRecrutador(atual.id, new CadastrarVagaDTO(texto(dados, 'nome'), texto(dados, 'descricao'), texto(dados, 'local'), null, competencias(dados)))
                response.status = 201
            }
        }
    }
}
