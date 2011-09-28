/**
 * $Id$
 * 
 * Copyright (C) 2011 Arcusys Oy - http://www.arcusys.fi/
 * 
 * This file is part of Arcusys Taskmanager.
 * 
 * Arcusys Taskmanager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Arcusys Taskmanager is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package fi.arcusys.oulu.login;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.liferay.portal.security.auth.AutoLogin;
import fi.arcusys.oulu.intalio.TaskHandle;
import fi.arcusys.oulu.util.TaskUtil;
import fi.arcusys.oulu.util.Util;

/**
 * A hook to handle login event in liferay to get username and password, which 
 * is used to intalio authentication. Retrieve the intalio participant token 
 * with username and password, and save the token to TaskUtil as a global
 * variable temporarily
 * @author Jinhua Chen
 * May 10, 2011
 */
public class CustomAutoLogin implements AutoLogin {
	
	private Properties props;
	
	public CustomAutoLogin() {
		props = Util.loadProperties();
	}

	public String[] login(HttpServletRequest request,
			HttpServletResponse response) {

		String[] credentials = null;
		String username = null;
		String password = null;

		try {		
			username = request.getParameter(TaskUtil.LOGIN_USERNAME_VAR);
			password = request.getParameter(TaskUtil.LOGIN_PASSWORD_VAR);
			
			if(username != null && password != null) {
				username = props.getProperty(Util.INTALIO_REALM) + username;						
				if(TaskUtil.getToken(username) == null ) { // login user changes
					TaskHandle taskhandle = new TaskHandle();
					String token = taskhandle.getTokenByUser(username, password);
					TaskUtil.addToken(username, token);
				}			
			}
			
			credentials = new String[3];

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return credentials;
	}

}
