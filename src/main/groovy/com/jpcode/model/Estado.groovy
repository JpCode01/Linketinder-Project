package com.jpcode.model

class Estado {
    Long id
    String sigla

    Estado(String sigla) {
        this.sigla = sigla
    }

    Estado(Long id, String sigla) {
        this.id = id
        this.sigla = sigla
    }
}
