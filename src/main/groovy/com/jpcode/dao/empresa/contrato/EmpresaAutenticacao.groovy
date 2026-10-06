package com.jpcode.dao.empresa.contrato

import com.jpcode.model.core.Empresa

interface EmpresaAutenticacao {
    Optional<Empresa> buscarPorEmailESenha(String emailCorporativo, String senha)
}