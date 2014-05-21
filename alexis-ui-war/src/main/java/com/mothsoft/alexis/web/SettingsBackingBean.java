package com.mothsoft.alexis.web;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;

import com.mothsoft.alexis.domain.User;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.service.UserService;

public class SettingsBackingBean {

    private UserService userService;
    private String timeZone;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @PostConstruct
    public void initialize() {
        final User user = this.userService.getUser(CurrentUserUtil.getCurrentUserId());
        this.timeZone = user.getTimeZone().getID();
    }

    public List<String> getAvailableTimeZones() {
        return Arrays.asList(TimeZone.getAvailableIDs());
    }

    public String getTimeZone() {
        return this.timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public void save(ActionEvent event) {
        final User user = this.userService.getUser(CurrentUserUtil.getCurrentUserId());
        final TimeZone tz = TimeZone.getTimeZone(this.timeZone);
        user.setTimeZone(tz);
        this.userService.update(user);
        CurrentUserUtil.getCurrentUser().setTimeZone(tz);
    }
}
