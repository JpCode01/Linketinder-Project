package com.jpcode.model.core

class Match {
    Long id
    Long idCandidato
    Long idEmpresa
    Long idVaga

    Match(Long id, Long idCandidato, Long idEmpresa, Long idVaga) {
        this.idCandidato = idCandidato
        this.idEmpresa = idEmpresa
        this.idVaga = idVaga
        this.id = id
    }
}
