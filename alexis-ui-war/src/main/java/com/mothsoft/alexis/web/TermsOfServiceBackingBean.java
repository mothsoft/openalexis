/*   Copyright 2013 Tim Garrett, Mothsoft LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.mothsoft.alexis.web;

import java.io.IOException;
import java.util.Date;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import com.mothsoft.alexis.domain.User;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.service.UserService;

public class TermsOfServiceBackingBean {

    private UserService userService;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void accept(final ActionEvent event) throws IOException {
        final User user = this.userService.getUser(CurrentUserUtil.getCurrentUserId());
        user.setTosAcceptDate(new Date());
        this.userService.update(user);

        final FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().redirect("/" + context.getExternalContext().getContextName() + "/dashboard/");
    }

}
