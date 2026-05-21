package com.synapse.engagement.community.service;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class ShareTokenGenerator {

    String generate() {
        /*
         * UUID 128비트를 URL-safe Base64로 바꿉니다.
         * 일반 UUID 문자열보다 짧고, '/'나 '+'가 없어 공유 URL에 그대로 넣기 쉽습니다.
         */
        UUID uuid = UUID.randomUUID();
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());
    }
}

