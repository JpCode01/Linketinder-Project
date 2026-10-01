    package com.jpcode.view

    import com.jpcode.config.DaoConfig
    import com.jpcode.config.ServiceConfig
    import com.jpcode.dto.candidato.CandidatoAnonimoDTO
    import com.jpcode.dto.competencia.RemoverCompetenciaDTO
    import com.jpcode.dto.empresa.AtualizarEmpresaDTO
    import com.jpcode.dto.empresa.CadastrarEmpresaDTO
    import com.jpcode.dto.vaga.AtualizarVagaDTO
    import com.jpcode.dto.vaga.CadastrarVagaDTO
    import com.jpcode.dto.vaga.VagaEmpresaDTO
    import com.jpcode.exception.referencia.CompetenciaNaoEncontradaException
    import com.jpcode.exception.empresa.EmpresaLoginException
    import com.jpcode.exception.referencia.EstadoNaoEncontradoException
    import com.jpcode.exception.referencia.PaisNaoEncontradoException
    import com.jpcode.exception.referencia.VagaNaoEncontradaException
    import com.jpcode.model.core.Empresa
    import com.jpcode.model.core.Vaga
    import com.jpcode.service.*

    class MenuEmpresa {
        final Scanner scanner = new Scanner(System.in)
        final DaoConfig daoConfig = new DaoConfig()
        final ServiceConfig serviceConfig = new ServiceConfig(daoConfig)

        final EmpresaService empresaService = serviceConfig.empresaService
        final VagaService vagaService = serviceConfig.vagaService
        final MatchService matchService = serviceConfig.matchService
        final CompetenciaService competenciaService = serviceConfig.competenciaService
        final ReferenciaService referenciaService = serviceConfig.referenciaService

        void inicio() {
            int opcao = capturarEscolha("""
            1 - Cadastrar Empresa
            2 - Fazer Login
            """)
            switch (opcao) {
                case 1:
                    cadastrarEmpresa()
                    break
                case 2:
                    login()
                    break
                
            }
        }

        private int capturarEscolha(String mensagem) {
            while (true) {
                println(mensagem)
                String opcaoUsuario = scanner.nextLine()
                try {
                    return Integer.parseInt(opcaoUsuario)
                } catch (NumberFormatException e) {
                    println("Erro, Digite uma opção númerica inteira!")
                }
            }
        }

        private login() {
                println("Digite o email da empresa: ")
                String email = scanner.nextLine()
                println("Digite a senha da empresa: ")
                String senha = scanner.nextLine()
                tentarLogarEmpresa(email, senha)
            }

        private void tentarLogarEmpresa(String email, String senha) {
            try {
                Empresa empresaEncontrada = empresaService.logar(email, senha)
                println("Empresa Logada com sucesso!")
                menuEmpresa(empresaEncontrada)
            } catch (EmpresaLoginException e) {
                println(e.getMessage())
            }
            
        }

        private menuEmpresa(Empresa empresa) {
            while(true) {
                println(empresa)
                int escolha = capturarEscolha("""
                1 - Ver Vagas
                2 - Curtir Candidatos em Vaga
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
                switch (escolha) {
                    case 1:
                        println(verVagasEmpresa(empresa.id))
                        break
                    case 2:
                        escolherCandidatoParaCurtir(empresa.id)
                        break
                    case 3:
                        criarVaga(empresa.id)
                        break
                    case 4:
                        verCandidatosCurtidos(empresa.id)
                        break
                    case 5:
                        if (apagarEmpresa(empresa.id, empresa.senha)) {
                            return
                        }
                        break
                    case 6:
                        apagarVaga(empresa.id)
                        break
                    case 7:
                        atualizarEmpresa(empresa)
                        break
                    case 8:
                        atualizarVaga(empresa.id)
                        break
                    case 9:
                        atualizarCompetencias(empresa.id)
                        break
                    case 10:
                        verMatches(empresa.id)
                        break
                    case 11:
                        removerCompetencia(empresa.id)
                        break
                    case 12:
                        return
                }
            }
        }

        private verCandidatosCurtidos(Long idEmpresa) {
            println(empresaService.buscarCandidatosCurtidos(idEmpresa))
        }

        private List<VagaEmpresaDTO> verVagasEmpresa(Long idEmpresa) {
            return vagaService.listarVagas(idEmpresa)
        }

        private boolean confirmarCurtida() {
            println("Deseja curtir o candidato? (s/n)")
            String resposta = scanner.nextLine().trim().toLowerCase()

            return resposta == "s"
        }

        private boolean VerificaSeExisteVagaNaLista(Long idVaga, List<VagaEmpresaDTO> vagasEmpresa) {
            return vagasEmpresa.any {
                VagaEmpresaDTO vaga -> vaga.id == idVaga
            }
        }

        private void escolherCandidatoParaCurtir(Long idEmpresa) {
            scanner.nextLine()
            List<VagaEmpresaDTO> vagasEmpresa = verVagasEmpresa(idEmpresa)
            println(vagasEmpresa)

            try {
                Long idVaga = solicitarId("Digite o ID da vaga:")

                if (!VerificaSeExisteVagaNaLista(idVaga, vagasEmpresa)) {
                    println("Vaga de ID ${idVaga} não encontrada!")
                    return
                }
                
                List<CandidatoAnonimoDTO> candidatosQueCurtiram = empresaService.buscarCandidatosQueCurtiram(idVaga)
                println(candidatosQueCurtiram)
                
                Long idCandidato = solicitarId("Digite o ID do candidato:")

                if (!tentarProcurarCandidato(idCandidato, candidatosQueCurtiram)) {
                    println("Candidato de ID ${idCandidato} não encontrado!")
                    return
                }

                if (confirmarCurtida()) {
                    curtirCandidato(idCandidato, idEmpresa, idVaga)
                }
            } catch (InputMismatchException e) {
                scanner.nextLine()
                println("Entrada inválida! Digite um ID numérico." + e.getMessage())
            }
        }

        private Long solicitarId(String mensagem) {
            while (true) {
                println(mensagem)
                String idEscolhido = scanner.nextLine()
                try {
                    return Long.parseLong(idEscolhido)
                } catch (NumberFormatException e) {
                    println("Erro, digite um ID númerico!")
                }
            }
        }

        private void curtirCandidato(
                Long idCandidato,
                Long idEmpresa,
                Long idVaga) {

            empresaService.curtirCandidato(idEmpresa, idCandidato)
            matchService.salvar(idCandidato, idEmpresa, idVaga)

            println("Curtida registrada com sucesso!")
        }

        private boolean tentarProcurarCandidato(Long idCandidato, List<CandidatoAnonimoDTO> candidatosEncontrados) {
            return candidatosEncontrados.any {
                CandidatoAnonimoDTO candidatoAnonimoDTO -> candidatoAnonimoDTO.id == idCandidato
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

        private void criarVaga(Long idEmpresa) {
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
                            descricao,
                            local,
                            idEmpresa,
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

        boolean apagarEmpresa(Long idEmpresa, String senhaEmpresa) {
            println("Digite sua senha para confirmar (Caso queira desistir, aperte enter): ")
            if (scanner.nextLine() == senhaEmpresa) {
                empresaService.desativarEmpresa(idEmpresa)
                println("Conta deletada com sucesso")
                return true
            }
            return false
        }

        void tentarDeletarVaga(Long idVaga) {
            try {
                vagaService.deletarVaga(idVaga)
                println("Vaga deletada com sucesso!")
            } catch (VagaNaoEncontradaException e) {
                e.getMessage()
            }
        }

        void apagarVaga(Long idEmpresa) {
            List<VagaEmpresaDTO> vagasEmpresa = verVagasEmpresa(idEmpresa)
            println(vagasEmpresa)

            try {
                Long idVaga = solicitarId("Digite o ID da vaga:")
                tentarDeletarVaga(idVaga)
            } catch (InputMismatchException e) {
                println("Entrada inválida! Digite um ID numérico." + e.getMessage())
            }
        }

        private void atualizarEmpresa(Empresa empresa) {
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

        void atualizarVaga(Long idEmpresa) {
            List<VagaEmpresaDTO> vagasEmpresa = verVagasEmpresa(idEmpresa)
            println(vagasEmpresa)

            try {
                Long idVaga = solicitarId("Digite o ID da vaga: ")


                if (!VerificaSeExisteVagaNaLista(idVaga, vagasEmpresa)) {
                        println("Vaga de ID ${idVaga} não encontrada!")
                        return
                }

                Vaga vagaEncontrada = vagaService.buscarVaga(idVaga)
                
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

            } catch (InputMismatchException e) {
                println("Entrada inválida! Digite um ID numérico." + e.getMessage())
            }
        }

        void atualizarCompetencias(Long idEmpresa) {
            List<VagaEmpresaDTO> vagasEmpresa = verVagasEmpresa(idEmpresa)
            println(vagasEmpresa)

            try {
                Long idVaga = capturarEscolha("Digite o ID da vaga desejada: ")

                if (!VerificaSeExisteVagaNaLista(idVaga, vagasEmpresa)) {
                    println("Vaga de ID ${idVaga} não encontrada!")
                    return
                }

                List<String> competencias = vagaService.competenciasEmString(idVaga)
                vagaService.adicionarCompetencias(idVaga, capturarCompetencias(competencias))

                println("Competencias da vaga atualizada com sucesso!")
            } catch (InputMismatchException e) {
                scanner.nextLine()
                println("Entrada inválida! Digite um ID numérico." + e.getMessage())
            }
        }

        void verMatches(Long idEmpresa) {
            println(matchService.verMatchesPorEmpresa(idEmpresa))
        }

        void removerCompetencia(Long idEmpresa) {
            List<VagaEmpresaDTO> vagasEmpresa = verVagasEmpresa(idEmpresa)
            println(vagasEmpresa)

            try {
                Long idVaga = solicitarId("Digite o ID da vaga: ")


                if (!VerificaSeExisteVagaNaLista(idVaga, vagasEmpresa)) {
                    println("Vaga de ID ${idVaga} não encontrada!")
                    return
                }

                println(empresaService.listaParaRemover(idVaga))
                println("Digite o ID da competencia: ")
                Long idCompetencia = scanner.nextLong()

                List<RemoverCompetenciaDTO> competenciasVaga = empresaService.listaParaRemover(idVaga)

                if (!verificaSeExisteCompetenciaNaLista(idCompetencia, competenciasVaga)) {
                    println("Competencia de ID ${idCompetencia} não encontrada na vaga de ID ${idVaga}!")
                    return
                }

                empresaService.removerCompetencia(idVaga, idCompetencia)

            } catch (InputMismatchException e) {
                println("Entrada inválida! Digite um ID numérico." + e.getMessage())
            }
        }

        private boolean verificaSeExisteCompetenciaNaLista(Long idCompetencia, List<RemoverCompetenciaDTO> competenciasVaga) {
            return competenciasVaga.any {
                RemoverCompetenciaDTO competencia -> competencia.id == idCompetencia
            }
        }
    }
