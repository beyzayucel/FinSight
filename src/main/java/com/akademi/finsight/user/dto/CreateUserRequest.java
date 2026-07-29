package com.akademi.finsight.user.dto;


import com.akademi.finsight.user.validation.InternationalPhone;
import com.akademi.finsight.user.validation.PersonName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

        @Schema(description = "User email", example = "john.doe@infina.com")
        @NotBlank(message = "{validation.email.required}")
        @Email(message = "{validation.email.format}")
        @Size(max = 320, message = "{validation.email.size}")
        String email,

        @Schema(description = "First name", example = "John")
        @NotBlank(message = "{validation.firstname.required}")
        @PersonName
        String firstName,

        @Schema(description = "Last name", example = "Doe")
        @NotBlank(message = "{validation.lastname.required}")
        @PersonName
        String lastName,

        @Schema(description = "Phone in E.164 format", example = "+905551234567")
        @NotBlank(message = "{validation.phone.required}")
        @InternationalPhone
        String phoneNumber
) {}
