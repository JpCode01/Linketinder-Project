    package com.jpcode.view

    import com.jpcode.enums.CompetenciasEnum
    import com.jpcode.model.Candidato
    import com.jpcode.model.Empresa
    import com.jpcode.model.Match
    import com.jpcode.model.Vaga
    import com.jpcode.service.EmpresaService
    import com.jpcode.service.VagaService
    import com.jpcode.validation.CompetenciaValidation

    class MenuEmpresa {
        final Scanner scanner = new Scanner(System.in)
        final EmpresaService empresaService = new EmpresaService(new CompetenciaValidation())
        final VagaService vagaService = new VagaService()
            
        void inicio() {
            println("""
            1 - Cadastrar Empresa
            2 - Acessar empresa por nome
            """)
            switch (scanner.nextInt()) {
                case 1:
                    cadastrarEmpresa()
                    break
                case 2:
                    login()
                    break
                
            }
        }

        private login() {
            scanner.nextLine()
            println("Digite o nome da empresa: ")
            String nome = scanner.nextLine()
            Empresa empresa = Menu.empresas.find {it.nome == nome }
            if (empresa) {
                menuEmpresa(empresa)
            }
        }

        private menuEmpresa(Empresa empresa) {
            while(true) {
                println(empresa)
                println("""
                1 - Ver Vagas
                2 - Ver Candidatos em vagas
                3 - Criar Vaga
                4 - Sair
                """)
                switch (scanner.nextInt()) {
                    case 1:
                        empresaService.ListarVagasPorEmpresa(empresa)
                        break
                    case 2:
                        verVagasCandidatos(empresa)
                        break
                    case 3:
                        criarVaga(empresa)
                        break
                    case 4:
                        return
                }
            }
        }

        private verVagasCandidatos(Empresa empresa) {
            scanner.nextLine()
            empresaService.ListarVagasPorEmpresa(empresa)
            println("Digite o nome da vaga: ")
            String nomeVaga = scanner.nextLine()
            if (empresa.vagas.find {it.nome == nomeVaga} ) {
                Vaga vaga = empresa.vagas.find {it.nome == nomeVaga}
                List candidatos = vaga.candidatosQueCurtiram
                if (candidatos != null) {
                    empresaService.ListarCandidatosPorVaga(vaga)
                    println("Escolha um candidato por id: ")
                    int idCandidato = scanner.nextInt()
                    scanner.nextLine()
                    try {
                        Candidato candidato = candidatos.get(idCandidato)
                        println(candidato.competencias)
                        println("Deseja curtir o candidato(s/n)? ")
                        if (scanner.nextLine().toLowerCase() == "s") {
                            empresaService.curtirCandidato(candidato, empresa)
                            Match match = new Match(empresa, candidato, vaga)
                            println(match)
                        }
                    } catch (IndexOutOfBoundsException ex) {
                        println("ID invalido!")
                    }
                } else {
                    println("Nenhum candidato curtiu a sua vaga até o momento, por favor aguarde!")
                }
            } else {
                println("A Empresa nao possui essa vaga")
            }
        }

        private void cadastrarEmpresa() {
            scanner.nextLine()

            println("Nome Empresa:")
            String nome = scanner.nextLine()

            println("Email:")
            String email = scanner.nextLine()

            println("CNPJ:")
            String cnpj = scanner.nextLine()

            println("País:")
            String pais = scanner.nextLine()

            println("Estado:")
            String estado = scanner.nextLine()

            println("CEP:")
            String cep = scanner.nextLine()

            println("Descrição:")
            String descricao = scanner.nextLine()

            List<String> competencias = capturarCompetencias()

            Empresa empresa = empresaService.cadastrarEmpresa(
                    nome,
                    email,
                    cnpj,
                    pais,
                    estado,
                    cep,
                    descricao,
                    competencias
            )

            Menu.empresas.add(empresa)
        }

        private List<String> capturarCompetencias() {
            List<String> competencias = []

            while (true) {
                println("""
                Competências atuais da empresa: ${competencias}
        
                1 - Digitar nova competência
                2 - Parar
                """)

                if (scanner.nextInt() == 2) {
                    break
                }

                scanner.nextLine()

                println("""
        Competências disponíveis: ${CompetenciasEnum.values() - competencias}
        
        Digite uma competência:
        """)

                competencias.add(scanner.nextLine())
            }

            return competencias
        }

        private void criarVaga(Empresa empresa) {
            scanner.nextLine()

            println("Digite o nome da vaga:")
            String nome = scanner.nextLine()

            println("Digite a descrição da vaga:")
            String descricao = scanner.nextLine()

            Vaga vaga = vagaService.criarVaga(
                    empresa,
                    nome,
                    descricao
            )

            Menu.vagasGerais.add(vaga)
        }
    }
