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

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import com.liferay.portal.util.PortalUtil;

/**
 * Shows task form page and store the current query information on the jsp page
 * @author Jinhua Chen
 * May 11, 2011
 */
@Controller("formController")
@RequestMapping(value = "VIEW")
public class FormController {

	private static final Logger LOG = Logger.getLogger(FormController.class);

	@RenderMapping(params = "myaction=taskform")
	public String showForm(RenderResponse response) {

		return "taskform";
	}

	// @ModelAttribute here works as the referenceData method
	@ModelAttribute(value = "tasklink")
	public String model(@RequestParam String tasklink,
			@RequestParam String currentPage,
			@RequestParam String numPerPage,
			@RequestParam String taskType,
			@RequestParam String keyword,
			@RequestParam String orderType,
			RenderRequest request) {

		// store parameters in session for returning page from form page
		HttpServletRequest httpRequest = PortalUtil.getHttpServletRequest(request);
		HttpSession httpSession = httpRequest.getSession();
		httpSession.setAttribute("currentPage", currentPage);
		httpSession.setAttribute("numPerPage", numPerPage);
		httpSession.setAttribute("taskType", taskType);
		httpSession.setAttribute("keyword", keyword);
		httpSession.setAttribute("orderType", orderType);

		return tasklink;
	}

}
