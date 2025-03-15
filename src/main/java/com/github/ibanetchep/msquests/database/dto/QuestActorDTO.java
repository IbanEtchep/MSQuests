package com.github.ibanetchep.msquests.database.dto;

import java.util.UUID;

public record QuestActorDTO(String actorType, UUID uniqueId, String actorReferenceId) {}