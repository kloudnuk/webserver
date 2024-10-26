package com.kloudnuk.webserver.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import com.kloudnuk.webserver.daos.api.IOrgRepo;
import com.kloudnuk.webserver.daos.api.IUserAuthorityRepo;
import com.kloudnuk.webserver.daos.api.IUserRepo;
import com.kloudnuk.webserver.ddos.UserAuthorityDdo;
import com.kloudnuk.webserver.ddos.UserDdo;
import com.kloudnuk.webserver.models.Org;
import com.kloudnuk.webserver.models.User;

import java.util.List;
import java.util.ArrayList;

@Controller
public class LoginController {
    final Logger log = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private IOrgRepo repo;

    @Autowired
    private IUserRepo userrepo;

    @Autowired
    private IUserAuthorityRepo userAuthRepo;

    public LoginController() {}

    @GetMapping("/login")
    String login() {
        return "login";
    }

    @PostMapping("/login/register")
    public String register(User user) {

        Org org = new Org(user.orgname());
        List<Org> orglist = new ArrayList<Org>();
        orglist.add(org);
        repo.create(orglist);
        long orgid = repo.getOrgId(user.orgname());

        List<UserDdo> userlist = new ArrayList<UserDdo>();
        UserDdo userddo =
                new UserDdo(user.name(), user.email(), orgid, user.password(), user.enabled());
        userlist.add(userddo);
        userrepo.insert(userlist, orgid);

        List<UserAuthorityDdo> userAuthList = new ArrayList<UserAuthorityDdo>();
        UserAuthorityDdo ddo = new UserAuthorityDdo(userrepo.getUserId(user.name()), 1);
        userAuthList.add(ddo);
        userAuthRepo.insert(userAuthList);

        return "redirect:/login";
    }

}
