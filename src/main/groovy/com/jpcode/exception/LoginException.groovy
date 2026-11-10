package com.jpcode.exception

class LoginException extends RuntimeException {
    LoginException() {
        super("Credenciais inválidas.")
    }
}