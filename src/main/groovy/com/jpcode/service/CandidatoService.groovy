package com.jpcode.service

import com.jpcode.enums.CompetenciasEnum
import com.jpcode.model.Candidato
import com.jpcode.validation.Validation

class CandidatoService {
    final Validation validation = new Validation()
    final Scanner scanner = new Scanner(System.in)

    Candidato cadastrarCandidato() {
        println("Nome: ")
        String nome = scanner.nextLine()
        println("Email: ")
        String email = scanner.nextLine()
        println("Cpf: ")
        String cpf = scanner.nextLine()
        println("Idade: ")
        int idade = scanner.nextInt()
        scanner.nextLine()
        println("Estado: ")
        String estado = scanner.nextLine()
        println("Cep: ")
        String cep = scanner.nextLine()
        println("Descricao: ")
        String descricao = scanner.nextLine()
        Candidato candidato = new Candidato(nome, email, cpf, idade, estado, cep, descricao)
        while (true) {
            if (candidato.competencias.size() == CompetenciasEnum.values().length) {
                break
            }
            println("""
        Suas competências: ${candidato.competencias}
        
        1 - Digitar nova competência
        Qualquer tecla - Parar 
        """)
            if (scanner.nextInt() == 1) {
                scanner.nextLine()
                println("""
            Competencias disponiveis: ${CompetenciasEnum.values()}
            Suas competencias: ${candidato.competencias}
            
            Digite uma competencia: """)
                String competencia = scanner.nextLine()
                if (!validation.validarCompetencia(competencia.toUpperCase(), candidato.competencias)) {
                    println("Opa, Parece que você digitou alguma competencia errada ou ja existente, tente novamente!")
                } else {
                    candidato.adicionarCompetencia(CompetenciasEnum.valueOf(competencia.toUpperCase()))
                    println("Candidato cadastrado com sucesso!")
                    break
                }
            } else {
                break
            }
        }
        candidato
    }


}
