package com.jpcode.dao.candidato.contrato

import com.jpcode.model.core.Candidato

interface CandidatoAutenticacao {
    Optional<Candidato> buscarPorEmailESenha(String email, String senha)
}