package com.jpcode.integracao

import spock.lang.Specification

import java.sql.Connection
import java.sql.SQLException

import static org.junit.jupiter.api.Assumptions.assumeTrue

class SchemaRecrutadorTest extends Specification {

    Connection conexao
    long empresa
    long area

    def setup() {
        assumeTrue(SuportePostgres.configurado(),
                'Integração NÃO EXECUTADA: configure LINKETINDER_TEST_JDBC_URL, LINKETINDER_TEST_DB_USER e LINKETINDER_TEST_DB_PASSWORD.')
        conexao = SuportePostgres.abrir()
        SuportePostgres.criarSchemaAtual(conexao)
        long pais = inserir("INSERT INTO pais (nome) VALUES (?) RETURNING id", ['País de teste'])
        empresa = inserir('''INSERT INTO empresas (nome, cnpj, email_corporativo, id_pais, cep, senha)
                             VALUES (?, ?, ?, ?, ?, ?) RETURNING id''',
                ['Empresa de teste', 'cnpj-teste', 'empresa@example.invalid', pais, '00000000', 'fixture-sem-login'])
        area = inserir('INSERT INTO areas_recrutamento (nome) VALUES (?) RETURNING id', ['Recursos Humanos'])
    }

    def cleanup() {
        SuportePostgres.fechar(conexao)
    }

    def 'persiste recrutadores ativos com uma empresa e uma area compartilhadas'() {
        given:
        long primeiro = inserirRecrutador(empresa, area, 'primeiro')
        long segundo = inserirRecrutador(empresa, area, 'segundo')

        expect:
        primeiro != segundo
        conexao.createStatement().withCloseable { comando ->
            comando.executeQuery('''SELECT r.id_empresa, r.id_area_recrutamento, r.ativo, a.nome
                                    FROM recrutadores r JOIN areas_recrutamento a ON a.id = r.id_area_recrutamento
                                    ORDER BY r.id''').withCloseable { resultado ->
                int quantidade = 0
                while (resultado.next()) {
                    assert resultado.getLong('id_empresa') == empresa
                    assert resultado.getLong('id_area_recrutamento') == area
                    assert resultado.getBoolean('ativo')
                    assert resultado.getString('nome') == 'Recursos Humanos'
                    quantidade++
                }
                quantidade == 2
            }
        }
    }

    def 'candidato e empresa tambem iniciam ativos por default'() {
        given:
        inserir('''INSERT INTO candidatos (nome, sobrenome, email, senha, data_nascimento, cpf, id_pais, cep)
                   VALUES (?, ?, ?, ?, ?, ?, (SELECT id FROM pais LIMIT 1), ?) RETURNING id''',
                ['Candidato', 'Teste', 'candidato@example.invalid', 'fixture-sem-login',
                 java.sql.Date.valueOf('1990-01-01'), 'cpf-candidato', '00000000'])

        expect:
        conexao.createStatement().withCloseable { comando ->
            comando.executeQuery('SELECT ativo FROM candidatos UNION ALL SELECT ativo FROM empresas').withCloseable { resultado ->
                assert resultado.next() && resultado.getBoolean(1)
                assert resultado.next() && resultado.getBoolean(1)
                !resultado.next()
            }
        }
    }

    def 'rejeita vinculo #vinculo com referencia #referencia'() {
        when:
        inserirRecrutador(vinculo == 'empresa' ? referencia : empresa,
                vinculo == 'area' ? referencia : area)

        then:
        SQLException erro = thrown()
        erro.SQLState == estado

        where:
        vinculo   | referencia | estado
        'empresa' | -1L        | '23503'
        'area'    | -1L        | '23503'
        'empresa' | null       | '23502'
        'area'    | null       | '23502'
    }

