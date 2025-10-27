
package com.github.ibanetchep.msquests.core.dto;

import java.util.UUID;

public record PlayerProfileDTO(
    UUID id,
    String name,
    UUID trackedQuestId
) {}