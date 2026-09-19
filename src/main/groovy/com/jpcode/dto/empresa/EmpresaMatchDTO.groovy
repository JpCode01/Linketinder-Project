package com.jpcode.dto.empresa

class EmpresaMatchDTO {
    Long id
    String nome
    String cnpj
    String emailCorporativo
    String descricao
    String pais
    String cep
    String estado

    EmpresaMatchDTO(Long id, String nome, String cnpj, String emailCorporativo, String descricao, String pais, String cep, String estado) {
        this.id = id
        this.nome = nome
        this.cnpj = cnpj
        this.emailCorporativo = emailCorporativo
        this.descricao = descricao
        this.pais = pais
        this.cep = cep
        this.estado = estado
    }


    @Override
    public String toString() {
        return """
            ----------------------------------------------------------------

            EMPRESA:
            
            ID: ${id}
            NOME DA EMPRESA: ${nome} 
            EMAIL CORPORATIVO: ${emailCorporativo}
            DESCRICAO: ${descricao}
            CNPJ: ${cnpj}
            CEP: ${cep}
            ESTADO: ${estado}
            PAIS: ${pais}
        """
    }
}
