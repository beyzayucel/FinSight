package com.akademi.finsight.bootstrap;

import com.akademi.finsight.user.validation.InternationalPhone;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "admin")
public class AdminProperties {

    private String email;
    private String username;
    private String firstName;
    private String lastName;

    @NotBlank
    @InternationalPhone
    private String phoneNumber;

    private String password;
}
