package ru.offer.hunt.oh_course.security;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public final class JwtTestUtil {
    private JwtTestUtil() {}

    public static String rs256Token(
            RSAKey key, String issuer, String audience, List<String> roles, List<String> scopes) {
        try {
            var now = Instant.now();
            var claims =
                    new JWTClaimsSet.Builder()
                            .issuer(issuer)
                            .audience(audience)
                            .issueTime(Date.from(now))
                            .expirationTime(Date.from(now.plusSeconds(3600)))
                            .claim("roles", roles)
                            .claim("scp", scopes)
                            .build();
            var header =
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .keyID(key.getKeyID())
                            .type(JOSEObjectType.JWT)
                            .build();
            var jwt = new SignedJWT(header, claims);
            jwt.sign(new RSASSASigner(key.toPrivateKey()));
            return jwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}