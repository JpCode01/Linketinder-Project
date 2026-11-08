package com.jpcode.integracao

import com.jpcode.dao.candidato.CandidatoDAO
import com.jpcode.dao.candidato.CompetenciasCandidatoDAO
import com.jpcode.dao.empresa.EmpresaDAO
import com.jpcode.dao.referencia.CompetenciaDAO
import com.jpcode.dao.referencia.EstadoDAO
import com.jpcode.dao.referencia.PaisDAO
import com.jpcode.database.ConnectionFactory
import com.jpcode.dto.candidato.AtualizarCandidatoDTO
import com.jpcode.dto.candidato.CadastrarCandidatoDTO
import com.jpcode.dto.empresa.AtualizarEmpresaDTO
import com.jpcode.dto.empresa.CadastrarEmpresaDTO
import com.jpcode.service.CandidatoService
import com.jpcode.service.EmpresaService
import com.jpcode.service.SenhaService
import groovy.json.JsonOutput
import spock.lang.Specification

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Proxy
import java.sql.Connection
import java.time.LocalDate

import static org.junit.jupiter.api.Assumptions.assumeTrue

/** T05: serviços e DAOs reais, somente no banco/schema isolado de SuportePostgres. */
class SenhaPersistenciaTest extends Specification {
    Connection conexao
    CandidatoDAO candidatoDAO
    EmpresaDAO empresaDAO
    CandidatoService candidatoService
    EmpresaService empresaService
    SenhaService senhaService = new SenhaService()

    def setup() {
        assumeTrue(SuportePostgres.configurado(), 'Integração NÃO EXECUTADA: configure o banco dedicado de testes.')
        conexao = SuportePostgres.abrir()
        SuportePostgres.criarSchemaAtual(conexao)
        conexao.createStatement().withCloseable { comando ->
            comando.execute("INSERT INTO pais (nome) VALUES ('BRASIL'); INSERT INTO estados (sigla) VALUES ('SP')")
        }
        // DAOs fecham a conexão recebida. Este proxy só adia close para cleanup,
        // mantendo todas as operações reais na transação descartável do teste.
        Connection emprestada = (Connection) Proxy.newProxyInstance(Connection.classLoader,
                [Connection] as Class[], { Object proxy, java.lang.reflect.Method metodo, Object[] argumentos ->
            if (metodo.name == 'close') return null
            try {
                return metodo.invoke(conexao, argumentos)
            } catch (InvocationTargetException erro) {
                throw erro.cause
            }
        } as java.lang.reflect.InvocationHandler)
        ConnectionFactory factory = Stub() { getConnection() >> emprestada }
        candidatoDAO = new CandidatoDAO(factory)
        empresaDAO = new EmpresaDAO(factory)
        def pais = new PaisDAO(factory)
        def estado = new EstadoDAO(factory)
        def competencia = new CompetenciaDAO(factory)
        candidatoService = new CandidatoService(pais, estado, competencia, competencia,
                new CompetenciasCandidatoDAO(factory), candidatoDAO, senhaService)
        empresaService = new EmpresaService(pais, estado, empresaDAO, null, null, null, senhaService)
    }

    def cleanup() {
        SuportePostgres.fechar(conexao)
    }

