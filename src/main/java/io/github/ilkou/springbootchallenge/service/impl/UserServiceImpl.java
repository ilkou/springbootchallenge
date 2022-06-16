package io.github.ilkou.springbootchallenge.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ilkou.springbootchallenge.exceptions.BadRequestException;
import io.github.ilkou.springbootchallenge.exceptions.UserNotFoundException;
import io.github.ilkou.springbootchallenge.repository.UserRepository;
import io.github.ilkou.springbootchallenge.repository.entity.CtUser;
import io.github.ilkou.springbootchallenge.repository.enumeration.CtRole;
import io.github.ilkou.springbootchallenge.service.UserService;
import io.github.ilkou.springbootchallenge.transverse.dto.BatchUsersSummaryDto;
import io.github.ilkou.springbootchallenge.transverse.dto.CtUserDto;
import io.github.ilkou.springbootchallenge.util.ValidatorHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import net.datafaker.Name;
import net.datafaker.fileformats.Format;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository repository;
    private final ObjectMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public String generate(Integer count) {
        log.info("Creating {} users...", count);

        if (count < 1) {
            throw new BadRequestException("count must be positive");
        }

        Faker faker = new Faker();
        String json = Format.toJson(
                        faker.<Name>collection()
                                .suppliers(faker::name)
                                .maxLen(count)
                                .minLen(count)
                                .build())
            .set("firstName", Name::firstName)
            .set("lastName", Name::lastName)
            .set("birthDate", name -> faker.date().birthday().toLocalDateTime().toLocalDate())
            .set("city", name -> faker.address().city())
            .set("country", name -> faker.address().countryCode())
            .set("avatar", name -> faker.avatar().image())
            .set("company", name -> faker.company().name())
            .set("jobPosition", name -> faker.job().position())
            .set("mobile", name -> faker.phoneNumber().phoneNumber())
            .set("username", name -> faker.name().username())
            .set("email", name -> faker.internet().emailAddress())
            .set("password", name -> faker.regexify("[a-zA-Z1-9]{6,10}"))
            .set("role", name -> faker.random().nextBoolean() ? CtRole.USER : CtRole.ADMIN)
            .build()
            .generate();

        log.info("Creating {} users... done", count);
        return json;
    }

    public BatchUsersSummaryDto batch(MultipartFile content) {
        log.info("Batching users...");

        BatchUsersSummaryDto summary = new BatchUsersSummaryDto();

        try {
            List<CtUserDto> users = mapper.readValue(content.getInputStream(), new TypeReference<List<CtUserDto>>(){});

            log.info("USERS: {}", users);

            users.forEach(user -> {
                try {
                    log.info("Saving user: {}", user.getUsername());
                    ValidatorHelper.validate(user);
                    CtUser toSave = fromDto(user);
                    toSave.setPassword(passwordEncoder.encode(user.getPassword()));
                    repository.save(toSave);
                    log.info("Saving user: {} done", user.getUsername());
                    summary.setSuccessfulImports(summary.getSuccessfulImports() + 1);
                } catch (Exception e) {
                    log.error("Error while saving user: {}", user.getUsername());
                    summary.setFailedImports(summary.getFailedImports() + 1);
                }
            });
            summary.setTotalUsers(users.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("Batching users... done");
        return summary;
    }

    @Override
    public CtUserDto getUserProfile(String username) {
        return repository.findOneByUsername(username)
                .map(this::fromEntity)
                .orElseThrow(UserNotFoundException::new);
    }

    public UserDetails loadUserByUsername(final String login) {
        log.debug("Authenticating user '{}'", login);

        if (new EmailValidator().isValid(login, null)) {
            return repository.findOneByEmail(login)
                    .map(this::createSpringSecurityUser)
                    .orElseThrow(() -> new UserNotFoundException("User with email " + login + " was not found in the database"));
        }

        return repository.findOneByUsername(login)
                .map(this::createSpringSecurityUser)
                .orElseThrow(() -> new UserNotFoundException("User " + login + " was not found in the database"));

    }

    private org.springframework.security.core.userdetails.User createSpringSecurityUser(CtUser user) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                user.getPassword(),
                grantedAuthorities);
    }

    private CtUserDto fromEntity(CtUser user) {
        return mapper.convertValue(user, CtUserDto.class);
    }
    private CtUser fromDto(CtUserDto dto) {
        return mapper.convertValue(dto, CtUser.class);
    }
}
