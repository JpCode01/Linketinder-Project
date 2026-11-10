package com.jpcode.integracao

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import static org.junit.jupiter.api.Assumptions.assumeTrue

class FluxoHttpRecrutadorTest extends SuporteHttpPostgres {
    @Override
    protected int portaHttp() { 8080 }

    def 'cadastro candidato preserva vinculos com catalogo padronizado em maiusculas'() {
        given:
        try (def conexao = banco.factory.getConnection(); def comando = conexao.createStatement()) {
            comando.execute("INSERT INTO competencias (nome_competencia) VALUES ('ADMINISTRAÇÃO')")
        }
        def candidato = cliente()
        Map dados = [nome: 'Pessoa', sobrenome: 'T18', email: 'catalogo@t18.invalid', senha: 'fixture-t18',
                     cpf: 'cpf-catalogo-t18', dataNascimento: '1990-01-01', pais: 'BRASIL', estado: 'SP',
                     idade: 36, cep: '00000000', descricao: 'Catálogo persistido', competencias: ['RH', 'ADMINISTRAÇÃO']]

        when:
        def resposta = enviar(candidato, 'POST', '/candidatos', dados)
        int contas
        try (def conexao = banco.factory.getConnection(); def comando = conexao.createStatement();
             def resultado = comando.executeQuery('SELECT count(*) FROM candidatos')) {
            assert resultado.next()
            contas = resultado.getInt(1)
        }
        println "T18 catálogo: HTTP=${resposta.statusCode()}, candidatos=${contas}, vínculos=${quantidade('candidatos_competencias')}"

        then:
        json(enviar(candidato, 'GET', '/competencias')) == ['RH', 'ADMINISTRAÇÃO']
        resposta.statusCode() == 201
        contas == 1
        quantidade('candidatos_competencias') == 2
    }

    def 'formularios e jornadas reais preservam persistencia cookies escopo e autoria historica'() {
        given:
        String navegador = System.getenv('LINKETINDER_TEST_BROWSER')
        assumeTrue(navegador != null, 'Navegador NÃO EXECUTADO: configure LINKETINDER_TEST_BROWSER com Chromium/Chrome instalado.')
        assert Files.isRegularFile(Path.of(navegador))
        assert Files.isRegularFile(Path.of('frontend/dist/js/bundle.js')): 'Execute o build frontend antes do aceite.'
        try (def conexao = banco.factory.getConnection(); def comando = conexao.createStatement()) {
            comando.execute("INSERT INTO areas_recrutamento (nome) VALUES ('Administração')")
            comando.execute("INSERT INTO competencias (nome_competencia) VALUES ('ADMINISTRAÇÃO')")
        }
        Path log = Path.of('build', 'navegador-t18.log')
        def builder = new ProcessBuilder('node', 'frontend/tests/aceite-t18.cjs', navegador)
                .redirectErrorStream(true).redirectOutput(log.toFile())
        builder.environment().keySet().removeIf { it.startsWith('DB_') || it.startsWith('LINKETINDER_TEST_') }
        Process processo = builder.start()

        when:
        boolean terminou = processo.waitFor(150, TimeUnit.SECONDS)
        if (!terminou) processo.destroyForcibly()

        then:
        assert terminou: 'Timeout do navegador; consulte build/navegador-t18.log.'
        assert processo.exitValue() == 0: 'Falha funcional no navegador; consulte build/navegador-t18.log.'
        quantidade('vagas') == 4
        quantidade('vagas_competencias') == 8
        quantidade('candidatos_competencias') == 2
        quantidade('vagas_curtidas_candidato') == 4
        quantidade('candidatos_curtidos_empresa') == 3
        quantidade('match') == 3
        verificarPersistenciaContas()

        when: 'sessão real invalidada após inatividade e continuidade pela proprietária'
        def autor = cliente()
        def empresa = cliente()
        def candidato = cliente()
        assert login(autor, 'recrutador', 'autor@t18.invalid').statusCode() == 200
        assert login(empresa, 'empresa', 'empresa@t18.invalid').statusCode() == 200
        assert login(candidato, 'candidato', 'candidato@t18.invalid').statusCode() == 200
        assert enviar(autor, 'POST', '/recrutadores/1/vagas', [nome: 'Histórica', descricao: 'Continuidade', local: 'SP', competencias: ['RH']]).statusCode() == 201
        assert enviar(candidato, 'POST', '/curtidas/vagas', [idVaga: 5]).statusCode() == 204
        try (def conexao = banco.factory.getConnection(); def comando = conexao.createStatement()) {
            comando.execute('UPDATE recrutadores SET ativo = false WHERE id = 1')
        }

        then:
        enviar(autor, 'GET', '/matches/recrutador/1').statusCode() == 401
        login(autor, 'recrutador', 'autor@t18.invalid').statusCode() == 401
        enviar(empresa, 'POST', '/curtidas/candidatos', [idCandidato: 1, idVaga: 5]).statusCode() == 204
        json(enviar(empresa, 'GET', '/matches/empresa/1')).find { it.nome == 'Histórica' }.with {
            nomeRecrutador == 'Autor T18' && areaRecrutamento == 'Recursos Humanos'
        }
        json(enviar(candidato, 'GET', '/vagas/5')).keySet() == ['id', 'descricao', 'competencias'] as Set
        quantidade('match') == 4

        cleanup:
        if (processo?.isAlive()) processo.destroyForcibly()
    }

    private boolean verificarPersistenciaContas() {
        try (def conexao = banco.factory.getConnection(); def comando = conexao.createStatement()) {
            comando.executeQuery('''
                WITH contas AS (
                    SELECT senha, ativo FROM candidatos UNION ALL
                    SELECT senha, ativo FROM empresas UNION ALL
                    SELECT senha, ativo FROM recrutadores
                )
                SELECT count(*) = 6 AND bool_and(ativo)
                       AND bool_and(senha LIKE 'v1$PBKDF2WithHmacSHA256$600000$%')
                       AND count(DISTINCT split_part(senha, '$', 4)) = 6 AS protegidas
                FROM contas
            ''').withCloseable { resultado ->
                assert resultado.next()
                assert resultado.getBoolean('protegidas'): 'Contas ativas devem ter hashes e salts individuais.'
            }
            comando.executeQuery('''
                SELECT count(*) = 3 AND bool_and(id_empresa = CASE WHEN id = 3 THEN 2 ELSE 1 END)
                       AND bool_and(id_area_recrutamento = CASE WHEN id = 2 THEN 2 ELSE 1 END) AS vinculadas
                FROM recrutadores
            ''').withCloseable { resultado ->
                assert resultado.next()
                assert resultado.getBoolean('vinculadas')
            }
        }
        true
    }
}
