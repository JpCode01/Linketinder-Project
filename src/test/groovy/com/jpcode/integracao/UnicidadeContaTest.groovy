package com.jpcode.integracao

import com.jpcode.dao.candidato.CandidatoDAO
import com.jpcode.dao.empresa.EmpresaDAO
import com.jpcode.dao.recrutador.RecrutadorDAO
import com.jpcode.dao.referencia.AreaRecrutamentoDAO
import com.jpcode.dao.conta.ValidacaoContaDAO
import com.jpcode.database.ConnectionFactory
import com.jpcode.dto.recrutador.CadastrarRecrutadorDTO
import com.jpcode.exception.conta.IdentificadorEmUsoException
import com.jpcode.model.core.*
import com.jpcode.service.RecrutadorService
import com.jpcode.service.SenhaService
import groovy.json.JsonOutput
import spock.lang.Specification

import java.sql.Connection
import java.sql.SQLException
import java.time.LocalDate
import java.util.concurrent.*
import static org.junit.jupiter.api.Assumptions.assumeTrue

class UnicidadeContaTest extends Specification {
    SuporteContasPostgres banco
    Long empresa

    def setup() {
        assumeTrue(SuportePostgres.configurado(), 'Integração NÃO EXECUTADA: configure o banco dedicado de testes.')
        banco = new SuporteContasPostgres()
        empresa = dao('empresa').salvar(conta('empresa', 'proprietaria', 'proprietaria@example.invalid')).id
    }

    def cleanup() { banco?.close() }

    def 'cadastros concorrentes #a e #b confirmam somente um email ativo'() {
        when:
        def resultados = concorrer(
                { dao(a).salvar(conta(a, 'primeiro', 'disputado@example.invalid')) },
                { dao(b).salvar(conta(b, 'segundo', 'disputado@example.invalid')) })

        then:
        resultados.count { !(it instanceof Throwable) } == 1
        resultados.count { it instanceof IdentificadorEmUsoException } == 1
        resultados.find { it instanceof Throwable }.message == 'Identificador já utilizado por conta ativa.'
        contarEmail('disputado@example.invalid') == 1

        where:
        [a, b] << [['candidato','empresa','recrutador'], ['candidato','empresa','recrutador']].combinations()
    }

    def 'atualizacao de #perfil disputa email com cadastro de #outro'() {
        given:
        def atual = dao(perfil).salvar(conta(perfil, 'atual', 'atual@example.invalid'))
        atual.email = 'disputado@example.invalid'

        when:
        def resultados = concorrer(
                { dao(perfil).atualizarDados(atual) },
                { dao(outro).salvar(conta(outro, 'nova', 'disputado@example.invalid')) })

        then:
        resultados.count { !(it instanceof Throwable) } == 1
        resultados.count { it instanceof IdentificadorEmUsoException } == 1
        contarEmail('disputado@example.invalid') == 1
        dao(perfil).buscarPorId(atual.id).orElseThrow().email in ['atual@example.invalid','disputado@example.invalid']

        where:
        [perfil, outro] << [['candidato','empresa'], ['candidato','empresa','recrutador']].combinations()
    }

    def 'duas atualizacoes concorrentes nao compartilham email'() {
        given:
        def candidato = dao('candidato').salvar(conta('candidato', 'c', 'c@example.invalid'))
        def corporacao = dao('empresa').salvar(conta('empresa', 'e', 'e@example.invalid'))
        candidato.email = corporacao.email = 'disputado@example.invalid'

        when:
        def resultados = concorrer({ dao('candidato').atualizarDados(candidato) },
                { dao('empresa').atualizarDados(corporacao) })

        then:
        resultados.count { it instanceof IdentificadorEmUsoException } == 1
        contarEmail('disputado@example.invalid') == 1
    }

