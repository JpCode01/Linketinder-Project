package com.jpcode.exception.vaga

class SemVagasDisponiveisException extends  RuntimeException{

    SemVagasDisponiveisException() {
        super("Nao ha vagas disponiveis no momento!")
    }
}
