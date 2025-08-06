package com.example.all_in_one.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Message(
        String message,
        @JsonProperty("isActive") boolean isPublic
        ) {}