    def 'mesmo CPF em candidato e recrutador e permitido mas duplicata ativa no perfil e rejeitada'() {
        given:
        def candidato = conta('candidato', 'comum', 'c@example.invalid')
        def recrutador = conta('recrutador', 'comum', 'r@example.invalid')
        dao('candidato').salvar(candidato)
        dao('recrutador').salvar(recrutador)

        expect:
        candidato.cpf == recrutador.cpf
        candidato.id && recrutador.id

        when:
        dao(perfil).salvar(conta(perfil, 'comum', 'novo@example.invalid'))

        then:
        thrown(IdentificadorEmUsoException)
        contarEmail('novo@example.invalid') == 0

        where:
        perfil << ['candidato','recrutador']
    }

    def 'CNPJ duplicado e conflito de identificador em UPDATE nao alteram dados'() {
        given:
        def original = dao('empresa').salvar(conta('empresa', 'original', 'original@example.invalid'))
        def outra = dao('empresa').salvar(conta('empresa', 'outra', 'outra@example.invalid'))
        outra.cnpj = original.cnpj

        when:
        dao('empresa').atualizarDados(outra)

        then:
        thrown(IdentificadorEmUsoException)
        dao('empresa').buscarPorId(outra.id).orElseThrow().cnpj == 'outra'

        when:
        dao('empresa').salvar(conta('empresa', 'original', 'duplicada@example.invalid'))

        then:
        thrown(IdentificadorEmUsoException)
        contarEmail('duplicada@example.invalid') == 0
    }

    def 'conta inativa #perfil permite reutilizacao com ID novo sem transferir vinculos'() {
        given:
        def antiga = dao(perfil).salvar(conta(perfil, 'reutilizado', 'reuso@example.invalid'))
        long proprietaria = perfil == 'empresa' ? antiga.id : empresa
        executar("""INSERT INTO vagas (nome, descricao, local, id_empresa, id_recrutador)
                    VALUES ('Vaga histórica', 'Descricao', 'SP', ${proprietaria},
                    ${perfil == 'recrutador' ? antiga.id : 'NULL'})""".toString())
        if (perfil == 'candidato') {
            executar("INSERT INTO vagas_curtidas_candidato (id_candidato, id_vaga) VALUES (${antiga.id}, 1)".toString())
        }
        if (perfil == 'recrutador') {
            executar('UPDATE recrutadores SET ativo = false WHERE id = ' + antiga.id)
        } else {
            dao(perfil).desativar(antiga.id)
        }

        when:
        def nova = dao(perfil).salvar(conta(perfil, 'reutilizado', 'reuso@example.invalid'))

        then:
        nova.id != antiga.id
        !dao(perfil).buscarPorId(antiga.id).orElseThrow().ativo
        nova.ativo
        contarEmail('reuso@example.invalid') == 1
        banco.abrirConexao().withCloseable { conexao ->
            conexao.createStatement().withCloseable { comando ->
                String sql = perfil == 'candidato' ?
                        'SELECT id_candidato FROM vagas_curtidas_candidato' :
                        "SELECT ${perfil == 'empresa' ? 'id_empresa' : 'id_recrutador'} FROM vagas".toString()
                comando.executeQuery(sql).withCloseable { resultado ->
                    assert resultado.next()
                    assert resultado.getLong(1) == antiga.id
                    assert resultado.getLong(1) != nova.id
                    assert !resultado.next()
                }
            }
            true
        }

        where:
        perfil << ['candidato','empresa','recrutador']
    }

    def 'reutilizacao de email inativo entre perfis mantem conta antiga intacta'() {
        given:
        def antiga = dao('candidato').salvar(conta('candidato', 'antiga', 'reuso@example.invalid'))
        dao('candidato').desativar(antiga.id)

        expect:
        dao('recrutador').salvar(conta('recrutador', 'nova', 'reuso@example.invalid')).ativo
        !dao('candidato').buscarPorId(antiga.id).orElseThrow().ativo
        dao('candidato').buscarPorId(antiga.id).orElseThrow().email == 'reuso@example.invalid'
    }

