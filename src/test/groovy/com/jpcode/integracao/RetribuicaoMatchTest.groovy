package com.jpcode.integracao

import com.jpcode.dao.contratos.MatchRepository
import com.jpcode.dao.match.MatchDAO
import com.jpcode.dao.relacionamento.CandidatoCurtirDAO
import com.jpcode.dao.relacionamento.EmpresaCurtirDAO
import com.jpcode.dao.vaga.VagaDAO
import com.jpcode.facade.MatchFacade
import com.jpcode.service.MatchService
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Specification
import java.sql.Connection
import java.sql.SQLException
import java.util.concurrent.*
import static org.junit.jupiter.api.Assumptions.assumeTrue

/** T09: somente banco dedicado, transações reais e schema próprio por caso. */
class RetribuicaoMatchTest extends Specification {
    SuporteContasPostgres banco
    CandidatoCurtirDAO interesses
    EmpresaCurtirDAO retribuicoes
    MatchDAO matches
    MatchFacade facade

    def setup() {
        assumeTrue(SuportePostgres.configurado(), 'Integração NÃO EXECUTADA: configure o banco dedicado de testes.')
        banco = new SuporteContasPostgres()
        executar('''
            INSERT INTO empresas (nome, cnpj, email_corporativo, id_pais, cep)
            VALUES ('Empresa A', 'a', 'a@teste', 1, 'cep'), ('Empresa B', 'b', 'b@teste', 1, 'cep');
            INSERT INTO recrutadores (nome, id_area_recrutamento, email, cpf, data_nascimento, senha, id_empresa)
            VALUES ('Autor A', 1, 'r1@teste', 'r1', '1990-01-01', 'fixture-sem-login', 1),
                   ('Colega A', 1, 'r2@teste', 'r2', '1990-01-01', 'fixture-sem-login', 1),
                   ('Autor B', 1, 'r3@teste', 'r3', '1990-01-01', 'fixture-sem-login', 2);
            INSERT INTO candidatos (nome, sobrenome, email, senha, data_nascimento, cpf, id_pais, cep, descricao)
            VALUES ('Pessoa', 'Candidata', 'c@teste', 'fixture-sem-login', '1990-01-01', 'c1', 1, 'cep', 'Descrição anônima');
            INSERT INTO vagas (nome, descricao, local, id_empresa, id_recrutador)
            VALUES ('Autoral A', 'Descrição A', 'SP', 1, 1), ('Colega A', 'Descrição B', 'SP', 1, 2),
                   ('Direta', 'Descrição C', 'SP', 1, NULL), ('Outra empresa', 'Descrição D', 'SP', 2, 3);
            INSERT INTO competencias (nome_competencia) VALUES ('RH');
            INSERT INTO candidatos_competencias (id_candidato, id_competencia) VALUES (1, 1);
            INSERT INTO vagas_competencias (id_vaga, id_competencia) VALUES (1, 1);
        ''')
        interesses = new CandidatoCurtirDAO(banco.factory)
        retribuicoes = new EmpresaCurtirDAO(banco.factory)
        matches = new MatchDAO(banco.factory, null, null)
        facade = new MatchFacade(banco.factory, retribuicoes, interesses, new MatchService(matches))
    }

    def cleanup() { banco?.close() }

    def 'interesse unico chega a empresa e autor sem match e contratos continuam anonimos'() {
        when:
        interesses.salvar(1L, 1L)
        interesses.salvar(1L, 1L)
        interesses.salvar(1L, 3L)

        then:
        contar('vagas_curtidas_candidato') == 2
        contar('candidatos_curtidos_empresa') == 0
        contar('match') == 0
        anonimo(facade.buscarInteressadosEmpresa(1L, 1L))[0].with {
            keySet() == ['id', 'descricao', 'competencias'] as Set && competencias*.nome == ['RH']
        }
        anonimo(facade.buscarInteressadosRecrutador(1L, 1L)) == anonimo(facade.buscarInteressadosEmpresa(1L, 1L))
        facade.buscarInteressadosEmpresa(1L, 3L)*.id == [1L]
        anonimo(interesses.buscarCurtidas(1L)).every { it.keySet() == ['id', 'descricao', 'competencias'] as Set }

        when:
        facade.buscarInteressadosRecrutador(1L, 3L)
        then:
        thrown(SecurityException)
    }

