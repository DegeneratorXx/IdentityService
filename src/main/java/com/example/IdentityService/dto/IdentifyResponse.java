package com.example.IdentityService.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IdentifyResponse {

    private ContactResponse contact;
}