    def 'desativacao de #perfil e cadastro de #outro concorrentes preservam exclusividade'() {
        given:
        def antiga = dao(perfil).salvar(conta(perfil, 'antiga', 'reuso@example.invalid'))

        when:
        def resultados = concorrer({ dao(perfil).desativar(antiga.id) },
                { dao(outro).salvar(conta(outro, 'nova', 'reuso@example.invalid')) })

        then:
        resultados.every { !(it instanceof Throwable) || it instanceof IdentificadorEmUsoException }
        !dao(perfil).buscarPorId(antiga.id).orElseThrow().ativo
        contarEmail('reuso@example.invalid') <= 1

        when:
        if (contarEmail('reuso@example.invalid') == 0) {
            dao(outro).salvar(conta(outro, 'nova', 'reuso@example.invalid'))
        }

        then:
        contarEmail('reuso@example.invalid') == 1

        where:
        [perfil, outro] << [['candidato', 'empresa'], ['candidato', 'empresa', 'recrutador']].combinations()
    }

    def 'CPF ou CNPJ concorrente em #perfil confirma somente uma conta'() {
        when:
        def resultados = concorrer({ dao(perfil).salvar(conta(perfil, 'mesmo', 'um@example.invalid')) },
                { dao(perfil).salvar(conta(perfil, 'mesmo', 'dois@example.invalid')) })

        then:
        resultados.count { !(it instanceof Throwable) } == 1
        resultados.count { it instanceof IdentificadorEmUsoException } == 1
        contarEmail('um@example.invalid') + contarEmail('dois@example.invalid') == 1

        where:
        perfil << ['candidato', 'empresa', 'recrutador']
    }

    def 'falha de INSERT real em #perfil nao reserva email e permite repetir'() {
        given:
        def nova = conta(perfil, 'falha', 'falha@example.invalid')
        nova.nome = null

        when:
        dao(perfil).salvar(nova)

        then:
        SQLException erro = thrown()
        erro.SQLState == '23502'
        contarEmail('falha@example.invalid') == 0

        when:
        nova.nome = 'Nome'
        dao(perfil).salvar(nova)

        then:
        contarEmail('falha@example.invalid') == 1

        where:
        perfil << ['candidato', 'empresa', 'recrutador']
    }

    def 'UPDATE exclui somente propria linha e rejeita conta inativa'() {
        given:
        def conta = dao(perfil).salvar(conta(perfil, 'mesma', 'mesma@example.invalid'))

        expect:
        dao(perfil).atualizarDados(conta).id == conta.id

        when:
        dao(perfil).desativar(conta.id)
        conta.ativo = true
        dao(perfil).atualizarDados(conta)

        then:
        thrown(IllegalStateException)
        !dao(perfil).buscarPorId(conta.id).orElseThrow().ativo

        where:
        perfil << ['candidato','empresa']
    }

    def 'falha SQL reverte escrita anterior libera locks e permite repetir'() {
        when:
        new ValidacaoContaDAO(banco.factory).executar('candidatos', null, 'falha@example.invalid', 'falha') { conexao ->
            conexao.createStatement().withCloseable { comando ->
                comando.executeUpdate("UPDATE empresas SET descricao = 'nao confirmar' WHERE id = ${empresa}".toString())
                comando.executeUpdate("INSERT INTO areas_recrutamento (nome) VALUES (NULL)")
            }
        }

        then:
        SQLException erro = thrown()
        erro.SQLState == '23502'
        dao('empresa').buscarPorId(empresa).orElseThrow().descricao == 'Descricao'

        when:
        def resultado = concorrer(
                { dao('candidato').salvar(conta('candidato', 'retentativa', 'falha@example.invalid')) },
                { dao('empresa').salvar(conta('empresa', 'livre', 'livre@example.invalid')) })

        then:
        resultado.every { !(it instanceof Throwable) }
        contarEmail('falha@example.invalid') == 1
    }

