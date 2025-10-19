package ru.offer.hunt.oh_course.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;



public final class JwksTestUtil {
    private JwksTestUtil() {}

    public static RSAKey generateRsa(String keyId) {
        try {
            return new RSAKeyGenerator(2048).keyID(keyId).generate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String toJwksJson(RSAKey rsaKey) {
        return new JWKSet(rsaKey.toPublicJWK()).toString();
    }
}