    def 'rejeita atividade nula no perfil #tabela'() {
        given:
        if (tabela == 'recrutadores') {
            inserirRecrutador(empresa, area)
        } else if (tabela == 'candidatos') {
            inserir('''INSERT INTO candidatos (nome, sobrenome, email, senha, data_nascimento, cpf, id_pais, cep)
                       VALUES (?, ?, ?, ?, ?, ?, (SELECT id FROM pais LIMIT 1), ?) RETURNING id''',
                    ['Candidato', 'Teste', 'candidato@example.invalid', 'fixture-sem-login',
                     java.sql.Date.valueOf('1990-01-01'), 'cpf-candidato', '00000000'])
        }

        when:
        conexao.createStatement().withCloseable { it.executeUpdate("UPDATE ${tabela} SET ativo = NULL".toString()) }

        then:
        SQLException erro = thrown()
        erro.SQLState == '23502'

        where:
        tabela << ['candidatos', 'empresas', 'recrutadores']
    }

    def 'fks do recrutador sao simples e nao apagam referencias em cascata'() {
        expect:
        conexao.createStatement().withCloseable { comando ->
            comando.executeQuery('''SELECT a.attname AS coluna, c.confrelid::regclass::text AS destino,
                                           cardinality(c.conkey) AS colunas, c.confdeltype
                                    FROM pg_constraint c
                                    JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = c.conkey[1]
                                    WHERE c.conrelid = 'recrutadores'::regclass AND c.contype = 'f'
                                    ORDER BY a.attname''').withCloseable { resultado ->
                assert resultado.next()
                assert resultado.getString('coluna') == 'id_area_recrutamento'
                assert resultado.getString('destino') == 'areas_recrutamento'
                assert resultado.getInt('colunas') == 1
                assert resultado.getString('confdeltype') == 'a'
                assert resultado.next()
                assert resultado.getString('coluna') == 'id_empresa'
                assert resultado.getString('destino') == 'empresas'
                assert resultado.getInt('colunas') == 1
                assert resultado.getString('confdeltype') == 'a'
                !resultado.next()
            }
        }
    }

    def 'persiste vaga de origem #origem com propriedade empresarial'() {
        given:
        Long autor = origem == 'recrutador' ? inserirRecrutador(empresa, area) : null

        when:
        long vaga = origem == 'empresa sem coluna de autor' ?
                inserir('''INSERT INTO vagas (nome, descricao, local, id_empresa)
                           VALUES (?, ?, ?, ?) RETURNING id''', ['Vaga direta', 'Descrição', 'Local', empresa]) :
                inserirVaga(empresa, autor)

        then:
        conexao.prepareStatement('SELECT id_empresa, id_recrutador FROM vagas WHERE id = ?').withCloseable { comando ->
            comando.setLong(1, vaga)
            comando.executeQuery().withCloseable { resultado ->
                assert resultado.next()
                assert resultado.getLong('id_empresa') == empresa
                assert resultado.getObject('id_recrutador') == autor
                !resultado.next()
            }
        }

        where:
        origem << ['empresa sem coluna de autor', 'empresa com autor nulo', 'recrutador']
    }

    def 'rejeita autoria com #cenario na #operacao de vaga'() {
        given:
        long autor = inserirRecrutador(empresa, area)
        long outraEmpresa = inserir('''INSERT INTO empresas (nome, cnpj, email_corporativo, id_pais, cep)
                                      VALUES (?, ?, ?, (SELECT id FROM pais LIMIT 1), ?) RETURNING id''',
                ['Outra empresa', 'outro-cnpj', 'outra@example.invalid', '00000000'])
        Long proprietaria = cenario == 'empresa divergente' ? outraEmpresa :
                cenario == 'empresa ausente' ? null : cenario == 'empresa inexistente' ? -1L : empresa
        Long idAutor = cenario == 'autor inexistente' ? -1L : autor
        long vaga = inserirVaga(empresa, null)

        when:
        if (operacao == 'insercao') {
            inserirVaga(proprietaria, idAutor)
        } else {
            conexao.prepareStatement('UPDATE vagas SET id_empresa = ?, id_recrutador = ? WHERE id = ?').withCloseable { comando ->
                comando.setObject(1, proprietaria)
                comando.setLong(2, idAutor)
                comando.setLong(3, vaga)
                comando.executeUpdate()
            }
        }

        then:
        SQLException erro = thrown()
        erro.SQLState == estado

        where:
        cenario               | operacao      | estado
        'empresa divergente'  | 'insercao'    | '23503'
        'autor inexistente'   | 'insercao'    | '23503'
        'empresa ausente'     | 'insercao'    | '23502'
        'empresa inexistente' | 'insercao'    | '23503'
        'empresa divergente'  | 'atualizacao' | '23503'
        'autor inexistente'   | 'atualizacao' | '23503'
    }