    def 'transacao realmente espera lock de outra conexao e valida email depois do commit'() {
        given:
        Connection primeira = banco.abrirConexao()
        primeira.autoCommit = false
        primeira.createStatement().withCloseable { comando ->
            comando.execute('LOCK TABLE candidatos, empresas, recrutadores IN SHARE ROW EXCLUSIVE MODE')
            comando.execute("UPDATE empresas SET email_corporativo = 'ocupado@example.invalid' WHERE id = ${empresa}".toString())
        }
        def executor = Executors.newSingleThreadExecutor()
        def iniciou = new CountDownLatch(1)
        ConnectionFactory outraFactory = { ->
            def conexao = banco.abrirConexao()
            iniciou.countDown()
            conexao
        } as ConnectionFactory
        def tarefa = executor.submit({ ->
            try {
                new CandidatoDAO(outraFactory).salvar(conta('candidato', 'espera', 'ocupado@example.invalid'))
            } catch (Exception erro) { return erro }
        } as Callable)

        when:
        assert iniciou.await(5, TimeUnit.SECONDS)
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(4)
        boolean esperando = false
        while (!esperando && System.nanoTime() < limite) {
            primeira.createStatement().withCloseable { comando ->
                comando.executeQuery('''SELECT EXISTS (SELECT 1 FROM pg_locks l JOIN pg_class c ON c.oid = l.relation
                        JOIN pg_namespace n ON n.oid = c.relnamespace
                        WHERE n.nspname = current_schema() AND c.relname = 'candidatos' AND NOT l.granted)''').withCloseable { r ->
                    r.next(); esperando = r.getBoolean(1)
                }
            }
            if (!esperando) Thread.sleep(20)
        }
        assert esperando
        assert !tarefa.done
        primeira.commit()
        def resultado = tarefa.get(10, TimeUnit.SECONDS)

        then:
        resultado instanceof IdentificadorEmUsoException
        contarEmail('ocupado@example.invalid') == 1

        cleanup:
        primeira?.rollback()
        primeira?.close()
        executor?.shutdownNow()
        executor?.awaitTermination(10, TimeUnit.SECONDS)
    }

    def 'recrutador usa servico e JDBC reais com hash referencias e DTO seguro'() {
        given:
        def areas = new AreaRecrutamentoDAO(banco.factory)
        def service = new RecrutadorService(dao('recrutador'), areas, dao('empresa'))

        when:
        def resultado = service.cadastrarRecrutador(empresa, new CadastrarRecrutadorDTO(nome: 'Nome',
                cpf: 'cpf-real', email: 'real@example.invalid', dataNascimento: LocalDate.of(1990,1,1),
                descricao: 'Descricao', senha: 'segredo real', idAreaRecrutamento: 1L))
        def persistido = dao('recrutador').buscarPorId(resultado.id).orElseThrow()

        then:
        persistido.ativo && persistido.idEmpresa == empresa && persistido.idAreaRecrutamento == 1L
        new SenhaService().verificar('segredo real', persistido.senha)
        !JsonOutput.toJson(resultado).contains(persistido.senha)
        !JsonOutput.toJson(service.buscarRecrutador(resultado.id, resultado.id)).contains('senha')
        areas.buscar()*.nome == ['Recursos Humanos']
        areas.buscarIdPorNome('Recursos Humanos').orElseThrow() == 1L
        !areas.buscarIdPorNome('ausente').present
        service.logar('real@example.invalid', 'segredo real').id == resultado.id
        service.estaAtivo(resultado.id)
        !service.estaAtivo(-1L)

        when:
        service.logar('real@example.invalid', 'senha incorreta')

        then:
        thrown(SecurityException)

        when:
        try (def conexao = banco.abrirConexao();
             def comando = conexao.prepareStatement('UPDATE recrutadores SET ativo = false WHERE id = ?')) {
            comando.setLong(1, resultado.id)
            comando.executeUpdate()
        }

        then:
        !service.estaAtivo(resultado.id)
        !dao('recrutador').buscarAtivoPorEmail('real@example.invalid').present

        when:
        service.logar('real@example.invalid', 'segredo real')

        then:
        thrown(SecurityException)
    }

