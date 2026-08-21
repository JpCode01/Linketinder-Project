package com.jpcode.view

import com.jpcode.model.Candidato
import com.jpcode.model.Empresa
import com.jpcode.model.Vaga
import com.jpcode.service.EmpresaService
import com.jpcode.service.VagaService

class MenuEmpresa {
    final Scanner scanner = new Scanner(System.in)
    final EmpresaService empresaService = new EmpresaService()
    final VagaService vagaService = new VagaService()
        
    void inicio() {
        println("""
        1 - Cadastrar Empresa
        2 - Acessar empresa por nome
        """)
        switch (scanner.nextInt()) {
            case 1:
                Menu.empresas.add(empresaService.cadastrarEmpresa())
                break
            case 2:
                scanner.nextLine()
                String nome
                while (true) {
                    println("Digite o nome da empresa: ")
                    nome = scanner.nextLine()
                    if (Menu.empresas.contains(nome)) {
                        Empresa empresa = Menu.empresas.find(nome)
                        println("""
                        1 - Ver Vagas
                        2 - Ver Candidatos em vagas
                        3 - Criar Vaga
                        Qualquer Tecla - Sair
                        """)
                        switch (scanner.nextInt()) {
                            case 1:
                                empresaService.ListarVagasPorEmpresa(empresa)
                            case 2:
                                empresaService.ListarVagasPorEmpresa(empresa)
                                println("Digite o nome da vaga: ")
                                String nomeVaga = scanner.nextLine()
                                if (empresa.find {it.vagas.find(nomeVaga)} != null) {
                                    Vaga vaga = empresa.find {it.vagas.find(nomeVaga)} as Vaga
                                    List candidatos = empresaService.ListarCandidatosPorVaga(vaga)
                                    println("Escolha um candidato por id: ")
                                    int idCandidato = scanner.nextInt()
                                    try {
                                        Candidato candidato = candidatos.get(idCandidato)
                                        println(candidato.competencias)
                                        println("Deseja curtir o candidato(s/n)? ")
                                        if (scanner.nextLine().toLowerCase("s")) {
                                            empresaService.curtirCandidato(candidato, empresa)
                                        }
                                    } catch (IndexOutOfBoundsException ex) {
                                        println("ID inválido!")
                                    }
                                } else {
                                    println("A Empresa nao possui essa vaga")
                                }
                                break
                            case 3:
                                vagaService.criarVaga(empresa)
                            default:
                                return
                        }
                    } else {
                        println("Empresa não encontrada!")
                    }
                }
        }
    }
}