    def 'empresa e recrutador retribuem no contexto exato e repeticao conserva primeiro ator: #primeiro'() {
        given:
        interesses.salvar(1L, 1L)
        interesses.salvar(1L, 2L)

        when:
        if (primeiro == 'empresa') facade.retribuirInteresseEmpresa(1L, 1L, 1L)
        else facade.retribuirInteresseRecrutador(1L, 1L, 1L)
        facade.retribuirInteresseEmpresa(1L, 1L, 1L)
        facade.retribuirInteresseRecrutador(1L, 1L, 1L)

        then:
        contar('candidatos_curtidos_empresa') == 1
        contar('match') == 1
        valor('SELECT id_vaga FROM match') == 1L
        valor('SELECT id_empresa FROM match') == 1L
        valor('SELECT id_recrutador_ator FROM candidatos_curtidos_empresa') == (primeiro == 'empresa' ? null : 1L)
        valor('SELECT id_recrutador FROM vagas WHERE id = 1') == 1L
        facade.buscarInteressadosRecrutador(2L, 2L)*.id == [1L]
        anonimo(facade.buscarInteressadosRecrutador(2L, 2L))[0].keySet() == ['id', 'descricao', 'competencias'] as Set
        where:
        primeiro << ['empresa', 'recrutador']
    }

    def 'empresa retribui em vaga direta sem atribuir recrutador'() {
        given:
        interesses.salvar(1L, 3L)
        when:
        facade.retribuirInteresseEmpresa(1L, 1L, 3L)
        then:
        contar('match') == 1
        valor('SELECT id_recrutador_ator FROM candidatos_curtidos_empresa') == null
        valor('SELECT id_recrutador FROM vagas WHERE id = 3') == null
    }

    def 'interesse na primeira vaga nao permite retribuicao na segunda'() {
        given:
        interesses.salvar(1L, 1L)
        when:
        facade.retribuirInteresseEmpresa(1L, 1L, 2L)
        then:
        thrown(IllegalStateException)
        contar('match') == 0
        contar('candidatos_curtidos_empresa') == 0
    }

    def 'repeticao nos DAOs exige a mesma empresa do contexto confirmado'() {
        given:
        interesses.salvar(1L, 1L)
        facade.retribuirInteresseEmpresa(1L, 1L, 1L)
        when:
        try (Connection conexao = banco.abrirConexao()) {
            conexao.autoCommit = false
            try { retribuicoes.salvar(conexao, 2L, 1L, 1L, null) }
            finally { conexao.rollback() }
        }
        then:
        thrown(IllegalStateException)
        when:
        try (Connection conexao = banco.abrirConexao()) {
            conexao.autoCommit = false
            try { matches.salvar(conexao, 1L, 2L, 1L) }
            finally { conexao.rollback() }
        }
        then:
        thrown(IllegalStateException)
        contar('match') == 1
        contar('candidatos_curtidos_empresa') == 1
        valor('SELECT id_empresa FROM match') == 1L
    }

    def 'rejeicao real nao deixa escrita: #cenario'() {
        given:
        if (interesse) interesses.salvar(1L, 1L)
        if (preparar) executar(preparar)
        when:
        if (recrutador) facade.retribuirInteresseRecrutador(ator, 1L, 1L)
        else facade.retribuirInteresseEmpresa(ator, 1L, 1L)
        then:
        thrown(RuntimeException)
        contar('candidatos_curtidos_empresa') == 0
        contar('match') == 0
        where:
        cenario             | recrutador | ator | interesse | preparar
        'sem interesse'     | false      | 1L   | false     | null
        'outra empresa'     | false      | 2L   | true      | null
        'colega'            | true       | 2L   | true      | null
        'outro vinculo'     | true       | 3L   | true      | null
        'autor inativo'     | true       | 1L   | true      | 'UPDATE recrutadores SET ativo = false WHERE id = 1'
        'empresa inativa'   | false      | 1L   | true      | 'UPDATE empresas SET ativo = false WHERE id = 1'
        'candidato inativo' | false      | 1L   | true      | 'UPDATE candidatos SET ativo = false WHERE id = 1'
    }

    def 'consulta real rejeita colega e empresa alheia mesmo apos match'() {
        given:
        interesses.salvar(1L, 1L)
        facade.retribuirInteresseEmpresa(1L, 1L, 1L)
        when:
        facade.buscarInteressadosRecrutador(2L, 1L)
        then:
        thrown(SecurityException)
        when:
        facade.buscarInteressadosEmpresa(2L, 1L)
        then:
        thrown(SecurityException)
    }

    def 'empresa continua recebendo interesses e formando match com autor historico inativo'() {
        given:
        executar('UPDATE recrutadores SET ativo = false WHERE id = 1')
        interesses.salvar(1L, 1L)
        when:
        facade.retribuirInteresseEmpresa(1L, 1L, 1L)
        then:
        facade.buscarInteressadosEmpresa(1L, 1L)*.id == [1L]
        new VagaDAO(banco.factory).buscarVagasEmpresa(1L)[0].with {
            idRecrutador == 1L && nomeRecrutador == 'Autor A'
        }
        contar('match') == 1
        when:
        facade.buscarInteressadosRecrutador(1L, 1L)
        then:
        thrown(SecurityException)
        when:
        facade.retribuirInteresseRecrutador(1L, 1L, 1L)
        then:
        thrown(SecurityException)
        contar('match') == 1
    }