    def 'cadastro atualizacao e login usam hash real para #perfil'() {
        given:
        def service = servico(perfil)
        def dao = repositorio(perfil)
        cadastrar(perfil)
        def conta = dao.buscarAtivoPorEmail(email(perfil)).orElseThrow()
        String anterior = conta.senha

        expect:
        conta.ativo
        anterior != 'senha inicial'
        senhaService.verificar('senha inicial', anterior)
        service.logar(email(perfil), 'senha inicial').id == conta.id

        when:
        atualizar(perfil, conta.id)
        def atualizada = dao.buscarPorId(conta.id).orElseThrow()
        String json = JsonOutput.toJson(perfil == 'candidato' ?
                candidatoService.buscarCandidato(conta.id) : empresaService.buscarEmpresa(conta.id))

        then:
        atualizada.ativo // DTO falso não modifica o ciclo de vida.
        atualizada.senha != anterior
        senhaService.verificar('senha nova', atualizada.senha)
        !senhaService.verificar('senha inicial', atualizada.senha)
        service.logar(email(perfil), 'senha nova').id == conta.id
        !json.contains('senha') && !json.contains('salt') && !json.contains('hash')
        !json.contains(atualizada.senha)

        when:
        service.logar(email(perfil), 'senha inicial')

        then:
        RuntimeException erro = thrown()
        erro.message.startsWith('Credenciais inválidas')

        where:
        perfil << ['candidato', 'empresa']
    }

    def 'inatividade entre leitura e escrita impede UPDATE real de #perfil'() {
        given:
        def dao = repositorio(perfil)
        cadastrar(perfil)
        def conta = dao.buscarAtivoPorEmail(email(perfil)).orElseThrow()
        String anterior = conta.senha
        dao.desativar(conta.id)
        conta.senha = senhaService.derivar('tentativa de troca')
        conta.ativo = true

        when:
        dao.atualizarDados(conta)

        then:
        thrown(IllegalStateException)
        !dao.buscarPorId(conta.id).orElseThrow().ativo
        dao.buscarPorId(conta.id).orElseThrow().senha == anterior
        !dao.buscarAtivoPorEmail(email(perfil)).present

        when:
        atualizar(perfil, conta.id)

        then:
        thrown(IllegalStateException)

        when:
        servico(perfil).logar(email(perfil), 'senha inicial')

        then:
        RuntimeException erro = thrown()
        erro.message.startsWith('Credenciais inválidas')

        where:
        perfil << ['candidato', 'empresa']
    }

    def 'busca por email ignora conta antiga inativa de #perfil'() {
        given:
        def dao = repositorio(perfil)
        cadastrar(perfil)
        def antiga = dao.buscarAtivoPorEmail(email(perfil)).orElseThrow()
        dao.desativar(antiga.id)
        cadastrar(perfil)

        expect:
        dao.buscarAtivoPorEmail(email(perfil)).orElseThrow().id != antiga.id
        servico(perfil).logar(email(perfil), 'senha inicial').id != antiga.id

        where:
        perfil << ['candidato', 'empresa']
    }

    private def servico(String perfil) { perfil == 'candidato' ? candidatoService : empresaService }
    private def repositorio(String perfil) { perfil == 'candidato' ? candidatoDAO : empresaDAO }
    private static String email(String perfil) { perfil + '@example.invalid' }

    private void cadastrar(String perfil) {
        if (perfil == 'candidato') {
            candidatoService.cadastrarCandidato(new CadastrarCandidatoDTO('Nome', 'Sobrenome', email(perfil),
                    'senha inicial', 'cpf-teste', LocalDate.of(2000, 1, 1), 'BRASIL', 26, 'SP', '00000000', 'Descricao', []))
        } else {
            empresaService.cadastrarEmpresa(new CadastrarEmpresaDTO('Nome', email(perfil),
                    'senha inicial', 'cnpj-teste', 'BRASIL', 'SP', '00000000', 'Descricao'))
        }
    }

    private void atualizar(String perfil, Long id) {
        if (perfil == 'candidato') {
            candidatoService.atualizarCandidato(new AtualizarCandidatoDTO(id, 'Nome', 'Sobrenome', email(perfil),
                    'senha nova', 'cpf-teste', LocalDate.of(2000, 1, 1), 'BRASIL', 26, 'SP', '00000000', 'Descricao', false))
        } else {
            empresaService.atualizarEmpresa(new AtualizarEmpresaDTO(id, 'Nome', email(perfil),
                    'senha nova', 'cnpj-teste', 'BRASIL', 'SP', '00000000', 'Descricao', false))
        }
    }
}
