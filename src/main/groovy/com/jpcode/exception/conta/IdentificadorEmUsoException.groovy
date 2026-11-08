package com.jpcode.exception.conta

class IdentificadorEmUsoException extends RuntimeException {
    IdentificadorEmUsoException() {
        super('Identificador já utilizado por conta ativa.')
    }
}
