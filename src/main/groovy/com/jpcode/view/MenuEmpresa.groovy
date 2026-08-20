package com.jpcode.view

import com.jpcode.model.Empresa
import com.jpcode.service.EmpresaService
import com.jpcode.validation.MenuValidation

class MenuEmpresa {
    final Scanner scanner = new Scanner(System.in)
    final EmpresaService empresaService = new EmpresaService()
    final MenuValidation validation = new MenuValidation();
        
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
                        1 - Ver Vagas Disponiveis
                        2 - Ver Candidatos por Vaga
                        """)
                        switch (scanner.nextInt()) {
                            case 1:
                                empresaService.ListarVagasPorEmpresa(empresa)
                            case 2:
                                empresaService.ListarVagasPorEmpresa(empresa)
                                println("Digite o nome da vaga: ")
                                String nomeVaga = scanner.nextLine()
                                empresaService.ListarCandidatosPorVaga(empresa, nomeVaga)
                        }
                    }
                }
        }
    }
}
