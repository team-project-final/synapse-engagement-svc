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
