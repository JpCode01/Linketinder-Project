package com.jpcode.service

import com.jpcode.dao.contratos.AreaRecrutamentoRepository
import com.jpcode.dao.contratos.RecrutadorRepository
import com.jpcode.dao.contratos.UsuarioRepository
import com.jpcode.dto.recrutador.CadastrarRecrutadorDTO
import com.jpcode.dto.recrutador.RecrutadorViewDTO
import com.jpcode.model.core.Recrutador
import com.jpcode.model.referencia.AreaRecrutamento

class RecrutadorService {
    private final RecrutadorRepository recrutadorRepository
    private final AreaRecrutamentoRepository areaRepository
    private final UsuarioRepository empresaRepository
    private final SenhaService senhaService

    RecrutadorService(RecrutadorRepository recrutadorRepository, AreaRecrutamentoRepository areaRepository,
                     UsuarioRepository empresaRepository, SenhaService senhaService = new SenhaService()) {
        this.recrutadorRepository = recrutadorRepository
        this.areaRepository = areaRepository
        this.empresaRepository = empresaRepository
        this.senhaService = senhaService
    }

    // idEmpresaOperante será fornecido pelo contexto empresarial HTTP, nunca pelo DTO.
    RecrutadorViewDTO cadastrarRecrutador(Long idEmpresaOperante, CadastrarRecrutadorDTO dados) {
        def empresa = empresaRepository.buscarPorId(idEmpresaOperante)
                .orElseThrow(() -> new IllegalArgumentException('Empresa inexistente ou inativa.'))
        if (!empresa.ativo) throw new IllegalArgumentException('Empresa inexistente ou inativa.')
        if (dados.idAreaRecrutamento == null) throw new IllegalArgumentException('Área de recrutamento obrigatória.')
        AreaRecrutamento area = areaRepository.buscarPorId(dados.idAreaRecrutamento)
                .orElseThrow(() -> new IllegalArgumentException('Área de recrutamento inexistente.'))
        Recrutador recrutador = new Recrutador(nome: dados.nome, cpf: dados.cpf, email: dados.email,
                dataNascimento: dados.dataNascimento, descricao: dados.descricao,
                senha: senhaService.derivar(dados.senha), idAreaRecrutamento: area.id,
                idEmpresa: empresa.id, ativo: true)
        return new RecrutadorViewDTO(recrutadorRepository.salvar(recrutador), area)
    }

    RecrutadorViewDTO buscarRecrutador(Long idRecrutadorOperante, Long idRecrutador) {
        if (idRecrutadorOperante == null || idRecrutadorOperante != idRecrutador) {
            throw new IllegalArgumentException('Consulta restrita ao próprio recrutador.')
        }
        Recrutador recrutador = recrutadorRepository.buscarPorId(idRecrutador)
                .orElseThrow(() -> new IllegalArgumentException('Recrutador inexistente.'))
        if (!recrutador.ativo) throw new IllegalStateException('Recrutador inativo.')
        return new RecrutadorViewDTO(recrutador, areaRepository.buscarPorId(recrutador.idAreaRecrutamento).orElseThrow())
    }

    List<AreaRecrutamento> buscarAreas() {
        return areaRepository.buscar()
    }
}
