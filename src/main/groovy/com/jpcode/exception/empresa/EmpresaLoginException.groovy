package com.jpcode.exception.empresa

class EmpresaLoginException extends RuntimeException {

    EmpresaLoginException() {
        super('Credenciais inválidas para empresa.')
    }

    EmpresaLoginException(String email, String senha) {
        this()
    }
}