    def 'falha JDBC entre INSERTs reverte retribuicao e libera transacao para repetir'() {
        given:
        interesses.salvar(1L, 1L)
        MatchRepository falho = new MatchDAO(banco.factory, null, null) {
            @Override
            void salvar(Connection conexao, Long candidato, Long empresa, Long vaga) {
                // Falha SQL real depois do primeiro INSERT, na MESMA conexão.
                super.salvar(conexao, candidato, empresa, 999L)
            }
        }
        def facadeFalha = new MatchFacade(banco.factory, retribuicoes, interesses, new MatchService(falho))
        when:
        facadeFalha.retribuirInteresseRecrutador(1L, 1L, 1L)
        then:
        SQLException erro = thrown()
        erro.SQLState == '23503'
        contar('candidatos_curtidos_empresa') == 0
        contar('match') == 0
        contar('vagas_curtidas_candidato') == 1
        when:
        facade.retribuirInteresseEmpresa(1L, 1L, 1L)
        then:
        contar('match') == 1
        contar('candidatos_curtidos_empresa') == 1
    }

    def 'duas conexoes concorrentes de empresa e autor confirmam somente uma interacao'() {
        given:
        interesses.salvar(1L, 1L)
        when:
        concorrer({ facade.retribuirInteresseEmpresa(1L, 1L, 1L) },
                  { facade.retribuirInteresseRecrutador(1L, 1L, 1L) })
        then:
        contar('match') == 1
        contar('candidatos_curtidos_empresa') == 1
        contar('vagas_curtidas_candidato') == 1
        valor('SELECT id_recrutador_ator FROM candidatos_curtidos_empresa') in [null, 1L]
    }

    def 'curtidas iniciais concorrentes nao duplicam nem geram match'() {
        when:
        concorrer({ interesses.salvar(1L, 1L) }, { interesses.salvar(1L, 1L) })
        then:
        contar('vagas_curtidas_candidato') == 1
        contar('candidatos_curtidos_empresa') == 0
        contar('match') == 0
    }

    def 'interesse rejeita candidato ou proprietaria inativos sem escrita: #sql'() {
        given:
        executar(sql)
        when:
        interesses.salvar(1L, 1L)
        then:
        thrown(IllegalArgumentException)
        contar('vagas_curtidas_candidato') == 0
        where:
        sql << ['UPDATE candidatos SET ativo = false WHERE id = 1', 'UPDATE empresas SET ativo = false WHERE id = 1']
    }

    def 'exclusao remove dependencias atomicamente e preserva outras vagas'() {
        given:
        [1L, 2L].each {
            interesses.salvar(1L, it)
            facade.retribuirInteresseEmpresa(1L, 1L, it)
        }
        when:
        new VagaDAO(banco.factory).deletar(1L)
        then:
        contar('vagas') == 3
        contar('vagas_competencias') == 0
        contar('vagas_curtidas_candidato') == 1
        contar('candidatos_curtidos_empresa') == 1
        contar('match') == 1
        valor('SELECT id_vaga FROM match') == 2L
        when:
        interesses.salvar(1L, 1L)
        then:
        thrown(IllegalArgumentException)
    }

    def 'falha na exclusao reverte matches e retribuicoes removidos antes da vaga'() {
        given:
        interesses.salvar(1L, 1L)
        facade.retribuirInteresseEmpresa(1L, 1L, 1L)
        executar('CREATE TABLE bloqueio_fixture (id_vaga INTEGER REFERENCES vagas(id)); INSERT INTO bloqueio_fixture VALUES (1)')
        when:
        new VagaDAO(banco.factory).deletar(1L)
        then:
        SQLException erro = thrown()
        erro.SQLState == '23503'
        contar('match') == 1
        contar('candidatos_curtidos_empresa') == 1
        contar('vagas_curtidas_candidato') == 1
        contar('vagas_competencias') == 1
        contar('vagas') == 4
    }

    def 'DAO de match nao grava sem reciprocidade mesmo com conexao fornecida'() {
        when:
        try (Connection conexao = banco.abrirConexao()) {
            conexao.autoCommit = false
            try { matches.salvar(conexao, 1L, 1L, 1L) }
            finally { conexao.rollback() }
        }
        then:
        SQLException erro = thrown()
        erro.SQLState == '23503'
        contar('match') == 0
    }