    def 'DAO revalida empresa e area depois dos locks e forca atividade inicial'() {
        given:
        def r = conta('recrutador', 'ref', 'ref@example.invalid')
        r.idAreaRecrutamento = -1L

        when:
        dao('recrutador').salvar(r)

        then:
        thrown(IllegalArgumentException)
        contarEmail(r.email) == 0

        when:
        r.idAreaRecrutamento = 1L
        dao('empresa').desativar(empresa)
        dao('recrutador').salvar(r)

        then:
        thrown(IllegalArgumentException)
        contarEmail(r.email) == 0
    }

    def 'DAO de #perfil forca ativo no cadastro'() {
        given:
        def nova = conta(perfil, 'ativo', 'ativo@example.invalid')
        nova.ativo = false

        expect:
        dao(perfil).salvar(nova).ativo
        dao(perfil).buscarPorId(nova.id).orElseThrow().ativo

        where:
        perfil << ['candidato','empresa','recrutador']
    }

    private def dao(String perfil) {
        switch (perfil) {
            case 'candidato': return new CandidatoDAO(banco.factory)
            case 'empresa': return new EmpresaDAO(banco.factory)
            case 'recrutador': return new RecrutadorDAO(banco.factory)
        }
    }

    private def conta(String perfil, String identificador, String email) {
        switch (perfil) {
            case 'candidato': return new Candidato('Nome', 'Sobrenome', email, 'fixture-sem-login', identificador,
                    LocalDate.of(1990,1,1), 1L, 36, 1L, '00000000', 'Descricao')
            case 'empresa': return new Empresa('Empresa', email, 'fixture-sem-login', identificador, 1L, 1L, '00000000', 'Descricao')
            case 'recrutador': return new Recrutador(nome: 'Nome', email: email, cpf: identificador,
                    senha: 'fixture-sem-login', dataNascimento: LocalDate.of(1990,1,1), idEmpresa: empresa,
                    idAreaRecrutamento: 1L, descricao: 'Descricao')
        }
    }

    private static List concorrer(Closure primeira, Closure segunda) {
        def executor = Executors.newFixedThreadPool(2)
        def prontas = new CountDownLatch(2)
        def inicio = new CountDownLatch(1)
        try {
            def tarefas = [primeira, segunda].collect { operacao ->
                executor.submit({ ->
                    prontas.countDown()
                    if (!inicio.await(5, TimeUnit.SECONDS)) throw new TimeoutException('Barreira não liberada.')
                    try { return operacao.call() } catch (Exception erro) { return erro }
                } as Callable)
            }
            assert prontas.await(5, TimeUnit.SECONDS)
            inicio.countDown()
            return tarefas.collect { it.get(15, TimeUnit.SECONDS) }
        } finally {
            inicio.countDown()
            executor.shutdownNow()
            assert executor.awaitTermination(10, TimeUnit.SECONDS)
        }
    }

    private long contarEmail(String email) {
        try (def conexao = banco.abrirConexao(); def comando = conexao.prepareStatement('''SELECT count(*) FROM (
                SELECT email FROM candidatos WHERE ativo = true UNION ALL
                SELECT email_corporativo FROM empresas WHERE ativo = true UNION ALL
                SELECT email FROM recrutadores WHERE ativo = true) contas WHERE email = ?''')) {
            comando.setString(1, email)
            try (def resultado = comando.executeQuery()) { resultado.next(); return resultado.getLong(1) }
        }
    }

    private void executar(String sql) {
        try (def conexao = banco.abrirConexao(); def comando = conexao.createStatement()) { comando.execute(sql) }
    }
}
