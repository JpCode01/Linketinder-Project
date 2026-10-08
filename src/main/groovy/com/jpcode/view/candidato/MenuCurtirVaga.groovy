package com.jpcode.view.candidato

import com.jpcode.dto.vaga.VagaAnonimaDTO
import com.jpcode.exception.vaga.SemVagasDisponiveisException
import com.jpcode.exception.vaga.VagaNaoEncontradaException
import com.jpcode.controller.vaga.VagaController

class MenuCurtirVaga {

    final Scanner scanner
    final VagaController vagaController

    MenuCurtirVaga(Scanner scanner, VagaController vagaController) {
        this.scanner = scanner
        this.vagaController = vagaController
    }

    List<VagaAnonimaDTO> vagasAnonimasCurtidas(Long idCandidato) {
        return vagaController.listarVagasCurtidas(idCandidato)
    }

    List<VagaAnonimaDTO> vagasDisponiveisParaCandidato(Long idCandidato) {
        return vagaController.buscarTodasAsVagas() - vagasAnonimasCurtidas(idCandidato)
    }

    void curtirVaga(Long idCandidato) {
        List<VagaAnonimaDTO> vagasDisponiveis = vagasDisponiveisParaCandidato(idCandidato)
        println(vagasDisponiveis)

        try {
            verificaSeHaVagasDisponiveis(vagasDisponiveis)
            Long idVaga = solicitarId("Digite o ID da vaga: ")

            tentarCurtirVaga(idVaga, idCandidato, vagasDisponiveis)
        } catch (VagaNaoEncontradaException e) {
            println(e.getMessage())
        } catch (SemVagasDisponiveisException e) {
            println(e.getMessage())
        }
    }

    private void tentarCurtirVaga(Long idVaga, Long idCandidato, List<VagaAnonimaDTO> vagasDisponiveis) {
        if (!verificaSeExisteVaga(idVaga, vagasDisponiveis)) {
            throw new VagaNaoEncontradaException(idVaga)
        }

        vagaController.curtir(idCandidato, idVaga)
        println("Vaga Curtida com sucesso!")
    }

    private boolean verificaSeExisteVaga(Long idVaga, List<VagaAnonimaDTO> vagasDisponiveis) {
        return vagasDisponiveis.any {
            VagaAnonimaDTO vaga -> vaga.id == idVaga
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

    private void verificaSeHaVagasDisponiveis(List<VagaAnonimaDTO> vagasDisponiveis) {
        if (vagasDisponiveis.isEmpty()) {
            throw new SemVagasDisponiveisException()
        }
    }
}
