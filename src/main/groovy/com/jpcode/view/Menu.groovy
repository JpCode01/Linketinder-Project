package com.jpcode.view

import com.jpcode.enums.CompetenciasEnum
import com.jpcode.model.Candidato
import com.jpcode.model.Empresa
import com.jpcode.service.CandidatoService
import com.jpcode.service.EmpresaService

class Menu {
    private final Scanner scanner = new Scanner(System.in)
    private final CandidatoService candidatoService = new CandidatoService()
    private final EmpresaService empresaService = new EmpresaService()
    private final MenuEmpresa menuPrincipal = new MenuEmpresa()
    static List candidatos = []
    static List empresas = []

    void inicio() {

        instanciarObjetosEmpresa()
        instanciarObjetosCandidato()
        println("""
    SEJA BEM VINDO AO LINKETINDER, AQUI EMPRESAS PODEM
    ENCONTRAR CANDIDATOS POR MATCH
    """)

        while (true) {
            println("""
        1 - ENTRAR COMO EMPRESA
        2 - ENTRAR COMO CANDIDATO

        QUALQUER TELA - SAIR
        
//        ESCOLHA A OPCAO DESEJADA:""")
            switch (scanner.nextInt()) {
                case 1:
                    
            }
//            switch (scanner.nextInt()) {
//                case 1:
//                    empresas.add(empresaService.cadastrarEmpresa())
//                    break
//                case 2:
//                    candidatos.add(candidatoService.cadastrarCandidato())
//                    break
//                case 3:
//                    listarCandidatos()
//                    break
//                case 4:
//                    listarEmpresas()
//                    break
//                default:
//                    return
//
//            }
        }

    }

    void instanciarObjetosEmpresa() {
        Empresa empresa = new Empresa("IBM", "ibm@gmail.com",
                "56.789.012/0001-34", "Brasil", "Paraná", "80000-000", "IBM Brasil");
        empresa.competencias.addAll([
                CompetenciasEnum.JAVA,
                CompetenciasEnum.GROOVY
        ])
        empresas.add(empresa)
        Empresa empresa1 = new Empresa("Google", "google@gmail.com",
                "12.345.678/0001-90", "Brasil", "São Paulo", "01000-000", "Google Brasil");
        empresa1.competencias.addAll([
                CompetenciasEnum.PYTHON,
                CompetenciasEnum.JAVASCRIPT,
                CompetenciasEnum.DJANGO
        ])
        empresas.add(empresa1)
        Empresa empresa2 = new Empresa("Amazon", "amazon@gmail.com",
                "23.456.789/0001-01", "USA", "Washington", "98101-000", "Amazon");
        empresas.add(empresa2)
        Empresa empresa3 = new Empresa("Oracle", "oracle@gmail.com",
                "34.567.890/0001-12", "USA", "Texas", "73301-000", "Oracle Corporation");
        empresas.add(empresa3)
        Empresa empresa4 = new Empresa("NVIDIA", "nvidia@gmail.com",
                "45.678.901/0001-23", "USA", "Califórnia", "95050-000", "NVIDIA Corporation");
        empresas.add(empresa4)
    }

    void instanciarObjetosCandidato() {
        Candidato candidato = new Candidato("Joelinton", "joelinton@gmail.com", "cpf", 40, "Maranhao", "cep", "Programador Java Senior")
        candidato.competencias.addAll([
                CompetenciasEnum.JAVA,
                CompetenciasEnum.GROOVY
        ])
        candidatos.add(candidato)
        Candidato candidato1 = new Candidato("Carlos", "carlos@gmail.com", "12345678901", 28, "Sao Paulo", "12900000", "Desenvolvedor Java Pleno");
        candidato1.competencias.addAll([
                CompetenciasEnum.PYTHON,
                CompetenciasEnum.JAVASCRIPT,
                CompetenciasEnum.DJANGO
        ])
        candidatos.add(candidato1)
        Candidato candidato2 = new Candidato("Mariana", "mariana@gmail.com", "98765432100", 32, "Minas Gerais", "30100000", "Analista de Sistemas");
        candidatos.add(candidato2)
        Candidato candidato3 = new Candidato("Rafael", "rafael@gmail.com", "45678912300", 25, "Parana", "80000000", "Desenvolvedor Backend Júnior");
        candidatos.add(candidato3)
        Candidato candidato4 = new Candidato("Beatriz", "beatriz@gmail.com", "78912345600", 35, "Rio de Janeiro", "20000000", "Engenheira de Software")
        candidatos.add(candidato4)
    }

    void listarCandidatos() {
        candidatos.each{
            println(it)
        }
    }

    void listarEmpresas() {
        empresas.each{
            println(it)
        }
    }


}
