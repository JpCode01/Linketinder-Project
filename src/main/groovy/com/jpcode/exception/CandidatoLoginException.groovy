package com.jpcode.exception

class CandidatoLoginException extends RuntimeException {

    CandidatoLoginException(String email, String senha) {
        super("Candidato de email ${email} e senha ${senha} não encontrado!")
    }
}
