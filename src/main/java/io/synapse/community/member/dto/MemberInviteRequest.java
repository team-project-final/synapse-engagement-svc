package io.synapse.community.member.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MemberInviteRequest(@NotNull UUID userId) {
}
