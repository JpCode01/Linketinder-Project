package com.jpcode.service

import groovy.transform.CompileStatic

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.security.MessageDigest
import java.security.SecureRandom

@CompileStatic
class SenhaService {
    private static final String ALGORITMO = 'PBKDF2WithHmacSHA256'
    private static final int ITERACOES = 600_000
    private static final int SALT_BYTES = 16
    private static final int CHAVE_BITS = 256
    private final SecureRandom random = new SecureRandom()

    String derivar(String senha) {
        if (senha == null || senha.isEmpty()) {
            throw new IllegalArgumentException('Senha obrigatória.')
        }
        byte[] salt = new byte[SALT_BYTES]
        random.nextBytes(salt)
        byte[] hash = null
        try {
            hash = derivarBytes(senha, salt)
            return 'v1$' + ALGORITMO + '$' + ITERACOES + '$' +
                    Base64.encoder.encodeToString(salt) + '$' + Base64.encoder.encodeToString(hash)
        } finally {
            Arrays.fill(salt, (byte) 0)
            if (hash != null) Arrays.fill(hash, (byte) 0)
        }
    }

    boolean verificar(String senha, String representacao) {
        if (senha == null || senha.isEmpty() || representacao == null || representacao.length() > 120) {
            return false
        }
        String[] partes = representacao.split('\\$', -1)
        if (partes.length != 5 || partes[0] != 'v1' || partes[1] != ALGORITMO ||
                partes[2] != Integer.toString(ITERACOES) ||
                !(partes[3] ==~ /[A-Za-z0-9+\/]{22}==/) ||
                !(partes[4] ==~ /[A-Za-z0-9+\/]{43}=/)) {
            return false
        }
        byte[] salt = null
        byte[] esperado = null
        byte[] calculado = null
        try {
            salt = Base64.decoder.decode(partes[3])
            esperado = Base64.decoder.decode(partes[4])
            if (salt.length != SALT_BYTES || esperado.length != CHAVE_BITS / 8 ||
                    Base64.encoder.encodeToString(salt) != partes[3] ||
                    Base64.encoder.encodeToString(esperado) != partes[4]) {
                return false
            }
            calculado = derivarBytes(senha, salt)
            return MessageDigest.isEqual(esperado, calculado)
        } catch (IllegalArgumentException ignored) {
            return false
        } finally {
            if (salt != null) Arrays.fill(salt, (byte) 0)
            if (esperado != null) Arrays.fill(esperado, (byte) 0)
            if (calculado != null) Arrays.fill(calculado, (byte) 0)
        }
    }

    private static byte[] derivarBytes(String senha, byte[] salt) {
        char[] caracteres = senha.toCharArray()
        PBEKeySpec spec = new PBEKeySpec(caracteres, salt, ITERACOES, CHAVE_BITS)
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
            Arrays.fill(caracteres, (char) 0)
        }
    }
}
