    package com.jpcode.view

    import com.jpcode.dao.candidato.CandidatoDAO
    import com.jpcode.dao.match.MatchDAO
    import com.jpcode.dao.relacionamento.CandidatoCurtirDAO
    import com.jpcode.dao.relacionamento.EmpresaCurtirDAO
    import com.jpcode.dao.vaga.VagaDAO
    import com.jpcode.dto.candidato.CandidatoAnonimoDTO
    import com.jpcode.dto.vaga.VagaEmpresaDTO
    import com.jpcode.enums.CompetenciasEnum
    import com.jpcode.model.core.Candidato
    import com.jpcode.model.core.Empresa
    import com.jpcode.model.core.Match
    import com.jpcode.model.core.Vaga
    import com.jpcode.service.EmpresaService
    import com.jpcode.service.VagaService
    import com.jpcode.validation.CompetenciaValidation

    class MenuEmpresa {
        final Scanner scanner = new Scanner(System.in)
        final EmpresaService empresaService = new EmpresaService(new CompetenciaValidation())
        final VagaService vagaService = new VagaService()
        final VagaDAO vagaDAO = new VagaDAO()
        final CandidatoCurtirDAO candidatoCurtirDAO = new CandidatoCurtirDAO()
        final EmpresaCurtirDAO empresaCurtirDAO = new EmpresaCurtirDAO()
        final MatchDAO matchDAO = new MatchDAO()
        final CandidatoDAO candidatoDAO = new CandidatoDAO()
        
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
            while (true) {
                scanner.nextLine()
                println("Digite o email da empresa: ")
                String email = scanner.nextLine()
                println("Digite a senha da empresa: ")
                String senha = scanner.nextLine()
                Empresa empresaEncontrada = empresaService.logar(email, senha)
                if (empresaEncontrada) {
                    menuEmpresa(empresaEncontrada)
                } else {
                    println("""
                Email ou Senha incorretos
                
                1 - Tente Novamente
                Qualquer Tecla - Sair
                """)
                    if (scanner.nextLine() != "1") {
                        break
                    }
                }
            }
        }

        private menuEmpresa(Empresa empresa) {
            List<VagaEmpresaDTO> vagasEmpresa = vagaDAO.buscarVagasEmpresa(empresa.id)
            
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
                        println(vagasEmpresa)
                        break
                    case 2:
                        verVagasCandidatos(empresa, vagasEmpresa)
                        break
                    case 3:
                        criarVaga(empresa)
                        break
                    case 4:
                        return
                }
            }
        }

        private verVagasCandidatos(Empresa empresa, List<VagaEmpresaDTO> vagasEmpresa) {
            scanner.nextLine()
            println(vagasEmpresa)
            println("Digite o id da vaga: ")
            Long idVaga = scanner.nextLong()
            Vaga vagaEncontrada = vagaDAO.buscarPorId(idVaga)
            if (vagaEncontrada != null) {
                List<CandidatoAnonimoDTO> candidatosQueCurtiramVaga = candidatoCurtirDAO.buscarCandidatosQueCurtiram(vagaEncontrada.id)
                println("Digite o ID do Candidato: ")
                Long idCandidatoAnonimo = scanner.nextLong()
                Candidato candidatoEncontrado = candidatoDAO.buscacrPorId(idCandidatoAnonimo)
                if (candidatoEncontrado != null) {
                    scanner.nextLine()
                    println("Desja curtir o Candidado s/n?")
                    if (scanner.nextLine().toUpperCase() == "s") {
                        empresaCurtirDAO.salvar(empresa.id, idCandidatoAnonimo)
                        matchDAO.salvar(idCandidatoAnonimo, empresa.id, idVaga)
                    }
                } else {
                    println("Canidadato não encontrado!")
                }
            } else {
                println("Vaga não encontrada!")
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
