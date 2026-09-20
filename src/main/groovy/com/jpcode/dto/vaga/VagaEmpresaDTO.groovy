package com.jpcode.dto.vaga

import com.jpcode.model.referencia.Competencia

class VagaEmpresaDTO {
    Long id
    String nome
    String descricao
    String local
    Integer qtdeCandidatosQueCurtiram
    List<Competencia> competencias

    VagaEmpresaDTO(Long id, String nome, String descricao, String local, Integer qtdeCandidatosQueCurtiram, List<Competencia> competencias) {
        this.id = id
        this.nome = nome
        this.descricao = descricao
        this.local = local
        this.qtdeCandidatosQueCurtiram = qtdeCandidatosQueCurtiram
        this.competencias = competencias
    }


    @Override
    public String toString() {
        return """
        ---------------------------------

        VAGA:
        
        ID: ${id}
        NOME DA VAGA: ${nome}
        DESCRIÇÃO: ${descricao}
        LOCAL: ${local} 
        QUANTIDADE DE CANDIDATOS QUE CURTIRAM: ${qtdeCandidatosQueCurtiram}
        COMPETENCIAS: ${competencias*.nome.join(", ")}
        """
    }
}
