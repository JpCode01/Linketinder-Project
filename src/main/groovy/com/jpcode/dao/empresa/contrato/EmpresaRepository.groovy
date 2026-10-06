package com.jpcode.dao.empresa.contrato

import com.jpcode.model.core.Empresa

interface EmpresaRepository {
    Empresa salvar(Empresa empresa)
    Empresa atualizarDados(Empresa empresa)
}