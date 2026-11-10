package com.jpcode.api

import com.jpcode.controller.candidato.CandidatoController
import com.jpcode.controller.empresa.EmpresaController
import com.jpcode.controller.recrutador.RecrutadorController
import jakarta.servlet.http.HttpServletRequest

class SessaoHttp {
    private static final String ATRIBUTO = 'atorAutenticado'
    private final CandidatoController candidatoController
    private final EmpresaController empresaController
    private final RecrutadorController recrutadorController

    SessaoHttp(CandidatoController candidatoController, EmpresaController empresaController,
               RecrutadorController recrutadorController) {
        this.candidatoController = candidatoController
        this.empresaController = empresaController
        this.recrutadorController = recrutadorController
    }

    void invalidar(HttpServletRequest request) {
        request.getSession(false)?.invalidate()
    }

    void iniciar(HttpServletRequest request, Long id, String tipo) {
        invalidar(request)
        request.getSession(true).setAttribute(ATRIBUTO, [id: id, tipo: tipo].asImmutable())
    }

    Map recuperarAtor(HttpServletRequest request) {
        def sessao = request.getSession(false)
        def ator = sessao?.getAttribute(ATRIBUTO)
        boolean ativo = false
        if (ator instanceof Map && ator.id instanceof Long && ator.id > 0) {
            switch (ator.tipo) {
                case 'candidato': ativo = candidatoController.estaAtivo(ator.id); break
                case 'empresa': ativo = empresaController.estaAtivo(ator.id); break
                case 'recrutador': ativo = recrutadorController.estaAtivo(ator.id); break
            }
        }
        if (!ativo) {
            sessao?.invalidate()
            throw new SecurityException('Sessão ausente ou conta inativa.')
        }
        return [id: ator.id, tipo: ator.tipo].asImmutable()
    }
}