    def 'DAOs participantes nao confirmam nem fecham a conexao emprestada'() {
        given:
        interesses.salvar(1L, 1L)
        when:
        try (Connection conexao = banco.abrirConexao()) {
            conexao.autoCommit = false
            retribuicoes.salvar(conexao, 1L, 1L, 1L, null)
            matches.salvar(conexao, 1L, 1L, 1L)
            assert !conexao.closed
            assert !conexao.autoCommit
            assert contar('match') == 0
            assert contar('candidatos_curtidos_empresa') == 0
            conexao.rollback()
        }
        then:
        contar('match') == 0
        contar('candidatos_curtidos_empresa') == 0
    }

    def 'DAOs rejeitam conexao sem transacao antes de gravar'() {
        when:
        try (Connection conexao = banco.abrirConexao()) { retribuicoes.salvar(conexao, 1L, 1L, 1L, null) }
        then:
        thrown(IllegalStateException)
        when:
        try (Connection conexao = banco.abrirConexao()) { matches.salvar(conexao, 1L, 1L, 1L) }
        then:
        thrown(IllegalStateException)
        contar('match') == 0
        contar('candidatos_curtidos_empresa') == 0
    }

    def 'locks protegem atividade autoria e interesse ate confirmar: #alteracao'() {
        given:
        interesses.salvar(1L, 1L)
        CountDownLatch gravada = new CountDownLatch(1)
        CountDownLatch liberar = new CountDownLatch(1)
        def pausada = new EmpresaCurtirDAO(banco.factory) {
            @Override
            void salvar(Connection conexao, Long empresa, Long candidato, Long vaga, Long ator) {
                super.salvar(conexao, empresa, candidato, vaga, ator)
                gravada.countDown()
                assert liberar.await(5, TimeUnit.SECONDS)
            }
        }
        def facadePausada = new MatchFacade(banco.factory, pausada, interesses, new MatchService(matches))
        ExecutorService executor = Executors.newSingleThreadExecutor()
        Future<Boolean> tarefa = executor.submit({ ->
            facadePausada.retribuirInteresseRecrutador(1L, 1L, 1L)
            return true
        } as Callable<Boolean>)

        when:
        SQLException bloqueio = null
        try {
            assert gravada.await(5, TimeUnit.SECONDS)
            assert contar('match') == 0
            assert contar('candidatos_curtidos_empresa') == 0
            try (Connection conexao = banco.abrirConexao(); def comando = conexao.createStatement()) {
                comando.execute("SET lock_timeout TO '200ms'")
                try { comando.execute(alteracao) }
                catch (SQLException erro) { bloqueio = erro }
            }
        } finally {
            liberar.countDown()
            executor.shutdown()
        }

        then:
        bloqueio?.SQLState == '55P03'
        tarefa.get(10, TimeUnit.SECONDS)
        executor.awaitTermination(10, TimeUnit.SECONDS)
        contar('match') == 1
        contar('candidatos_curtidos_empresa') == 1

        cleanup:
        liberar.countDown()
        executor.shutdownNow()
        executor.awaitTermination(10, TimeUnit.SECONDS)

        where:
        alteracao << ['UPDATE empresas SET ativo = false WHERE id = 1',
                      'UPDATE recrutadores SET ativo = false WHERE id = 1',
                      'UPDATE candidatos SET ativo = false WHERE id = 1',
                      'UPDATE vagas SET id_recrutador = 2 WHERE id = 1',
                      'DELETE FROM vagas_curtidas_candidato WHERE id_candidato = 1 AND id_vaga = 1']
    }

    private static def anonimo(def dados) { new JsonSlurper().parseText(JsonOutput.toJson(dados)) }

    private static void concorrer(Closure primeira, Closure segunda) {
        ExecutorService executor = Executors.newFixedThreadPool(2)
        CountDownLatch prontas = new CountDownLatch(2)
        CountDownLatch iniciar = new CountDownLatch(1)
        try {
            def tarefas = [primeira, segunda].collect { Closure operacao ->
                executor.submit({ ->
                    prontas.countDown()
                    assert iniciar.await(5, TimeUnit.SECONDS)
                    operacao.call()
                    return true
                } as Callable<Boolean>)
            }
            assert prontas.await(5, TimeUnit.SECONDS)
            iniciar.countDown()
            tarefas.each { assert it.get(15, TimeUnit.SECONDS) }
        } finally {
            iniciar.countDown()
            executor.shutdownNow()
            assert executor.awaitTermination(15, TimeUnit.SECONDS)
        }
    }

    private void executar(String sql) {
        try (def conexao = banco.abrirConexao(); def comando = conexao.createStatement()) { comando.execute(sql) }
    }

    private Long valor(String sql) {
        try (def conexao = banco.abrirConexao(); def comando = conexao.createStatement(); def resultado = comando.executeQuery(sql)) {
            assert resultado.next()
            long valor = resultado.getLong(1)
            return resultado.wasNull() ? null : valor
        }
    }

    private long contar(String tabela) { valor("SELECT count(*) FROM ${tabela}".toString()) }
}
