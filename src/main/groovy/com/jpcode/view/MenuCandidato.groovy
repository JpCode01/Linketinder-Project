package com.jpcode.view

import com.jpcode.model.Candidato
import com.jpcode.model.Vaga
import com.jpcode.service.CandidatoService
import com.jpcode.service.VagaService

class MenuCandidato {
    final Scanner scanner = new Scanner(System.in)
    final CandidatoService candidatoService = new CandidatoService()
    final VagaService vagaService = new VagaService()
    
    void inicio() {
        println("""
        1 - Cadastre-se 
        2 - Login por nome
        """)
        switch (scanner.nextInt()) {
            case 1:
                Menu.candidatos.add(candidatoService.cadastrarCandidato())
                break;
            case 2:
                scanner.nextLine()
                println("Digite o nome do candidato desejado: ")
                String nome = scanner.nextLine()
                while (true) {
                    if (Menu.candidatos.find {it.nome == nome}) {
                        Candidato candidato = Menu.candidatos.find {it.nome == nome}
                        println(candidato)
                        println("""
                        1 - Ver vagas curtidas
                        2 - Ver vagas disponíveis
                        3 - Curtir Vaga
                        4 - Sair
                        """)
                        switch (scanner.nextInt()) {
                            case 1: 
                                if (!candidato.vagasCurtidas.isEmpty()) {
                                    println(candidato.vagasCurtidas)
                                } else {
                                    println("Não há nenhuma vaga curtida")
                                }
                                break
                            case 2:
                                if (candidato.vagasCurtidas.isEmpty()) {
                                    println(Menu.vagasGerais)
                                } else {
                                    println(Menu.vagasGerais - candidato.vagasCurtidas)
                                }
                                break
                            case 3:
                                println(Menu.vagasGerais)
                                List vagas
                                if (candidato.vagasCurtidas.isEmpty()) {
                                    vagas = vagaService.listarVagas(Menu.vagasGerais)
                                } else {
                                    vagas = vagaService.listarVagas(Menu.vagasGerais - candidato.vagasCurtidas)
                                }
                                println("Escolha uma vaga por ID: ")
                                int idVaga = scanner.nextInt()
                                scanner.nextLine()
                                try {
                                    Vaga vaga = vagas.get(idVaga)
                                    println(vaga.competencias)
                                    println("Deseja curtir a vaga(s/n)? ")
                                    
                                    if  (scanner.nextLine().toLowerCase()) {
                                        vagaService.curtir(candidato, vaga)
                                    } else {
                                        println("Opção invalida")
                                    }
                                } catch (IndexOutOfBoundsException ex) {
                                    println("ID invalido")
                                }
                                break
                            case 4:
                                return
                        }
                    }
                }
        }
    }
}
