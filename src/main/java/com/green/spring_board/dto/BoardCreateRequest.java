package com.green.spring_board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class BoardCreateRequest {
    @NotBlank
    @Size(min = 10, max = 50)
    private String title;
    @Size(min = 10)
    @NotBlank
    private String content;
}