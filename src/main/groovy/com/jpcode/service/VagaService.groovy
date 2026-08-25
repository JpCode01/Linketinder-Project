package com.jpcode.service

import com.jpcode.model.Candidato
import com.jpcode.model.Empresa
import com.jpcode.model.Vaga

class VagaService {
    
    Vaga criarVaga(
            Empresa empresa,
            String nome,
            String descricao
    ) {
        Vaga vaga = new Vaga(nome, descricao)

        vaga.competencias = empresa.competencias
        empresa.adicionarVaga(vaga)

        return vaga
    }

    void curtir(Candidato candidato, Vaga vaga) {
        vaga.adicionarCandidatoQueCurtiu(candidato)
        candidato.adicionarVagaCurtida(vaga)
    }

    List<Vaga> listarVagas(List<Vaga> vagas) {
        vagas.eachWithIndex { vaga, index ->
            println "$index - ${vaga.nome}"
        }
        return vagas

    } 
}