    def 'inativar recrutador preserva autoria e empresa da vaga'() {
        given:
        long autor = inserirRecrutador(empresa, area)
        long vaga = inserirVaga(empresa, autor)

        when:
        conexao.prepareStatement('UPDATE recrutadores SET ativo = FALSE WHERE id = ?').withCloseable { comando ->
            comando.setLong(1, autor)
            assert comando.executeUpdate() == 1
        }

        then:
        conexao.prepareStatement('''SELECT v.id_empresa, v.id_recrutador, r.ativo FROM vagas v
                                   JOIN recrutadores r ON r.id = v.id_recrutador WHERE v.id = ?''').withCloseable { comando ->
            comando.setLong(1, vaga)
            comando.executeQuery().withCloseable { resultado ->
                assert resultado.next()
                assert resultado.getLong('id_empresa') == empresa
                assert resultado.getLong('id_recrutador') == autor
                assert !resultado.getBoolean('ativo')
                !resultado.next()
            }
        }
    }

    def 'fk de autoria e composta e preserva referencias sem cascades'() {
        expect:
        conexao.createStatement().withCloseable { comando ->
            comando.executeQuery('''SELECT pg_get_constraintdef(oid) AS definicao, confdeltype, confupdtype
                                    FROM pg_constraint WHERE conrelid = 'vagas'::regclass
                                    AND conname = 'fk_vaga_recrutador_empresa' ''').withCloseable { resultado ->
                assert resultado.next()
                assert resultado.getString('definicao') ==
                        'FOREIGN KEY (id_recrutador, id_empresa) REFERENCES recrutadores(id, id_empresa)'
                assert resultado.getString('confdeltype') == 'a'
                assert resultado.getString('confupdtype') == 'a'
                !resultado.next()
            }
        }
    }

    def 'chave unica da vaga suporta referencia composta de empresa'() {
        expect:
        conexao.createStatement().withCloseable { comando ->
            comando.executeQuery('''SELECT pg_get_constraintdef(oid) AS definicao FROM pg_constraint
                                    WHERE conrelid = 'vagas'::regclass AND contype = 'u'
                                    AND conname = 'uk_vaga_empresa' ''').withCloseable { resultado ->
                assert resultado.next()
                assert resultado.getString('definicao') == 'UNIQUE (id, id_empresa)'
                !resultado.next()
            }
        }
    }

    def 'rejeita duplicata ativa de #coluna em #tabela na #operacao'() {
        given:
        inserirConta(tabela, 'original')
        long outra = inserirConta(tabela, 'outra', operacao != 'reativacao')
        if (operacao == 'reativacao') {
            executar("UPDATE ${tabela} SET ${coluna} = ? WHERE id = ?".toString(), ['original', outra])
        }

        when:
        if (operacao == 'insercao') {
            inserirConta(tabela, 'terceira', true, [(coluna): 'original'])
        } else if (operacao == 'atualizacao') {
            executar("UPDATE ${tabela} SET ${coluna} = ? WHERE id = ?".toString(), ['original', outra])
        } else {
            executar("UPDATE ${tabela} SET ativo = TRUE WHERE id = ?".toString(), [outra])
        }

        then:
        SQLException erro = thrown()
        erro.SQLState == '23505'

        where:
        [tabela, coluna, operacao] << [
                [['candidatos', 'cpf'], ['candidatos', 'email'],
                 ['empresas', 'cnpj'], ['empresas', 'email_corporativo'],
                 ['recrutadores', 'cpf'], ['recrutadores', 'email']],
                ['insercao', 'atualizacao', 'reativacao']
        ].combinations().collect { par, acao -> [par[0], par[1], acao] }
    }

