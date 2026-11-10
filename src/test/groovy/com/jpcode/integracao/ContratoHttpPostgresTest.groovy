package com.jpcode.integracao

class ContratoHttpPostgresTest extends SuporteHttpPostgres {
    def 'jornada HTTP preserva persistencia escopos anonimato reciprocidade e autoria historica'() {
        given:
        def empresa = cliente()
        def outraEmpresa = cliente()
        def autor = cliente()
        def colega = cliente()
        def candidato = cliente()
        Map cadastroEmpresa = [nome: 'Empresa', email: 'empresa@teste', senha: 'senha-t11', cnpj: 'cnpj-a',
                               pais: 'BRASIL', estado: 'SP', cep: 'cep', descricao: 'Descrição empresarial']
        Map cadastroRecrutador = [nome: 'Autor', cpf: 'cpf-r1', email: 'autor@teste', senha: 'senha-t11',
                                  dataNascimento: '1990-01-01', descricao: 'Perfil privado', idAreaRecrutamento: 1]
        Map cadastroVaga = [nome: 'Nome privado', descricao: 'Vaga anônima', local: 'Local privado', competencias: ['RH']]
        Map cadastroCandidato = [nome: 'Pessoa', sobrenome: 'Candidata', email: 'candidato@teste', senha: 'senha-t11',
                                 cpf: 'cpf-c', dataNascimento: '1990-01-01', pais: 'BRASIL', idade: 36, estado: 'SP',
                                 cep: 'cep', descricao: 'Candidato anônimo', competencias: ['RH']]

        expect: 'cadastros e referências persistidas'
        enviar(empresa, 'POST', '/empresas', cadastroEmpresa).statusCode() == 201
        enviar(outraEmpresa, 'POST', '/empresas', cadastroEmpresa + [email: 'outra@teste', cnpj: 'cnpj-b']).statusCode() == 201
        enviar(candidato, 'POST', '/candidatos', cadastroCandidato).statusCode() == 201
        json(enviar(empresa, 'GET', '/referencias/paises')) == [[id: 1, nome: 'BRASIL']]
        json(enviar(empresa, 'GET', '/referencias/estados')) == [[id: 1, sigla: 'SP']]
        json(enviar(empresa, 'GET', '/areas-recrutamento')) == [[id: 1, nome: 'Recursos Humanos']]
        json(enviar(empresa, 'GET', '/competencias')) == ['RH']
        login(empresa, 'empresa', 'empresa@teste').statusCode() == 200
        login(outraEmpresa, 'empresa', 'outra@teste').statusCode() == 200
        enviar(empresa, 'POST', '/empresas/1/recrutadores', cadastroRecrutador).statusCode() == 201
        enviar(empresa, 'POST', '/empresas/1/recrutadores', cadastroRecrutador + [nome: 'Colega', cpf: 'cpf-r2', email: 'colega@teste']).statusCode() == 201
        enviar(empresa, 'POST', '/empresas/2/recrutadores', cadastroRecrutador).statusCode() == 403
        enviar(empresa, 'POST', '/empresas/1/recrutadores', cadastroRecrutador + [cpf: 'cpf-r3', email: 'novo@teste', idAreaRecrutamento: 999]).statusCode() == 400
        enviar(empresa, 'POST', '/empresas/1/recrutadores', cadastroRecrutador + [cpf: 'cpf-r3', email: 'candidato@teste']).statusCode() == 409
        login(autor, 'recrutador', 'autor@teste').statusCode() == 200
        login(colega, 'recrutador', 'colega@teste').statusCode() == 200
        login(candidato, 'candidato', 'candidato@teste').statusCode() == 200
        json(enviar(autor, 'GET', '/recrutadores/1')).keySet() == ['id', 'nome', 'cpf', 'email', 'dataNascimento', 'descricao', 'idEmpresa', 'areaRecrutamento'] as Set
        enviar(colega, 'GET', '/recrutadores/1').statusCode() == 403
        enviar(autor, 'POST', '/empresas/1/recrutadores', cadastroRecrutador).statusCode() == 403

        and: 'criação direta/autoral e rollback de referência inválida'
        enviar(autor, 'POST', '/recrutadores/1/vagas', cadastroVaga).statusCode() == 201
        enviar(colega, 'POST', '/recrutadores/2/vagas', cadastroVaga).statusCode() == 201
        enviar(empresa, 'POST', '/vagas', cadastroVaga + [idEmpresa: 1]).statusCode() == 201
        enviar(autor, 'POST', '/recrutadores/1/vagas', cadastroVaga + [competencias: ['Inexistente']]).statusCode() == 400
        quantidade('vagas') == 3
        quantidade('vagas_competencias') == 3
        quantidade('candidatos_competencias') == 1
        json(enviar(autor, 'GET', '/vagas/recrutador/1'))*.id == [1]
        json(enviar(colega, 'GET', '/vagas/recrutador/2'))*.id == [2]
        json(enviar(empresa, 'GET', '/vagas/empresa/1'))*.id == [1, 2, 3]
        enviar(colega, 'GET', '/vagas/recrutador/1').statusCode() == 403
        enviar(outraEmpresa, 'PUT', '/vagas/1', cadastroVaga.findAll { it.key != 'competencias' }).statusCode() == 403
        enviar(empresa, 'PUT', '/vagas/1', [nome: 'Atualizada', descricao: 'Vaga anônima', local: 'Local privado']).statusCode() == 200

        and: 'interesse único sem match e descoberta com chaves exatas'
        enviar(candidato, 'POST', '/curtidas/vagas', [idVaga: 1]).statusCode() == 204
        enviar(candidato, 'POST', '/curtidas/vagas', [idVaga: 1]).statusCode() == 204
        enviar(candidato, 'POST', '/curtidas/vagas', [idVaga: 2]).statusCode() == 204
        quantidade('vagas_curtidas_candidato') == 2
        quantidade('match') == 0
        json(enviar(candidato, 'GET', '/vagas')).every { it.keySet() == ['id', 'descricao', 'competencias'] as Set }
        json(enviar(candidato, 'GET', '/vagas/1')).keySet() == ['id', 'descricao', 'competencias'] as Set
        json(enviar(candidato, 'GET', '/curtidas/vagas/candidato/1')).size() == 2
        json(enviar(empresa, 'GET', '/curtidas/candidatos/vaga/1')) == json(enviar(autor, 'GET', '/curtidas/candidatos/vaga/1'))
        json(enviar(autor, 'GET', '/curtidas/candidatos/vaga/1'))[0].keySet() == ['id', 'descricao', 'competencias'] as Set
        enviar(colega, 'GET', '/curtidas/candidatos/vaga/1').statusCode() == 403
        enviar(outraEmpresa, 'GET', '/curtidas/candidatos/vaga/1').statusCode() == 403
        enviar(empresa, 'POST', '/curtidas/candidatos', [idCandidato: 1, idVaga: 3]).statusCode() == 409
        enviar(outraEmpresa, 'POST', '/curtidas/candidatos', [idCandidato: 1, idVaga: 1]).statusCode() == 403
        enviar(colega, 'POST', '/curtidas/candidatos', [idCandidato: 1, idVaga: 1]).statusCode() == 403
        quantidade('match') == 0
        quantidade('candidatos_curtidos_empresa') == 0

        and: 'retribuição contextualizada, idempotência e visibilidade pós-match'
        json(enviar(empresa, 'GET', '/curtidas/candidatos/vaga/1/retribuidos')) == []
        enviar(cliente(), 'GET', '/curtidas/candidatos/vaga/1/retribuidos').statusCode() == 401
        enviar(candidato, 'GET', '/curtidas/candidatos/vaga/1/retribuidos').statusCode() == 403
        enviar(colega, 'GET', '/curtidas/candidatos/vaga/1/retribuidos').statusCode() == 403
        enviar(outraEmpresa, 'GET', '/curtidas/candidatos/vaga/1/retribuidos').statusCode() == 403
        enviar(empresa, 'POST', '/curtidas/candidatos', [idCandidato: 1, idVaga: 1]).statusCode() == 204
        enviar(autor, 'POST', '/curtidas/candidatos', [idCandidato: 1, idVaga: 1]).statusCode() == 204
        json(enviar(empresa, 'GET', '/curtidas/candidatos/vaga/1/retribuidos')) == [1]
        json(enviar(autor, 'GET', '/curtidas/candidatos/vaga/1/retribuidos')) == [1]
        json(enviar(empresa, 'GET', '/curtidas/candidatos/vaga/2/retribuidos')) == []
        enviar(empresa, 'GET', '/curtidas/candidatos/vaga/0/retribuidos').statusCode() == 400
        enviar(empresa, 'GET', '/curtidas/candidatos/vaga/999/retribuidos').statusCode() == 404
        quantidade('match') == 1
        quantidade('candidatos_curtidos_empresa') == 1
        json(enviar(colega, 'GET', '/matches/recrutador/2')) == []
        json(enviar(outraEmpresa, 'GET', '/matches/empresa/2')) == []
        json(enviar(autor, 'GET', '/matches/recrutador/1')) == json(enviar(empresa, 'GET', '/matches/empresa/1'))
        json(enviar(candidato, 'GET', '/matches/candidato/1'))[0].nomeRecrutador == 'Autor'
        json(enviar(candidato, 'GET', '/matches/candidato/1'))[0].areaRecrutamento == 'Recursos Humanos'
        json(enviar(candidato, 'GET', '/matches/candidato/1'))[0].keySet() == chavesMatch()
        json(enviar(candidato, 'GET', '/vagas/1')).keySet() == ['id', 'descricao', 'competencias'] as Set
        enviar(candidato, 'GET', '/empresas/1').statusCode() == 403
        enviar(autor, 'GET', '/candidatos/1').statusCode() == 403
        enviar(colega, 'GET', '/matches/recrutador/1').statusCode() == 403

        when: 'inatividade preparada apenas no schema dedicado'
        assert enviar(autor, 'POST', '/recrutadores/1/vagas', cadastroVaga).statusCode() == 201
        assert enviar(candidato, 'POST', '/curtidas/vagas', [idVaga: 4]).statusCode() == 204
        try (def conexao = banco.factory.getConnection(); def comando = conexao.createStatement()) {
            comando.execute('UPDATE recrutadores SET ativo = false WHERE id = 1')
        }
        then:
        enviar(autor, 'GET', '/matches/recrutador/1').statusCode() == 401
        login(autor, 'recrutador', 'autor@teste').statusCode() == 401
        json(enviar(empresa, 'GET', '/vagas/empresa/1'))[0].nomeRecrutador == 'Autor'
        json(enviar(empresa, 'GET', '/matches/empresa/1'))[0].nomeRecrutador == 'Autor'
        json(enviar(empresa, 'GET', '/curtidas/candidatos/vaga/1')).size() == 1
        json(enviar(empresa, 'GET', '/curtidas/candidatos/vaga/1/retribuidos')) == [1]
        enviar(empresa, 'POST', '/curtidas/candidatos', [idCandidato: 1, idVaga: 1]).statusCode() == 204
        enviar(empresa, 'POST', '/curtidas/candidatos', [idCandidato: 1, idVaga: 4]).statusCode() == 204
        quantidade('match') == 2
    }

}
