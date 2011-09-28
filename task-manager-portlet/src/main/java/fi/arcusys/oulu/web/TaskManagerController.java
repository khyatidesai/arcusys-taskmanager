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

package fi.arcusys.oulu.web;

import java.util.Properties;

import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import com.liferay.portal.model.User;
import com.liferay.portal.service.UserServiceUtil;
import com.liferay.portal.util.PortalUtil;

import fi.arcusys.oulu.util.TaskUtil;
import fi.arcusys.oulu.util.Util;

/**
 * Handles the main task manager page
 * @author Jinhua Chen
 * May 11, 2011
 */
@Controller("taskManagerController")
@RequestMapping(value = "VIEW")
public class TaskManagerController {
	
	private Properties props;
	
	public TaskManagerController() {
		props = Util.loadProperties();
	}
	
	// --maps the incoming portlet request to this method	
	@RenderMapping
	public String home(RenderRequest request, RenderResponse response) {
		HttpServletRequest httpRequest = PortalUtil.getHttpServletRequest(request); 
		HttpSession httpSession = httpRequest.getSession();
		httpSession.removeAttribute("currentPage");
		httpSession.removeAttribute("numPerPage");
		httpSession.removeAttribute("taskType");
		httpSession.removeAttribute("keyword");
		httpSession.removeAttribute("orderType");

		return "taskmanager";
	}
	
	@RenderMapping(params = "myaction=home")
	public String showHome(RenderResponse response) {		

		return "taskmanager";
	}
	
	// -- @ModelAttribute here works as the referenceData method
	@ModelAttribute(value = "tokenStatus")
	public String model(RenderRequest request) {
				
		if(checkUserToken(request)) {
			return "VALID";
		}else {
			return "INVALID";
		}
	}

	/**
	 * Checks user logged in or not, if logged in, verify the participant token
	 * @param request
	 * @return true if token is valid, otherwise false
	 */
	public boolean checkUserToken(RenderRequest request) {	
		String userid = null;
		String token = null;
		
		try {
			userid = request.getRemoteUser();
			
			if(userid != null) { // user is logged in
				User user = UserServiceUtil.getUserById(Long.parseLong(userid));
				String loginUsername = user.getLogin();
				PortletSession portletSession = request.getPortletSession();				
				token = (String) portletSession.getAttribute("USER_token");
				
				if(token == null) {
					String username = props.getProperty(Util.INTALIO_REALM) + loginUsername;	
					token = TaskUtil.getToken(username);
					TaskUtil.removeToken(username);
					portletSession.setAttribute("USER_token", token);
					portletSession.setAttribute("USER_username", username);
				}				
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
		
		return token != null;
	}
	
}
