package com.jpcode.dto.Match

import com.jpcode.model.referencia.Competencia

class MatchEncontradoDTO {
    Long id
    String nomeCandidato
    String sobrenomeCandidato
    String emailCandidato
    String cpfCandidato
    String descricaoCandidato
    int idadeCandidato
    String estadoCandidato
    String paisCandidato
    List<Competencia> competenciasCandidato

    String nomeEmpresa
    String cnpjEmpresa
    String emailCorporativo
    String descricaoEmpresa
    String paisEmpresa
    String estadoEmpresa

    String nome
    String descricao
    List<String> competencias

    MatchEncontradoDTO(Long id, String nomeCandidato, String sobrenomeCandidato, String emailCandidato, String cpfCandidato, String descricaoCandidato, int idadeCandidato, String estadoCandidato, String paisCandidato, List<Competencia> competenciasCandidato, String nomeEmpresa, String cnpjEmpresa, String emailCorporativo, String descricaoEmpresa, String paisEmpresa, String estadoEmpresa, String nome, String descricao, List<String> competencias) {
        this.id = id
        this.nomeCandidato = nomeCandidato
        this.sobrenomeCandidato = sobrenomeCandidato
        this.emailCandidato = emailCandidato
        this.cpfCandidato = cpfCandidato
        this.descricaoCandidato = descricaoCandidato
        this.idadeCandidato = idadeCandidato
        this.estadoCandidato = estadoCandidato
        this.paisCandidato = paisCandidato
        this.competenciasCandidato = competenciasCandidato

        this.nomeEmpresa = nomeEmpresa
        this.cnpjEmpresa = cnpjEmpresa
        this.emailCorporativo = emailCorporativo
        this.descricaoEmpresa = descricaoEmpresa

        this.paisEmpresa = paisEmpresa
        this.estadoEmpresa = estadoEmpresa
        this.nome = nome
        this.descricao = descricao
        this.competencias = competencias
    }


    @Override
    public String toString() {
      return """
        ----------------------------------------------------------------
    
        MATCH:
    
        ID MATCH: ${id  }
    
        CANDIDATO:
    
        NOME: ${nomeCandidato} ${sobrenomeCandidato}
        EMAIL: ${emailCandidato}
        DESCRICAO: ${descricaoCandidato}
        IDADE: ${idadeCandidato}
        CPF: ${cpfCandidato}
        ESTADO: ${estadoCandidato}
        PAIS: ${paisCandidato}
        COMPETENCIAS: ${competenciasCandidato*.nome.join(", ")}
    
        EMPRESA:
    
        NOME DA EMPRESA: ${nomeEmpresa}
        EMAIL CORPORATIVO: ${emailCorporativo}
        DESCRICAO: ${descricaoEmpresa}
        CNPJ: ${cnpjEmpresa}
        ESTADO: ${estadoEmpresa}
        PAIS: ${paisEmpresa}
    
        VAGA:
    
        NOME DA VAGA: ${nome}
        DESCRIÇÃO: ${descricao}
        COMPETENCIAS: ${competencias.join(", ")}
        """
    }
}
