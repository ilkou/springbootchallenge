package io.github.ilkou.springbootchallenge.transverse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.ilkou.springbootchallenge.repository.enumeration.CtRole;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CtUserDto implements Serializable {

    private UUID id;

    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String city;
    private String country;
    private String avatar;
    private String company;
    private String jobPosition;
    private String mobile;
    @NotNull
    private String username;
    @NotNull
    private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(min = 6, max = 10)
    private String password;
    @NotNull
    private CtRole role;

}
