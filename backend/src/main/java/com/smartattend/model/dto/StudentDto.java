package com.smartattend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDto {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String studentNumber;
    private String program;
    private String classGroup;
    private String status;
}
