package com.mothsoft.alexis.web.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

import com.mothsoft.alexis.domain.SocialConnection;
import com.mothsoft.alexis.domain.SocialNetworkType;
import com.mothsoft.alexis.domain.User;
import com.mothsoft.alexis.domain.UserAuthenticationDetails;
import com.mothsoft.alexis.service.UserService;
import com.mothsoft.alexis.web.security.GoogleOauthAuthenticationFilter.OauthAuthenticationToken;

public class GoogleOauthAuthenticationProvider implements AuthenticationProvider {

    private UserService userService;
    private UserDetailsService userDetailsService;

    public GoogleOauthAuthenticationProvider(final UserService userService, final UserDetailsService userDetailsService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public boolean supports(final Class<? extends Object> authenticationType) {
        return OauthAuthenticationToken.class.isAssignableFrom(authenticationType);
    }

    @Transactional
    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final String username = authentication.getName();

        SocialConnection socialConnection = userService.findSocialConnectionByRemoteUsername(username,
                SocialNetworkType.G);

        final OauthAuthenticationToken oauthToken = (OauthAuthenticationToken) authentication;
        final String email = oauthToken.getEmail();
        final User user;

        if (socialConnection == null) {
            // create user
            user = new User();
            user.setAdmin(false);
            user.setHashedPassword(oauthToken.getAccessToken());
            user.setPasswordSalt(null);
            user.setUsername(email);
            user.setAnalysisRole(true);

            socialConnection = new SocialConnection(user, username, oauthToken.getAccessToken(),
                    oauthToken.getRefreshToken(), SocialNetworkType.G);
            user.getSocialConnections().add(socialConnection);
            userService.addUser(user);
        } else {
            if (!socialConnection.getUser().getUsername().equals(email)) {
                throw new BadCredentialsException("Identity mismatch");
            }

            socialConnection.setOauthToken(oauthToken.getAccessToken());
            socialConnection.setOauthTokenSecret(oauthToken.getRefreshToken());
            user = socialConnection.getUser();
        }

        this.userService.createApiToken(user);
        this.userService.update(user);

        final List<GrantedAuthority> userAuthorities = new ArrayList<GrantedAuthority>();
        userAuthorities.add(new GrantedAuthorityImpl("ROLE_USER"));

        if (user.isAdmin()) {
            userAuthorities.add(new GrantedAuthorityImpl("ROLE_ADMIN"));
        }

        if (user.isAnalysisRole() || user.isAdmin()) {
            userAuthorities.add(new GrantedAuthorityImpl("ROLE_ANALYSIS"));
        }

        final UserAuthenticationDetails details = (UserAuthenticationDetails) this.userDetailsService
                .loadUserByUsername(email);
        final UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(details,
                oauthToken.getAccessToken(), userAuthorities);

        return newAuthentication;
    }

}
