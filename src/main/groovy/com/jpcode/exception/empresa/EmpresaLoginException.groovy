package com.jpcode.exception.empresa

class EmpresaLoginException extends RuntimeException {

    EmpresaLoginException(String email, String senha) {
        super("Empresa de email ${email} e senha ${senha} não encontrada!")
    }
}
