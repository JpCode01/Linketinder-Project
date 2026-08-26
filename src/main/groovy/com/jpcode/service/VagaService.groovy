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
        Vaga vaga = null
        if (empresa != null) {

            vaga = new Vaga(nome, descricao)

            vaga.competencias = empresa.competencias
            empresa.adicionarVaga(vaga)

        }
        return vaga
    }

    void curtir(Candidato candidato, Vaga vaga) {
        if (vaga && candidato && !candidato.vagasCurtidas.contains(vaga) &&
                !vaga.candidatosQueCurtiram.contains(candidato)
        ) {
            vaga.adicionarCandidatoQueCurtiu(candidato)
            candidato.adicionarVagaCurtida(vaga)
        }
    }

    List<Vaga> listarVagas(List<Vaga> vagas) {
        if (!vagas.isEmpty()) {
            vagas.eachWithIndex { vaga, index ->
                println "$index - ${vaga.nome}"
            }
        }
        return vagas

    } 
}
