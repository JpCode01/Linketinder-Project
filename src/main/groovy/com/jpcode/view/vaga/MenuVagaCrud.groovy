package com.jpcode.view.vaga

import com.jpcode.dto.competencia.RemoverCompetenciaDTO
import com.jpcode.dto.vaga.AtualizarVagaDTO
import com.jpcode.dto.vaga.CadastrarVagaDTO
import com.jpcode.dto.vaga.VagaEmpresaDTO
import com.jpcode.exception.referencia.CompetenciaNaoEncontradaException
import com.jpcode.exception.vaga.VagaNaoEncontradaException
import com.jpcode.model.core.Vaga
import com.jpcode.service.CompetenciaService
import com.jpcode.service.EmpresaService
import com.jpcode.service.VagaService
import com.jpcode.view.referencia.MenuCompetencia

class MenuVagaCrud {

    final Scanner scanner
    final VagaService vagaService
    final CompetenciaService competenciaService
    final EmpresaService empresaService
    
    final MenuCompetencia menuCompetencia = new MenuCompetencia(scanner, competenciaService)

    MenuVagaCrud(Scanner scanner, VagaService vagaService, CompetenciaService competenciaService, EmpresaService empresaService) {
        this.scanner = scanner
        this.vagaService = vagaService
        this.competenciaService = competenciaService
        this.empresaService = empresaService
    }

    void criarVaga(Long idEmpresa) {
        CadastrarVagaDTO vaga = capturarDadosCriarVaga(idEmpresa)
        tentarCriarVaga(vaga)
    }

    void atualizarVaga(Long idEmpresa) {
        escolherVagaParaAtualizar(idEmpresa)
    }

    List<VagaEmpresaDTO> verVagasEmpresa(Long idEmpresa) {
        return vagaService.listarVagas(idEmpresa)
    }

    void apagarVaga(Long idEmpresa) {
        List<VagaEmpresaDTO> vagasEmpresa = verVagasEmpresa(idEmpresa)
        println(vagasEmpresa)
        
        Long idVaga = solicitarId("Digite o ID da vaga:")
        tentarDeletarVaga(idVaga)
    }

    void atualizarCompetencias(Long idEmpresa) {
        List<VagaEmpresaDTO> vagasEmpresa = verVagasEmpresa(idEmpresa)
        println(vagasEmpresa)

        try {
            Long idVaga = solicitarId("Digite o ID da vaga desejada: ")

            if (!VerificaSeExisteVagaNaLista(idVaga, vagasEmpresa)) {
                println("Vaga de ID ${idVaga} não encontrada!")
                return
            }

            List<String> competencias = competenciasVaga(idVaga)
            vagaService.adicionarCompetencias(idVaga, menuCompetencia.capturarCompetencias(competencias))

            println("Competencias da vaga atualizada com sucesso!")
        } catch (InputMismatchException e) {
            scanner.nextLine()
            println("Entrada inválida! Digite um ID numérico." + e.getMessage())
        }
    }

    void escolherVagaParaRemoverCompetencia(Long idEmpresa) {
        List<VagaEmpresaDTO> vagasEmpresa = verVagasEmpresa(idEmpresa)
        println(vagasEmpresa)

        try {
            Long idVaga = solicitarId("Digite o ID da vaga: ")

            verificarSeVagaExiste(idVaga, vagasEmpresa)
            removerCompetencia(idVaga)

        } catch (VagaNaoEncontradaException e) {
            println(e.getMessage())
        }
    }

    private void listarCompetenciasVaga(Long idVaga) {
        println(vagaService.buscarCompetenciasDeVaga(idVaga))
    }

    private void removerCompetencia(Long idVaga) {
        listarCompetenciasVaga(idVaga)
        
        try {
            Long idCompetencia = solicitarId("Digite o ID da competencia: ")
            List<RemoverCompetenciaDTO> competenciasVaga = empresaService.listaParaRemover(idVaga)

            tentarRemoverCompetencia(idCompetencia, idVaga, competenciasVaga)
        } catch (CompetenciaNaoEncontradaException e) {
            println(e.getMessage())
        }
    }

    private void tentarRemoverCompetencia(Long idCompetencia, Long idVaga,List<RemoverCompetenciaDTO> competenciasVaga) {
        if (!verificaSeExisteCompetenciaNaLista(idCompetencia, competenciasVaga)) {
            throw new CompetenciaNaoEncontradaException(idCompetencia)
        }

        empresaService.removerCompetencia(idVaga, idCompetencia)
    }

    private boolean verificaSeExisteCompetenciaNaLista(Long idCompetencia, List<RemoverCompetenciaDTO> competenciasVaga) {
        return competenciasVaga.any {
            RemoverCompetenciaDTO competencia -> competencia.id == idCompetencia
        }
    }

    private void tentarDeletarVaga(Long idVaga) {
        try {
            vagaService.deletarVaga(idVaga)
            println("Vaga deletada com sucesso!")
        } catch (VagaNaoEncontradaException e) {
            e.getMessage()
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

    private escolherVagaParaAtualizar(Long idEmpresa) {
        List<VagaEmpresaDTO> vagasEmpresa = verVagasEmpresa(idEmpresa)
        println(vagasEmpresa)

        try {
            Long idVaga = solicitarId("Digite o ID da vaga: ")

            verificarSeVagaExiste(idVaga, vagasEmpresa)
            Vaga vagaEncontrada = buscarVaga(idVaga)
            capturarDadosAtualizarVaga(vagaEncontrada)

        } catch (VagaNaoEncontradaException e) {
            println(e.getMessage())
        }
    }



    private void verificarSeVagaExiste(Long idVaga, List<VagaEmpresaDTO> vagasDisponiveis) {
        boolean vagaExiste = vagasDisponiveis.any { VagaEmpresaDTO vaga ->
            vaga.id == idVaga
        }

        if (!vagaExiste) {
            throw new VagaNaoEncontradaException(idVaga)
        }
    }

    private AtualizarVagaDTO capturarDadosAtualizarVaga(Vaga vagaEncontrada) {
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

        return new AtualizarVagaDTO(
                vagaEncontrada.id,
                vagaEncontrada.nome,
                vagaEncontrada.descricao,
                vagaEncontrada.local,
                vagaEncontrada.idEmpresa
        )
    }

    private Vaga buscarVaga(Long idVaga) {
        return vagaService.buscarVaga(idVaga)
    }

    private CadastrarVagaDTO capturarDadosCriarVaga(Long idEmpresa) {
        println("Digite o nome da vaga:")
        String nome = scanner.nextLine()

        println("Digite a descricao da vaga:")
        String descricao = scanner.nextLine()

        println("Digite a localizacao da vaga: ")
        String local = scanner.nextLine()

        List<String> competencias = menuCompetencia.capturarCompetencias([])

        return new CadastrarVagaDTO(
                nome,
                descricao,
                local,
                idEmpresa,
                competencias
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

    private List<String> competenciasVaga(Long idVaga) {
        return vagaService.competenciasEmString(idVaga)
    }

    private boolean VerificaSeExisteVagaNaLista(Long idVaga, List<VagaEmpresaDTO> vagasEmpresa) {
        return vagasEmpresa.any {
            VagaEmpresaDTO vaga -> vaga.id == idVaga
        }
    }
}
