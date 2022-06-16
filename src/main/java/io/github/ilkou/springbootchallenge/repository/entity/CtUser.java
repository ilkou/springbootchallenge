package io.github.ilkou.springbootchallenge.repository.entity;

import io.github.ilkou.springbootchallenge.repository.enumeration.CtRole;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Table(name = "ct_user")
@Entity
public class CtUser implements Serializable {

    @Id
    @Column(nullable = false, unique = true, updatable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;
    @Column(name = "last_name", nullable = false)
    private String lastName;
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;
    private String city;
    private String country;
    private String avatar;
    private String company;
    @Column(name = "job_position", nullable = false)
    private String jobPosition;
    private String mobile;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private CtRole role;

}
