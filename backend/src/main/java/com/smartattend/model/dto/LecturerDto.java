package com.smartattend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LecturerDto {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String staffNumber;
    private String department;
    private String status;
}
