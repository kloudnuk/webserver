package com.kloudnuk.webserver.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.kloudnuk.webserver.daos.api.IUserRepo;
import com.kloudnuk.webserver.models.EntityUpdate;
import com.kloudnuk.webserver.models.User;
import com.kloudnuk.webserver.models.UserAuthority;
import com.kloudnuk.webserver.models.UserPasswordChange;
import com.kloudnuk.webserver.models.UserRegistration;
import com.kloudnuk.webserver.services.api.IUserManager;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "api/v1/users")
public class UserController {
    final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    IUserManager usermgr;

    private IUserRepo repo;

    public UserController(IUserRepo repo) {
        this.repo = repo;
    }

    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @GetMapping("/")
    @PreAuthorize("hasRole(#org) && hasRole('MANAGER')")
    public List<User> readAll(@RequestParam @P("org") String org) {
        return repo.readAll(org).toList();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @PostMapping("/create")
    @PreAuthorize("hasRole(#org) && hasRole('MANAGER')")
    public void createUsers(@RequestBody List<UserRegistration> userregs,
            @RequestParam @P("org") String org) {

        List<User> users = new ArrayList<>();
        List<UserAuthority> userauths = new ArrayList<>();

        users = userregs.stream()
                .map(reg -> new User(reg.name(), reg.email(), org, reg.password(), reg.enabled()))
                .collect(Collectors.toList());
        userauths = userregs.stream().map(reg -> new UserAuthority(reg.name(), reg.role()))
                .collect(Collectors.toList());

        usermgr.register(users, org);
        usermgr.authorize(userauths, org);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{username}")
    @ResponseBody
    @PreAuthorize("hasRole(#org) && hasRole('MANAGER')")
    public User readByName(@RequestParam @P("org") String org, @PathVariable String username) {
        return repo.readByName(username).orElseThrow();
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/edit")
    @PreAuthorize("hasRole(#org) && hasRole('MANAGER')")
    public void update(@RequestBody EntityUpdate update, @RequestParam @P("org") String org) {
        repo.updateOne(update.updateColumn(), update.updateValue(), update.filterColumn(),
                update.filterValue());
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/changepass")
    @PreAuthorize("hasRole(#org) && hasRole('MANAGER')")
    public void changePassword(@RequestParam @P("org") String org,
            @RequestBody UserPasswordChange body) {
        repo.updatePassword(body.username(), body.newPassword());
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/remove")
    @PreAuthorize("hasRole(#org) && hasRole('MANAGER')")
    public void removeUser(@RequestParam @P("org") String org, @RequestBody List<String> users) {
        usermgr.unauthorize(users);
        usermgr.unregister(users);
    }
}