    def 'permite reutilizar identificadores inativos em #tabela com novos ids'() {
        given:
        long antiga = inserirConta(tabela, 'reutilizado')
        executar("UPDATE ${tabela} SET ativo = FALSE WHERE id = ?".toString(), [antiga])

        when:
        long nova = inserirConta(tabela, 'reutilizado')
        long outraInativa = inserirConta(tabela, 'reutilizado', false)

        then:
        [antiga, nova, outraInativa].toSet().size() == 3
        consultar("SELECT id, ativo FROM ${tabela} WHERE ${coluna} = ? ORDER BY id".toString(), ['reutilizado']) == [
                [id: antiga, ativo: false], [id: nova, ativo: true], [id: outraInativa, ativo: false]]

        where:
        tabela         | coluna
        'candidatos'   | 'cpf'
        'empresas'     | 'cnpj'
        'recrutadores' | 'cpf'
    }

    def 'cpf coincidente entre candidato e recrutador ativos e permitido'() {
        when:
        long candidato = inserirConta('candidatos', 'compartilhado', true, [email: 'pessoal@example.invalid'])
        long recrutador = inserirConta('recrutadores', 'compartilhado', true, [email: 'corporativo@example.invalid'])

        then:
        consultar('SELECT cpf, ativo FROM candidatos WHERE id = ?', [candidato]) == [[cpf: 'compartilhado', ativo: true]]
        consultar('SELECT cpf, ativo FROM recrutadores WHERE id = ?', [recrutador]) == [[cpf: 'compartilhado', ativo: true]]
    }

    def 'reutilizar identificadores nao transfere vagas recrutadores ou matches historicos'() {
        given:
        long antigaEmpresa = inserirConta('empresas', 'historica-empresa')
        long antigoCandidato = inserirConta('candidatos', 'historico-candidato')
        long antigoAutor = inserirConta('recrutadores', 'historico-autor', true, [id_empresa: antigaEmpresa])
        long vaga = inserirVaga(antigaEmpresa, antigoAutor)
        inserir('INSERT INTO vagas_curtidas_candidato (id_candidato, id_vaga) VALUES (?, ?) RETURNING id', [antigoCandidato, vaga])
        inserir('INSERT INTO candidatos_curtidos_empresa (id_empresa, id_candidato, id_vaga) VALUES (?, ?, ?) RETURNING id',
                [antigaEmpresa, antigoCandidato, vaga])
        long match = inserir('INSERT INTO match (id_candidato, id_empresa, id_vaga) VALUES (?, ?, ?) RETURNING id',
                [antigoCandidato, antigaEmpresa, vaga])
        executar('UPDATE empresas SET ativo = FALSE WHERE id = ?', [antigaEmpresa])
        executar('UPDATE candidatos SET ativo = FALSE WHERE id = ?', [antigoCandidato])
        executar('UPDATE recrutadores SET ativo = FALSE WHERE id = ?', [antigoAutor])

        when:
        long novaEmpresa = inserirConta('empresas', 'historica-empresa')
        long novoCandidato = inserirConta('candidatos', 'historico-candidato')
        long novoAutor = inserirConta('recrutadores', 'historico-autor', true, [id_empresa: novaEmpresa])

        then:
        novaEmpresa != antigaEmpresa
        novoCandidato != antigoCandidato
        novoAutor != antigoAutor
        consultar('SELECT id_empresa, id_recrutador FROM vagas WHERE id = ?', [vaga]) ==
                [[id_empresa: antigaEmpresa, id_recrutador: antigoAutor]]
        consultar('SELECT id_empresa FROM recrutadores WHERE id = ?', [antigoAutor]) == [[id_empresa: antigaEmpresa]]
        consultar('SELECT id_candidato, id_empresa, id_vaga FROM match WHERE id = ?', [match]) ==
                [[id_candidato: antigoCandidato, id_empresa: antigaEmpresa, id_vaga: vaga]]
        consultar('SELECT id_vaga FROM vagas_curtidas_candidato WHERE id_candidato = ?', [antigoCandidato]) == [[id_vaga: vaga]]
        consultar('SELECT id_candidato FROM candidatos_curtidos_empresa WHERE id_empresa = ?', [antigaEmpresa]) ==
                [[id_candidato: antigoCandidato]]
        consultar('SELECT id FROM vagas WHERE id_empresa = ? OR id_recrutador = ?', [novaEmpresa, novoAutor]).empty
        consultar('SELECT id FROM recrutadores WHERE id_empresa = ?', [novaEmpresa]) == [[id: novoAutor]]
        consultar('SELECT id FROM match WHERE id_empresa = ? OR id_candidato = ?', [novaEmpresa, novoCandidato]).empty
        consultar('SELECT id FROM vagas_curtidas_candidato WHERE id_candidato = ?', [novoCandidato]).empty
        consultar('SELECT id FROM candidatos_curtidos_empresa WHERE id_empresa = ? OR id_candidato = ?', [novaEmpresa, novoCandidato]).empty
    }

