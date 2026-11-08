package com.jpcode.api.servlet

import com.jpcode.api.SessaoHttp
import com.jpcode.controller.vaga.VagaController
import com.jpcode.dto.vaga.*
import jakarta.servlet.http.*

class VagaServlet extends JsonServlet {
    private final VagaController vagas
    private final SessaoHttp sessao

    VagaServlet(VagaController vagas, SessaoHttp sessao) { this.vagas = vagas; this.sessao = sessao }
    protected List<String> metodosPermitidos() { ['GET', 'POST', 'PUT'] }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        executar(response) {
            def atual = ator(request, sessao, ['candidato', 'empresa', 'recrutador'])
            String caminho = request.pathInfo ?: ''
            def interno = caminho =~ /^\/(empresa|recrutador)\/([^\/]+)$/
            if (interno.matches()) {
                String tipo = interno.group(1)
                Long destino = id(interno.group(2))
                if (atual.tipo != tipo) throw new FalhaHttp(403, 'Perfil não permitido.')
                titular(atual, destino)
                json(response, tipo == 'empresa' ? vagas.listarVagasEmpresa(atual.id, destino) : vagas.listarVagasRecrutador(atual.id, destino))
            } else {
                if (atual.tipo != 'candidato') throw new FalhaHttp(403, 'Descoberta restrita ao candidato.')
                if (caminho in ['', '/']) json(response, vagas.buscarTodasAsVagas())
                else {
                    Long destino = id(caminho.substring(1))
                    def vaga = vagas.buscarVaga(destino)
                    json(response, new VagaAnonimaDTO(vaga.id, vaga.descricao, vagas.buscarCompetenciasDeVaga(vaga.id)))
                }
            }
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        executar(response) {
            rota(!request.pathInfo || request.pathInfo == '/')
            def atual = ator(request, sessao, ['empresa'])
            Map dados = lerJson(request, ['nome', 'descricao', 'local', 'competencias'], ['idEmpresa'])
            if (dados.containsKey('idEmpresa') && id(dados.idEmpresa) != atual.id) throw new IllegalArgumentException()
            vagas.criarVagaEmpresa(atual.id, new CadastrarVagaDTO(texto(dados, 'nome'), texto(dados, 'descricao'), texto(dados, 'local'), atual.id, competencias(dados)))
            response.status = 201
        }
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) {
        executar(response) {
            def atual = ator(request, sessao, ['empresa'])
            Long destino = id(request.pathInfo?.substring(1))
            Map dados = lerJson(request, ['nome', 'descricao', 'local'], ['idEmpresa'])
            if (dados.containsKey('idEmpresa') && id(dados.idEmpresa) != atual.id) throw new IllegalArgumentException()
            if (vagas.buscarVaga(destino).idEmpresa != atual.id) throw new FalhaHttp(403, 'Vaga fora do escopo da empresa.')
            vagas.atualizarVaga(new AtualizarVagaDTO(destino, texto(dados, 'nome'), texto(dados, 'descricao'), texto(dados, 'local'), atual.id))
            json(response, [mensagem: 'Vaga atualizada com sucesso.'])
        }
    }
}
