package com.jpcode.service

import spock.lang.Specification

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class SenhaServiceTest extends Specification {
    SenhaService service = new SenhaService()

    def 'deriva com parametros aprovados e salt individual sem guardar o segredo'() {
        when:
        String primeira = service.derivar('segredo de teste ç漢字')
        String segunda = service.derivar('segredo de teste ç漢字')
        String[] partes = primeira.split('\\$')

        then:
        primeira != segunda
        partes[0..2] == ['v1', 'PBKDF2WithHmacSHA256', '600000']
        partes[3] != segunda.split('\\$')[3]
        Base64.decoder.decode(partes[3]).length == 16
        Base64.decoder.decode(partes[4]).length == 32
        !primeira.contains('segredo')
        service.verificar('segredo de teste ç漢字', primeira)
        service.verificar('segredo de teste ç漢字', segunda)
        !service.verificar('senha incorreta', primeira)
    }

    def 'verifica representacao calculada independentemente pela API Java'() {
        given:
        byte[] salt = (0..<16).collect { (byte) it } as byte[]
        PBEKeySpec spec = new PBEKeySpec('senha de teste'.toCharArray(), salt, 600_000, 256)
        byte[] hash = SecretKeyFactory.getInstance('PBKDF2WithHmacSHA256').generateSecret(spec).encoded
        spec.clearPassword()
        String representacao = 'v1$PBKDF2WithHmacSHA256$600000$' +
                Base64.encoder.encodeToString(salt) + '$' + Base64.encoder.encodeToString(hash)

        expect:
        service.verificar('senha de teste', representacao)
        !service.verificar(representacao, representacao)
    }

    def 'rejeita formato ou custo invalido sem fallback: #caso'() {
        expect:
        !service.verificar('segredo', representacao)

        where:
        caso             | representacao
        'nulo'           | null
        'vazio'          | ''
        'texto simples'  | 'segredo'
        'versao'         | formato('v2', 'PBKDF2WithHmacSHA256', '600000')
        'algoritmo'      | formato('v1', 'PBKDF2WithHmacSHA1', '600000')
        'custo baixo'    | formato('v1', 'PBKDF2WithHmacSHA256', '1')
        'custo excessivo'| formato('v1', 'PBKDF2WithHmacSHA256', '2147483647')
        'overflow'       | formato('v1', 'PBKDF2WithHmacSHA256', '999999999999999999')
        'base64'         | 'v1$PBKDF2WithHmacSHA256$600000$!$!'
        'salt curto'     | 'v1$PBKDF2WithHmacSHA256$600000$AA==$AA=='
        'campo extra'    | formato('v1', 'PBKDF2WithHmacSHA256', '600000') + '$extra'
        'muito longo'    | 'x' * 121
        'nao canonico'   | 'v1$PBKDF2WithHmacSHA256$600000$AAAAAAAAAAAAAAAAAAAAAB==$' + 'A' * 43 + '='
    }

    def 'rejeita senha ausente sem expor credenciais'() {
        when:
        service.derivar(senha)

        then:
        IllegalArgumentException erro = thrown()
        erro.message == 'Senha obrigatória.'
        !service.verificar(senha, formato('v1', 'PBKDF2WithHmacSHA256', '600000'))

        where:
        senha << [null, '']
    }

    private static String formato(String versao, String algoritmo, String custo) {
        versao + '$' + algoritmo + '$' + custo + '$' + 'A' * 22 + '==$' + 'A' * 43 + '='
    }
}
