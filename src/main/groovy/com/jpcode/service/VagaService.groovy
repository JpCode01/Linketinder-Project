package com.jpcode.service

import com.jpcode.model.Candidato
import com.jpcode.model.Empresa
import com.jpcode.model.Vaga

class VagaService {
    final Scanner scanner = new Scanner(System.in)
    Vaga criarVaga(Empresa empresa) {
        println("Digite o nome da vaga: ")
        String nome = scanner.nextLine()
        println("Digite a descrição da vaga: ")
        String descricao = scanner.nextLine()
        Vaga vaga = new Vaga(nome, descricao)
        vaga.competencias = empresa.competencias
    }

    void curtir(Candidato candidato, Vaga vaga) {
        vaga.adicionarCandidatoQueCurtiu(candidato)
        candidato.adicionarVagaCurtida(vaga)
    }
}
