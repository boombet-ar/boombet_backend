package com.boombet.boombet_backend.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDataRequestDTO {


    @NotBlank(message = "El género es obligatorio")
    private String genero;


    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d+", message = "El DNI solo debe contener números")
    private String dni;
}