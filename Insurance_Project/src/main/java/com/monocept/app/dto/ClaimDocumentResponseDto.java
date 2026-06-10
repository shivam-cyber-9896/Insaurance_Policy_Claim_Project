package com.monocept.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDocumentResponseDto {
    private Long id;
    private String documentName;
    private String documentType;
    private String documentReference;  // this is the Cloudinary URL
}