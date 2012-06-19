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

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.liferay.portal.kernel.portlet.BaseConfigurationAction;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portlet.PortletPreferencesFactoryUtil;

/**
 * Configuration/edit mode of portlet in Liferay
 * @author Jinhua Chen
 * May 11, 2011
 */
public class ConfigurationActionImpl extends BaseConfigurationAction {

	@Override
	public void processAction(PortletConfig portletConfig,
			ActionRequest actionRequest, ActionResponse actionResponse)
			throws Exception {

		final String portletResource = ParamUtil.getString(actionRequest, "portletResource");
		final PortletPreferences preferences = PortletPreferencesFactoryUtil.getPortletSetup(actionRequest, portletResource);
		final String taskFilter = actionRequest.getParameter("taskFilter");
		final String notifFilter = actionRequest.getParameter("notifFilter");
		final String refreshDuration = actionRequest.getParameter("refreshDuration");
		final String openForm = actionRequest.getParameter("openForm");
		final String username = actionRequest.getParameter("username");
		final String password = actionRequest.getParameter("password");

		preferences.setValue("taskFilter", taskFilter);
		preferences.setValue("notifFilter", notifFilter);
		preferences.setValue("refreshDuration", refreshDuration);
		preferences.setValue("openForm", openForm);
		preferences.setValue("username", username);
		preferences.setValue("password", password);

		preferences.store();
		SessionMessages.add(
				actionRequest, portletConfig.getPortletName() + ".doConfigure");
	}

	@Override
	public String render(PortletConfig portletConfig,
			RenderRequest renderRequest, RenderResponse renderResponse)
			throws Exception {
		return "/WEB-INF/jsp/config.jsp";
	}
}
