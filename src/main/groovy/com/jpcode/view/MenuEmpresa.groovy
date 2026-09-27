    package com.jpcode.view

    import com.jpcode.config.DaoConfig

    import com.jpcode.config.ServiceConfig
    import com.jpcode.dto.empresa.AtualizarEmpresaDTO
    import com.jpcode.dto.empresa.CadastrarEmpresaDTO
    import com.jpcode.dto.vaga.AtualizarVagaDTO
    import com.jpcode.dto.vaga.CadastrarVagaDTO
    import com.jpcode.dto.vaga.VagaEmpresaDTO
    import com.jpcode.exception.CompetenciaNaoEncontradaException
    import com.jpcode.exception.EstadoNaoEncontradoException
    import com.jpcode.exception.PaisNaoEncontradoException
    import com.jpcode.model.core.Empresa
    import com.jpcode.model.core.Vaga
    import com.jpcode.service.*

    class MenuEmpresa {
        final Scanner scanner = new Scanner(System.in)
        final DaoConfig daoConfig = new DaoConfig()
        final ServiceConfig serviceConfig = new ServiceConfig(daoConfig)

        final EmpresaService empresaService = serviceConfig.empresaService
        final VagaService vagaService = serviceConfig.vagaService
        final CandidatoService candidatoService = serviceConfig.candidatoService
        final MatchService matchService = serviceConfig.matchService
        final CompetenciaService competenciaService = serviceConfig.competenciaService
        final ReferenciaService referenciaService = serviceConfig.referenciaService

        void inicio() {
            println("""
            1 - Cadastrar Empresa
            2 - Fazer Login
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
                    break
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
            while(true) {
                List<VagaEmpresaDTO> vagasEmpresa = vagaService.listarVagas(empresa.id)
                println(empresa)
                println("""
                1 - Ver Vagas
                2 - Ver Candidatos em vagas
                3 - Criar Vaga
                4 - Ver Candidatos Curtidos
                5 - Desativar Conta
                6 - Apagar vaga
                7 - Atualizar conta 
                8 - Atualizar vaga
                9 - Adicionar competencias em Vaga
                10 - Ver Matches
                11 - Remover competencia
                12 - Sair
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
                        verCandidatosCurtidos(empresa)
                        break
                    case 5:
                        if (apagarEmpresa(empresa)) {
                            return
                        }
                        break
                    case 6:
                        vagasEmpresa =  apagarVaga(vagasEmpresa)
                        break
                    case 7:
                        atualizarEmpresa(empresa)
                        break
                    case 8:
                        atualizarVaga(vagasEmpresa)
                        break
                    case 9:
                        atualizarCompetencias(vagasEmpresa)
                        break
                    case 10:
                        verMatches(empresa)
                        break
                    case 11:
                        removerCompetencia(vagasEmpresa)
                        break
                    case 12:
                        return
                }
            }
        }

        private verCandidatosCurtidos(Empresa empresa) {
            println(empresaService.buscarCandidatosCurtidos(empresa.id))
        }

        private verVagasCandidatos(Empresa empresa, List<VagaEmpresaDTO> vagasEmpresa) {
            scanner.nextLine()
            println(vagasEmpresa)
            println("Digite o id da vaga: ")
            Long idVaga = scanner.nextLong()
            if (vagaService.buscarVaga(idVaga) != null) {
                println(empresaService.buscarCandidatosQueCurtiram(idVaga))
                println("Digite o ID do Candidato: ")
                Long idCandidatoAnonimo = scanner.nextLong()
                if (candidatoService.buscarCandidato(idCandidatoAnonimo) != null) {
                    scanner.nextLine()
                    println("Desja curtir o Candidado s/n?")
                    if (scanner.nextLine().toLowerCase() == "s") {
                        empresaService.curtirCandidato(empresa.id, idCandidatoAnonimo)
                        matchService.salvar(idCandidatoAnonimo, empresa.id, idVaga)
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

            println("Senha: ")
            String senha = scanner.nextLine()

            println("CNPJ:")
            String cnpj = scanner.nextLine()

            println("Pais:")
            String pais = scanner.nextLine()

            println("Estado em sigla (SP/RS/RJ:")
            String estado = scanner.nextLine()

            println("CEP:")
            String cep = scanner.nextLine()

            println("Descricao:")
            String descricao = scanner.nextLine()

            tentarCadastrarEmpresa(new CadastrarEmpresaDTO(
                    nome,
                    email,
                    senha,
                    cnpj,
                    pais,
                    estado,
                    cep,
                    descricao
            ))
        }

        private void tentarCadastrarEmpresa(CadastrarEmpresaDTO cadastrarEmpresaDTO) {
            try {
                empresaService.cadastrarEmpresa(cadastrarEmpresaDTO)
                println("Empresa cadastrada com sucesso!")
            } catch (PaisNaoEncontradoException | EstadoNaoEncontradoException e) {
                println(e.getMessage())
            }
        }

        private List<String> capturarCompetencias(List<String> competencias) {
            List<String> todasCompetencias = competenciaService.listarCompetencias()

            while (true) {
                if (todasCompetencias - competencias == []) {
                    break
                }

                println("""
        Competencias atuais: ${competencias}

        1 - Digitar nova competencia
        2 - Parar
        """)

                if (scanner.nextInt() == 2) {
                    break
                }

                scanner.nextLine()

                println("""
        Competencias disponiveis: ${todasCompetencias - competencias}

        Digite uma competencia:
        """)

                String competencia = scanner.nextLine().trim().toUpperCase()

                if (!competencias.contains(competencia)) {
                    competencias.add(competencia)
                } else {
                    println("Competencia ja existente!")
                }
            }

            return competencias
        }

        private void criarVaga(Empresa empresa) {
            scanner.nextLine()

            println("Digite o nome da vaga:")
            String nome = scanner.nextLine()

            println("Digite a descricao da vaga:")
            String descricao = scanner.nextLine()

            println("Digite a localizacao da vaga: ")
            String local = scanner.nextLine()

            List<String> competencias = capturarCompetencias([])

            tentarCriarVaga(
                    new CadastrarVagaDTO(
                            nome,
                            descricao, local,
                            empresa.id,
                            competencias
                    )
            )
        }

        private void tentarCriarVaga(CadastrarVagaDTO cadastrarVagaDTO) {
            try {
                vagaService.criarVaga(cadastrarVagaDTO)
                println("Vaga cadastrada com sucesso!")
            } catch (CompetenciaNaoEncontradaException e) {
                println(e.getMessage())
            }
        }

        boolean apagarEmpresa(Empresa empresa) {
            scanner.nextLine()
            println("Digite sua senha para confirmar (Caso queira desistir, aperte enter): ")
            if (scanner.nextLine() == empresa.senha) {
                empresaService.desativarEmpresa(empresa.id)
                println("Conta deletada com sucesso")
                return true
            }
            return false
        }

        List<VagaEmpresaDTO> apagarVaga(List<VagaEmpresaDTO> vagas) {
            scanner.nextLine()
            println(vagas)

            println("Digite o ID da vaga desejada: (Enter para cancelar) ")
            String idVaga = scanner.nextLine()

            if (idVaga != "") {
                Long id = Long.parseLong(idVaga)

                vagaService.deletarVaga(id)

                vagas.removeIf { vaga -> vaga.id == id }
            }

            return vagas
        }

        private void atualizarEmpresa(Empresa empresa) {
            scanner.nextLine()


            println("Nome:")
            String nome = scanner.nextLine()
            if (!nome.isEmpty()) {
                empresa.nome = nome
            }

            println("Email:")
            String email = scanner.nextLine()
            if (!email.isEmpty()) {
                empresa.email = email
            }

            println("Senha:")
            String senha = scanner.nextLine()
            if (!senha.isEmpty()) {
                empresa.senha = senha
            }

            println("CNPJ:")
            String cnpj = scanner.nextLine()
            if (!cnpj.isEmpty()) {
                empresa.cnpj = cnpj
            }

            println("Pais:")
            String pais = scanner.nextLine()


            if (pais.isEmpty()) {
                pais = referenciaService.converterIdPaisParaString(empresa.idPais)
            }

            println("Estado em sigla (SP/RS/RJ):")
            String estado = scanner.nextLine()

            if (estado.isEmpty()) {
                estado = referenciaService.converterIdEstadoParaString(empresa.idEstado)
            }

            println("CEP:")
            String cep = scanner.nextLine()
            if (!cep.isEmpty()) {
                empresa.cep = cep
            }

            println("Descrição:")
            String descricao = scanner.nextLine()
            if (!descricao.isEmpty()) {
                empresa.descricao = descricao
            }

            tentarAtualizarEmpresa(
                    new AtualizarEmpresaDTO(
                            empresa.id,
                            empresa.nome,
                            empresa.email,
                            empresa.senha,
                            empresa.cnpj,
                            pais,
                            estado,
                            empresa.cep,
                            empresa.descricao,
                            empresa.ativo
                    )
            )
            
        }

        private void tentarAtualizarEmpresa(AtualizarEmpresaDTO atualizarEmpresaDTO) {
            try {
                empresaService.atualizarEmpresa(atualizarEmpresaDTO)
                println("Empresa atualizada com sucesso!")
            } catch (PaisNaoEncontradoException | EstadoNaoEncontradoException e) {
                println(e.getMessage())
            }
        }

        void atualizarVaga(List<VagaEmpresaDTO> vagas) {
            println(vagas)
            println("Digite o id da vaga: ")
            Long idVaga = scanner.nextLong()
            if (vagaService.buscarVaga(idVaga) != null) {
                Vaga vagaEncontrada = vagaService.buscarVaga(idVaga)
                scanner.nextLine()
                println("Nome:")
                String nome = scanner.nextLine()
                if (!nome.isEmpty()) {
                    vagaEncontrada.nome = nome
                }

                println("Descrição:")
                String descricao = scanner.nextLine()
                if (!descricao.isEmpty()) {
                    vagaEncontrada.descricao = descricao
                }

                println("Local:")
                String local = scanner.nextLine()
                if (!local.isEmpty()) {
                    vagaEncontrada.local = local
                }

                vagaService.atualizarVaga(
                        new AtualizarVagaDTO(
                                vagaEncontrada.id,
                                vagaEncontrada.nome,
                                vagaEncontrada.descricao,
                                vagaEncontrada.local,
                                vagaEncontrada.idEmpresa
                        )
                )
            } else {
                println("Vaga não encontrada!")
            }
        }

        void atualizarCompetencias(List<VagaEmpresaDTO> vagasEmpresa) {
            println(vagasEmpresa)
            println("Digite o id da vaga: ")
            Long idVaga = scanner.nextLong()
            if (vagaService.buscarVaga(idVaga) != null) {
                List<String> competencias = vagaService.competenciasEmString(idVaga)
                vagaService.adicionarCompetencias(idVaga, capturarCompetencias(competencias))
            } else {
                println("Vaga não encontrada!")
            }
        }

        void verMatches(Empresa empresa) {
            println(matchService.verMatchesPorEmpresa(empresa.id))
        }

        void removerCompetencia(List<VagaEmpresaDTO> vagasEmpresa) {
            println(vagasEmpresa)
            println("Digite o id da vaga: ")
            Long idVaga = scanner.nextLong()
            if (vagaService.buscarVaga(idVaga) != null) {
                println(empresaService.listaParaRemover(idVaga))
                println("Digite o ID da competencia: ")
                Long idCompetencia = scanner.nextLong()
                if (competenciaService.buscarCompetencia(idCompetencia) != null) {
                    empresaService.removerCompetencia(idVaga, idCompetencia)
                }
            }

        }
    }
