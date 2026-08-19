package com.jpcode.service

import com.jpcode.enums.CompetenciasEnum
import com.jpcode.model.Empresa
import com.jpcode.validation.Validation

class EmpresaService {
    final Validation validation = new Validation()
    final Scanner scanner = new Scanner(System.in)

    Empresa cadastrarEmpresa() {
        println("Nome Empresa: ")
        String nomeEmpresa = scanner.nextLine()
        println("Email: ")
        String emailEmpresa = scanner.nextLine()
        println("Cnpj: ")
        String cnpj = scanner.nextLine()
        println("Pais: ")
        String pais = scanner.nextLine()
        println("Estado: ")
        String estado = scanner.nextLine()
        println("Cep: ")
        String cep = scanner.nextLine()
        println("Descricao: ")
        String descricao = scanner.nextLine()
        Empresa empresa = new Empresa(nomeEmpresa, emailEmpresa, cnpj, pais, estado, cep, descricao)
        while (true) {
            if (empresa.competencias.size() == CompetenciasEnum.values().length) {
                println("A empresa ja busca por todas as competencias Disponiveis")
                break
            }
            println("""
        Competencias esperadas pela empresa: ${empresa.competencias}
        
        1 - Digitar nova competencia
        Qualquer tecla - Parar 
        """)
            if (scanner.nextInt() == 1) {
                scanner.nextLine()
                println("""
        Competencias disponiveis: ${CompetenciasEnum.values()}
        Competencias esperadas pela empresa: ${empresa.competencias}
        
        Digite uma competencia: """)
                String competencia = scanner.nextLine()
                if (!validation.validarCompetencia(competencia.toUpperCase(), empresa.competencias)) {
                    println("Opa, Parece que você digitou alguma competencia errada ou ja existente, tente novamente!")
                } else {
                    empresa.adicionarCompetencia(CompetenciasEnum.valueOf(competencia.toUpperCase()))
                    println("Empresa cadastrado com sucesso!")
                    break
                }
            } else {
                break
            }
        }
        empresa
    }
}
