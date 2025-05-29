package com.allo.orderservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    @NotBlank(message = "Full name cannot be blank")
    @Size(min = 2, max = 100)
    private String fullName;

    @NotBlank(message = "Address cannot be blank")
    @Size(min = 5, max = 200)
    private String address;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;
}
