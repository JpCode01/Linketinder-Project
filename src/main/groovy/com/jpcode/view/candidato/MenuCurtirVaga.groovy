package com.jpcode.view.candidato

import com.jpcode.dto.vaga.VagaAnonimaDTO
import com.jpcode.exception.vaga.SemVagasDisponiveisException
import com.jpcode.exception.vaga.VagaNaoEncontradaException
import com.jpcode.service.VagaService

class MenuCurtirVaga {

    final Scanner scanner
    final VagaService vagaService

    MenuCurtirVaga(Scanner scanner, VagaService vagaService) {
        this.scanner = scanner
        this.vagaService = vagaService
    }

    List<VagaAnonimaDTO> vagasAnonimasCurtidas(Long idCandidato) {
        return vagaService.listarVagasCurtidas(idCandidato)
    }

    List<VagaAnonimaDTO> vagasDisponiveisParaCandidato(Long idCandidato) {
        return vagaService.buscarTodasAsVagas() - vagasAnonimasCurtidas(idCandidato)
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

        vagaService.curtir(idCandidato, idVaga)
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
