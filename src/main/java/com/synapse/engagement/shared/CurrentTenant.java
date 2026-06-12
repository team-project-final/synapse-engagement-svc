package com.synapse.engagement.shared;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;

public final class CurrentTenant {
    public static final String DEFAULT_TENANT = "default";

    private CurrentTenant() {
    }

    public static String resolve(Jwt jwt) {
        if (jwt == null) {
            return DEFAULT_TENANT;
        }
        // 발급자별 claim 이름 차이를 흡수해 Service/Kafka 계층은 tenantId 하나만 보게 한다.
        String tenantId = jwt.getClaimAsString("tenantId");
        if (!StringUtils.hasText(tenantId)) {
            tenantId = jwt.getClaimAsString("tenant_id");
        }
        if (!StringUtils.hasText(tenantId)) {
            tenantId = jwt.getClaimAsString("tid");
        }
        return StringUtils.hasText(tenantId) ? tenantId : DEFAULT_TENANT;
    }
}
