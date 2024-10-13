package com.kloudnuk.webserver.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kloudnuk.webserver.daos.api.IOrgRepo;
import com.kloudnuk.webserver.daos.api.IUserAuthorityRepo;
import com.kloudnuk.webserver.daos.api.IUserRepo;
import com.kloudnuk.webserver.ddos.UserAuthorityDdo;
import com.kloudnuk.webserver.ddos.UserDdo;
import com.kloudnuk.webserver.models.Org;
import com.kloudnuk.webserver.models.User;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping(path = "api/v1/orgs")
public class OrgController {
    final Logger log = LoggerFactory.getLogger(OrgController.class);

    @Autowired
    private IOrgRepo repo;

    @Autowired
    private IUserRepo userrepo;

    @Autowired
    private IUserAuthorityRepo userAuthRepo;

    public OrgController(IOrgRepo repo, IUserRepo userrepo, IUserAuthorityRepo userAuthRepo) {
        this.repo = repo;
        this.userrepo = userrepo;
        this.userAuthRepo = userAuthRepo;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @PostMapping("/create")
    @PreAuthorize("permitAll")
    public void createOrgs(@RequestBody User user) {
        log.debug(user.toString());

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
    }
}
