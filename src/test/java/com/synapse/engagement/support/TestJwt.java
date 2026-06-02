package com.synapse.engagement.support;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * 통합 테스트용 RS256 JWT 발급기.
 * application-dev.yml의 synapse.jwt.public-key와 쌍을 이루는 테스트 개인키로 서명한다.
 */
public final class TestJwt {

    private static final String PRIVATE_KEY =
        "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCyli5uWptvswem4np3RJSQNOXrf/3ZPvg352DcyJvOurPlTwoZm3Py/qx3NsxNoM+bGUOQEuQVqzNTdib0+9IWjKbnl0hHNNkqmMs8wXJRGB93qdd8E+gCXMhvTRwZiN7p5ATVTU4BaiN7td9+Nm1Oxs3dW0Q5rp41tFba6/3kheQkO1wb+Sh4uI1vjYEXbyuGkBiYYTbiJDF8BFGqHshncTqCJaa5cgOwRpYkIq39SVRuYqlRIB+fCuJbETvyUAtcdBKYVaXZZn2u5fNQFalBkk8QY7hHFfbz3wfEDsm/lXtWqYa8SoHdlhezbDJp5ekWN+/+JY8L/7zc8zJGLpW1AgMBAAECggEAOA/wTIFSKVsU7F1Nn7JeRvTsNqVL7c6YQoh2vmiVjOzMe5B39bj4ydAMGTKRKU9xzNk9/fUIOAsyBiHhsed5uM13ud0iegQLppUnvA9oUS/W9QxS0qc3HsK8wz/8McHnZJpJsCJf+g61S+k421i/sMq1Jqe3f7oi/W37FCegYQPlrPkkXMnPGzqG/3wEkbPyxkBNTvlpFjurlzU0jftMCCYDL5J8CthE6Iu/jWZCUez5SUi5QSFpUG6lub4zTHunhEBTtah/lcxynhW5ZppiGPedxk+v2HpNGfPBv7sqRpE5h8dTbtqjCgrX7M1xEgU7wODn5VPTRTIlY1b4WopeQwKBgQD1K2Zx1W4NBoTLjYqHoATcaFZ1Q42pdojbfXDw9XbxEOYpEVu7zUj0YKWDJaB1FxijQSBfo8ZcGRI9YOOPWOS0tQY3YY/AzWG7x/QLEijHxq42+KnLE2ebsVsfMyMLy2kkMQ4w7CGHhVV048OIwrzpLMjKmiBz9oArCrcpZAHOowKBgQC6ec0qQmKeTfDo4soD3cELaxxVY9Yb1kw309LMJOOLHjMtfiHHEc9Cp2n5ekIxblPXavCREMg5AF6Dz7EqyI3/vue4HKVdfLQmx1dZudfxT0TLGfEPdlAC32gx6aK7Q1/F7CCTA0HVopVWUjCIECO7l1NmilRsd+mdnIAXH+WHxwKBgQC2ep4efhgSU9bFVs1UExNrJbGsSCKJjnNgwuYsQtdLqCNXT9cyWiJB2il3Cqt6Wz14TYIWDWUXqYV877+QMz7PDanZ0KDZhUSIKtSG5PY7c7K5sa1XPFMye/hxqXMdVUIlsOl6Glb+coxfmyMviJppB29P9RXQmhldb/VSNmBt9wKBgQCvKC/NzRODLSToK/ajkQ1+Yzr2/lMkTLPFEMQFm3TcvR5HUh36NkFfk4+Ylf1NHxvD0aBsMr5PxIgC+fipfj7bhf90UfwGh1dUwZPMJSOwd8vfltt2saRQPndJwvJnQc7ZQ4YJcVrKh/AMaCFL/RUDZQ4i2DaauDwJHvK2RfC9CwKBgChMTBYV96uwRqvF6r4GW1qazlMOSKErLl5967qHw2loZHwwY9v6D4a1yw2dDWhRZF2vy5ONNHxBuDAiuvY6fnhyEMENDiid/ytw5X3VLx4P5CBgOnvB7XgLfaDaKzKp6pBmitTIPfsiTT+AL726L2qQPdVHh72Yu1sPmDetFdSm";

    private TestJwt() {
    }

    public static String accessToken(String subject) {
        return accessToken(subject, List.of("MEMBER"));
    }

    public static String accessToken(String subject, List<String> roles) {
        try {
            byte[] der = Base64.getDecoder().decode(PRIVATE_KEY);
            RSAPrivateKey key = (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(der));
            Instant now = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer("synapse-auth")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(900)))
                .claim("roles", roles)
                .claim("type", "ACCESS")
                .build();
            SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("synapse-key-2026-05").build(),
                claims);
            jwt.sign(new RSASSASigner(key));
            return jwt.serialize();
        } catch (Exception ex) {
            throw new IllegalStateException("테스트 JWT 생성 실패", ex);
        }
    }
}
