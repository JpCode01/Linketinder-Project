package com.jpcode.model

class Match {
    Empresa empresa
    Candidato candidato
    Vaga vaga

    Match(Empresa empresa, Candidato candidato, Vaga vaga) {
        this.empresa = empresa
        this.candidato = candidato
        this.vaga = vaga
    }


    @Override
    String toString() {
        return """
        ----------------------------------
        MATCH ENCONTRADO:
        
        Nome da empresa: ${empresa.nome}
        Descrição da empresa: ${empresa.descricao}
        
        Nome da vaga: ${vaga.nome}
        Descrição da vaga: ${vaga.descricao}
        Competências da Vaga: ${vaga.competencias}

        Candidato: ${candidato.nome}
        Competências: ${candidato.competencias}
        """
    }
}