    def 'interesse inicial e unico e nao gera retribuicao ou match'() {
        given:
        long candidato = inserirConta('candidatos', 'interessado')
        long vaga = inserirVaga(empresa, null)

        when:
        inserirInteresse(candidato, vaga)
        conexao.prepareStatement('''INSERT INTO vagas_curtidas_candidato (id_candidato, id_vaga)
                                   VALUES (?, ?) ON CONFLICT (id_candidato, id_vaga) DO NOTHING''').withCloseable { comando ->
            comando.setLong(1, candidato)
            comando.setLong(2, vaga)
            assert comando.executeUpdate() == 0
        }

        then:
        consultar('SELECT id_candidato, id_vaga FROM vagas_curtidas_candidato', []) == [[id_candidato: candidato, id_vaga: vaga]]
        consultar('SELECT id FROM candidatos_curtidos_empresa', []).empty
        consultar('SELECT id FROM match', []).empty
    }

    def 'match exige reciprocidade no contexto exato: #cenario'() {
        given:
        long candidato = inserirConta('candidatos', 'interessado')
        long vaga = inserirVaga(empresa, null)
        long outraVaga = inserirVaga(empresa, null)
        long outraEmpresa = inserirConta('empresas', 'outra')
        if (cenario != 'sem interesse') {
            inserirInteresse(candidato, vaga)
        }
        if (cenario in ['retribuicao em outra vaga', 'empresa divergente']) {
            inserirInteresse(candidato, outraVaga)
            inserirRetribuicao(candidato, empresa, cenario == 'empresa divergente' ? vaga : outraVaga, null)
        }

        when:
        inserirMatch(candidato, cenario == 'empresa divergente' ? outraEmpresa : empresa, vaga)

        then:
        SQLException erro = thrown()
        erro.SQLState == '23503'

        where:
        cenario << ['sem interesse', 'sem retribuicao', 'retribuicao em outra vaga', 'empresa divergente']
    }

