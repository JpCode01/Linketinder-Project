package com.jpcode.view.empresa

import com.jpcode.dto.empresa.AtualizarEmpresaDTO
import com.jpcode.dto.empresa.CadastrarEmpresaDTO
import com.jpcode.exception.referencia.EstadoNaoEncontradoException
import com.jpcode.exception.referencia.PaisNaoEncontradoException
import com.jpcode.model.core.Empresa
import com.jpcode.controller.empresa.EmpresaController
import com.jpcode.controller.match.MatchController
import com.jpcode.controller.referencia.ReferenciaController

class MenuEmpresaCrud {

    final Scanner scanner
    final EmpresaController empresaController
    final ReferenciaController referenciaController
    final MatchController matchController

    MenuEmpresaCrud(Scanner scanner, EmpresaController empresaController, ReferenciaController referenciaController, MatchController matchController) {
        this.scanner = scanner
        this.empresaController = empresaController
        this.referenciaController = referenciaController
        this.matchController = matchController
    }

    void cadastrarEmpresa() {
        CadastrarEmpresaDTO empresa = capturarDadosCadastrar()
        tentarCadastrarEmpresa(empresa)
    }

    void atualizarEmpresa(Empresa empresa) {
        AtualizarEmpresaDTO empresaAtualizada = capturarDadosAtualizarEmpresa(empresa)
        tentarAtualizarEmpresa(empresaAtualizada)
    }

    boolean apagarEmpresa(Long idEmpresa, String senhaEmpresa) {
        println("Digite sua senha para confirmar (Caso queira desistir, aperte enter): ")
        if (scanner.nextLine() == senhaEmpresa) {
            empresaController.desativarEmpresa(idEmpresa)
            println("Conta deletada com sucesso")
            return true
        }
        return false
    }

    void exibirEmpresaInicio(Empresa empresa) {
        println(empresa)
    }

    void verMatches(Long idEmpresa) {
        println(matchController.verMatchesPorEmpresa(idEmpresa))
    }

    private CadastrarEmpresaDTO capturarDadosCadastrar() {
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

        return new CadastrarEmpresaDTO(
                nome,
                email,
                senha,
                cnpj,
                pais,
                estado,
                cep,
                descricao
        )
    }


    private void tentarCadastrarEmpresa(CadastrarEmpresaDTO cadastrarEmpresaDTO) {
        try {
            empresaController.cadastrarEmpresa(cadastrarEmpresaDTO)
            println("Empresa cadastrada com sucesso!")
        } catch (PaisNaoEncontradoException | EstadoNaoEncontradoException e) {
            println(e.getMessage())
        }
    }

    private AtualizarEmpresaDTO capturarDadosAtualizarEmpresa(Empresa empresa) {
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
            pais = referenciaController.converterIdPaisParaString(empresa.idPais)
        }

        println("Estado em sigla (SP/RS/RJ):")
        String estado = scanner.nextLine()

        if (estado.isEmpty()) {
            estado = referenciaController.converterIdEstadoParaString(empresa.idEstado)
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

        return new AtualizarEmpresaDTO(
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
    }

    private void tentarAtualizarEmpresa(AtualizarEmpresaDTO atualizarEmpresaDTO) {
        try {
            empresaController.atualizarEmpresa(atualizarEmpresaDTO)
            println("Empresa atualizada com sucesso!")
        } catch (PaisNaoEncontradoException | EstadoNaoEncontradoException e) {
            println(e.getMessage())
        }
    }
    
}
