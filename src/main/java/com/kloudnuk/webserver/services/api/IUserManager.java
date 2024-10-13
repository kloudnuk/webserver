package com.kloudnuk.webserver.services.api;

import java.util.List;
import com.kloudnuk.webserver.models.User;
import com.kloudnuk.webserver.models.UserAuthority;

public interface IUserManager {

    public void register(List<User> users, String org);

    public void authorize(List<UserAuthority> userAuthList, String org);

    public void unregister(List<String> users);

    public void unauthorize(List<String> userids);
}