    def 'retribuicao rejeita #cenario'() {
        given:
        long candidato = inserirConta('candidatos', 'interessado')
        long vaga = inserirVaga(empresa, null)
        long outraEmpresa = inserirConta('empresas', 'outra')
        long outroAtor = inserirRecrutador(outraEmpresa, area)
        if (cenario != 'interesse ausente') {
            inserirInteresse(candidato, vaga)
        }

        when:
        inserirRetribuicao(candidato, cenario == 'empresa divergente' ? outraEmpresa : empresa,
                cenario == 'vaga ausente' ? null : vaga,
                cenario == 'ator de outra empresa' ? outroAtor : cenario == 'ator inexistente' ? -1L : null)

        then:
        SQLException erro = thrown()
        erro.SQLState == estado

        where:
        cenario                 | estado
        'interesse ausente'      | '23503'
        'empresa divergente'     | '23503'
        'ator de outra empresa'  | '23503'
        'ator inexistente'       | '23503'
        'vaga ausente'           | '23502'
    }

    def 'retribuicao por #ator preserva autor e forma um match somente na vaga retribuida'() {
        given:
        long candidato = inserirConta('candidatos', 'interessado')
        Long autor = ator == 'empresa em vaga direta' ? null : inserirRecrutador(empresa, area)
        long vagaA = inserirVaga(empresa, autor)
        long vagaB = inserirVaga(empresa, null)
        inserirInteresse(candidato, vagaA)
        inserirInteresse(candidato, vagaB)

        when:
        inserirRetribuicao(candidato, empresa, vagaA, ator == 'recrutador' ? autor : null)
        inserirMatch(candidato, empresa, vagaA)
        ['candidatos_curtidos_empresa', 'match'].each { tabela ->
            conexao.prepareStatement("""INSERT INTO ${tabela} (id_candidato, id_empresa, id_vaga)
                                        VALUES (?, ?, ?) ON CONFLICT (id_candidato, id_vaga) DO NOTHING""".toString()).withCloseable { comando ->
                comando.setLong(1, candidato)
                comando.setLong(2, empresa)
                comando.setLong(3, vagaA)
                assert comando.executeUpdate() == 0
            }
        }

        then:
        consultar('SELECT id_candidato, id_empresa, id_vaga FROM match', []) ==
                [[id_candidato: candidato, id_empresa: empresa, id_vaga: vagaA]]
        consultar('SELECT id_recrutador_ator FROM candidatos_curtidos_empresa', []) ==
                [[id_recrutador_ator: ator == 'recrutador' ? autor : null]]
        consultar('SELECT id_recrutador FROM vagas WHERE id = ?', [vagaA]) == [[id_recrutador: autor]]
        consultar('SELECT id FROM match WHERE id_vaga = ?', [vagaB]).empty

        when:
        inserirRetribuicao(candidato, empresa, vagaB, null)
        inserirMatch(candidato, empresa, vagaB)

        then:
        consultar('SELECT id_vaga FROM match ORDER BY id_vaga', []) == [[id_vaga: vagaA], [id_vaga: vagaB]]

        where:
        ator << ['empresa em vaga direta', 'empresa em vaga autoral', 'recrutador']
    }

    def 'unicidade rejeita duplicata de candidato vaga em #tabela'() {
        given:
        long candidato = inserirConta('candidatos', 'interessado')
        long vaga = inserirVaga(empresa, null)
        inserirInteresse(candidato, vaga)
        inserirRetribuicao(candidato, empresa, vaga, null)
        inserirMatch(candidato, empresa, vaga)

        when:
        inserir("INSERT INTO ${tabela} (id_candidato, id_empresa, id_vaga) VALUES (?, ?, ?) RETURNING id".toString(),
                [candidato, empresa, vaga])

        then:
        SQLException erro = thrown()
        erro.SQLState == '23505'

        where:
        tabela << ['candidatos_curtidos_empresa', 'match']
    }

