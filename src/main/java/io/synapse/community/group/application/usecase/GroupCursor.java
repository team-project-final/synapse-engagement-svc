package io.synapse.community.group.application.usecase;

import io.synapse.community.group.domain.model.Group;
import io.synapse.community.group.domain.model.exception.InvalidGroupCursorException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

// 그룹 목록의 다음 페이지 위치를 숨겨서 전달하는 cursor 값입니다.
record GroupCursor(LocalDateTime createdAt, UUID id) {

    static String encode(Group group) {
        // 정렬 기준(createdAt, id)을 Base64로 감싸서 클라이언트가 내부 구조를 몰라도 되게 합니다.
        String value = group.createdAt() + "|" + group.id();
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    static GroupCursor decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        try {
            // 잘못된 cursor는 목록 조회 조건을 망가뜨리므로 도메인 예외로 바꿔 던집니다.
            String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|", 2);
            return new GroupCursor(LocalDateTime.parse(parts[0]), UUID.fromString(parts[1]));
        } catch (RuntimeException exception) {
            throw new InvalidGroupCursorException();
        }
    }
}

