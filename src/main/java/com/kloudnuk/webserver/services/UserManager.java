package com.kloudnuk.webserver.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.kloudnuk.webserver.daos.api.IAuthorityRepo;
import com.kloudnuk.webserver.daos.api.IOrgRepo;
import com.kloudnuk.webserver.daos.api.IUserAuthorityRepo;
import com.kloudnuk.webserver.daos.api.IUserRepo;
import com.kloudnuk.webserver.ddos.UserAuthorityDdo;
import com.kloudnuk.webserver.ddos.UserDdo;
import com.kloudnuk.webserver.models.Authority;
import com.kloudnuk.webserver.models.User;
import com.kloudnuk.webserver.models.UserAuthority;
import com.kloudnuk.webserver.services.api.IUserManager;

@Service
@Transactional
public class UserManager implements IUserManager {

    @Autowired
    IOrgRepo orgRepo;

    @Autowired
    IUserRepo userRepo;

    @Autowired
    IAuthorityRepo authRepo;

    @Autowired
    IUserAuthorityRepo userAuthRepo;

    @Transactional(propagation = Propagation.REQUIRED)
    public void register(List<User> users, String org) {
        List<UserDdo> insertList = new ArrayList<UserDdo>();
        Long organizationid = orgRepo.getOrgId(org);

        insertList = users.stream()
                .map(user -> new UserDdo(user.name(), user.email(),
                        orgRepo.getOrgId(user.orgname()), user.password(), user.enabled()))
                .collect(Collectors.toList());
        userRepo.insert(insertList, organizationid);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void authorize(List<UserAuthority> userAuthList, String org) {
        List<UserAuthorityDdo> authList = new ArrayList<UserAuthorityDdo>();

        userAuthList.stream().forEach(userauth -> {
            Long userid = userRepo.getUserId(userauth.username());
            Authority authority = authRepo.readByName(userauth.role()).orElseThrow();
            UserAuthorityDdo userAuth = new UserAuthorityDdo(userid, authority.id());
            authList.add(userAuth);
        });
        userAuthRepo.insert(authList);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void unregister(List<String> users) {
        for (String username : users) {
            userRepo.delete("name", username);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void unauthorize(List<String> users) {
        List<Long> useridList = new ArrayList<>();
        for (String username : users) {
            long id = userRepo.getUserId(username);
            useridList.add(id);
            userAuthRepo.delete("userid", String.valueOf(id));
        }
    }
}