    def 'referencias impedem romper reciprocidade ou propriedade por #alteracao'() {
        given:
        long candidato = inserirConta('candidatos', 'interessado')
        long vaga = inserirVaga(empresa, null)
        long outraEmpresa = inserirConta('empresas', 'outra')
        inserirInteresse(candidato, vaga)
        inserirRetribuicao(candidato, empresa, vaga, null)
        inserirMatch(candidato, empresa, vaga)

        when:
        conexao.prepareStatement(sql).withCloseable { comando ->
            if (alteracao == 'troca de proprietaria') {
                comando.setLong(1, outraEmpresa)
            }
            comando.executeUpdate()
        }

        then:
        SQLException erro = thrown()
        erro.SQLState == '23503'

        where:
        alteracao                  | sql
        'remocao de interesse'      | 'DELETE FROM vagas_curtidas_candidato'
        'remocao de retribuicao'    | 'DELETE FROM candidatos_curtidos_empresa'
        'troca de proprietaria'    | 'UPDATE vagas SET id_empresa = ?'
        'exclusao direta da vaga'  | 'DELETE FROM vagas'
    }

    def 'fks novas preservam no action e cascades existentes continuam declarados'() {
        expect:
        consultar('''SELECT conname, confdeltype::text AS exclusao, confupdtype::text AS atualizacao
                     FROM pg_constraint WHERE connamespace = current_schema()::regnamespace
                     AND conname IN ('fk_retribuicao_interesse', 'fk_retribuicao_vaga_empresa',
                                     'fk_retribuicao_recrutador_empresa', 'fk_match_vaga_empresa', 'fk_match_retribuicao')
                     ORDER BY conname''', []) == [
                'fk_match_retribuicao', 'fk_match_vaga_empresa', 'fk_retribuicao_interesse',
                'fk_retribuicao_recrutador_empresa', 'fk_retribuicao_vaga_empresa'
        ].collect { [conname: it, exclusao: 'a', atualizacao: 'a'] }
        consultar('''SELECT conname, confdeltype::text AS exclusao FROM pg_constraint
                     WHERE connamespace = current_schema()::regnamespace
                     AND conname IN ('vagas_competencias_id_vaga_fkey', 'vagas_curtidas_candidato_id_vaga_fkey', 'match_id_vaga_fkey')
                     ORDER BY conname''', []) == [
                'match_id_vaga_fkey', 'vagas_competencias_id_vaga_fkey', 'vagas_curtidas_candidato_id_vaga_fkey'
        ].collect { [conname: it, exclusao: 'c'] }
    }

    private long inserirInteresse(long candidato, long vaga) {
        inserir('INSERT INTO vagas_curtidas_candidato (id_candidato, id_vaga) VALUES (?, ?) RETURNING id', [candidato, vaga])
    }

    private long inserirRetribuicao(long candidato, long proprietaria, Long vaga, Long ator) {
        inserir('''INSERT INTO candidatos_curtidos_empresa (id_candidato, id_empresa, id_vaga, id_recrutador_ator)
                   VALUES (?, ?, ?, ?) RETURNING id''', [candidato, proprietaria, vaga, ator])
    }

    private long inserirMatch(long candidato, long proprietaria, long vaga) {
        inserir('INSERT INTO match (id_candidato, id_empresa, id_vaga) VALUES (?, ?, ?) RETURNING id', [candidato, proprietaria, vaga])
    }

    private long inserirConta(String tabela, String identificador, boolean ativo = true, Map alteracoes = [:]) {
        Map campos
        if (tabela == 'candidatos') {
            campos = [nome: 'Candidato', sobrenome: 'Teste', email: identificador, cpf: identificador,
                      senha: 'fixture-sem-login', data_nascimento: java.sql.Date.valueOf('1990-01-01'),
                      id_pais: consultar('SELECT id FROM pais', [])[0].id, cep: '00000000']
        } else if (tabela == 'empresas') {
            campos = [nome: 'Empresa', cnpj: identificador, email_corporativo: identificador,
                      id_pais: consultar('SELECT id FROM pais', [])[0].id, cep: '00000000']
        } else {
            assert tabela == 'recrutadores'
            campos = [nome: 'Recrutador', cpf: identificador, email: identificador, id_empresa: empresa,
                      id_area_recrutamento: area, senha: 'fixture-sem-login', data_nascimento: java.sql.Date.valueOf('1990-01-01')]
        }
        campos.ativo = ativo
        campos.putAll(alteracoes)
        inserir("INSERT INTO ${tabela} (${campos.keySet().join(', ')}) VALUES (${campos.collect { '?' }.join(', ')}) RETURNING id".toString(),
                campos.values().toList())
    }

