package com.jpcode.model

class Match {
    Long id
    Long idCandidato
    Long idEmpresa
    Long idVaga

    Match(Long idCandidato, Long idEmpresa, Long idVaga) {
        this.idCandidato = idCandidato
        this.idEmpresa = idEmpresa
        this.idVaga = idVaga
    }

    Match(Long id, Long idCandidato, Long idEmpresa, Long idVaga) {
        this.idCandidato = idCandidato
        this.idEmpresa = idEmpresa
        this.idVaga = idVaga
        this.id = id
    }


//    @Override
//    String toString() {
//        return """
//        ----------------------------------
//        MATCH ENCONTRADO:
//        
//        Nome da empresa: ${empresa.nome}
//        Descrição da empresa: ${empresa.descricao}
//        
//        Nome da vaga: ${vaga.nome}
//        Descrição da vaga: ${vaga.descricao}
//        Competências da Vaga: ${vaga.competencias}
//
//        Candidato: ${candidato.nome}
//        Competências: ${candidato.competencias}
//        """
//    }
}
