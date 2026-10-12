package com.jpcode.dao.empresa.contrato

import com.jpcode.model.core.Empresa

interface EmpresaRepository {
    Empresa salvar(Empresa empresa)

    Optional<Empresa> buscarPorId(Long id)

    Empresa atualizarDados(Empresa empresa)
}