    private void executar(String sql, List parametros) {
        conexao.prepareStatement(sql).withCloseable { comando ->
            parametros.eachWithIndex { valor, indice -> comando.setObject(indice + 1, valor) }
            int quantidade = comando.executeUpdate()
            assert quantidade == 1
        }
    }

    private List<Map> consultar(String sql, List parametros) {
        conexao.prepareStatement(sql).withCloseable { comando ->
            parametros.eachWithIndex { valor, indice -> comando.setObject(indice + 1, valor) }
            comando.executeQuery().withCloseable { resultado ->
                List<Map> linhas = []
                while (resultado.next()) {
                    Map linha = [:]
                    (1..resultado.metaData.columnCount).each { indice ->
                        linha[resultado.metaData.getColumnLabel(indice)] = resultado.getObject(indice)
                    }
                    linhas << linha
                }
                linhas
            }
        }
    }

    private long inserirVaga(Long idEmpresa, Long idRecrutador) {
        inserir('''INSERT INTO vagas (nome, descricao, local, id_empresa, id_recrutador)
                   VALUES (?, ?, ?, ?, ?) RETURNING id''', ['Vaga de teste', 'Descrição', 'Local', idEmpresa, idRecrutador])
    }

    private long inserirRecrutador(Long idEmpresa, Long idArea, String identificador = 'unico') {
        inserir('''INSERT INTO recrutadores (nome, id_area_recrutamento, descricao, email, cpf, data_nascimento, senha, id_empresa)
                   VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id''',
                ['Recrutador de teste', idArea, 'Perfil de RH', "${identificador}@example.invalid".toString(),
                 "cpf-${identificador}".toString(), java.sql.Date.valueOf('1990-01-01'), 'fixture-sem-login', idEmpresa])
    }

    private long inserir(String sql, List parametros) {
        conexao.prepareStatement(sql).withCloseable { comando ->
            parametros.eachWithIndex { valor, indice -> comando.setObject(indice + 1, valor) }
            comando.executeQuery().withCloseable { resultado ->
                assert resultado.next()
                resultado.getLong(1)
            }
        }
    }
}

class SegurancaSuportePostgresTest extends Specification {

    def 'bloqueia destino inseguro ou configuracao incompleta antes de conectar'() {
        when:
        SuportePostgres.abrir(ambiente)

        then:
        thrown(IllegalArgumentException)

        where:
        ambiente << [
                [:],
                [(SuportePostgres.URL): 'jdbc:postgresql://localhost:5432/linketinder_test'],
                configuracao('jdbc:postgresql://localhost:5432/linketinder'),
                configuracao('jdbc:postgresql://localhost:5432/postgres'),
                configuracao('jdbc:postgresql://localhost:5432/linketinder_test?currentSchema=public'),
                configuracao('jdbc:postgresql://localhost:5432/linketinder_test/../linketinder')
        ]
    }

    def 'aceita apenas configuracao explicita de banco dedicado'() {
        expect:
        !SuportePostgres.configurado([:])
        SuportePostgres.configurado([(SuportePostgres.USUARIO): 'usuario-de-teste'])
        SuportePostgres.validarConfiguracao(configuracao('jdbc:postgresql://127.0.0.1:5432/linketinder_test')) == 'linketinder_test'
        SuportePostgres.validarConfiguracao(configuracao('jdbc:postgresql://localhost:5432/linketinder_test_t01')) == 'linketinder_test_t01'
    }

    private static Map<String, String> configuracao(String url) {
        [(SuportePostgres.URL): url, (SuportePostgres.USUARIO): 'usuario-de-teste',
         (SuportePostgres.SENHA): 'valor-sintetico-sem-conexao']
    }
}
