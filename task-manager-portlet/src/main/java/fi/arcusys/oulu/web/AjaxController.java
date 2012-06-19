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

import java.util.List;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import fi.arcusys.oulu.exception.IntalioException;
import fi.arcusys.oulu.intalio.Task;
import fi.arcusys.oulu.intalio.TaskHandle;
import fi.arcusys.oulu.util.TaskUtil;

/**
 * Handles ajax request from web and returns the data with json string
 * @author Jinhua Chen
 * May 11, 2011
 */

@Controller("ajaxController")
@RequestMapping(value = "VIEW")
public class AjaxController {

	private static final Logger LOG = Logger.getLogger(AjaxController.class);

	/**
	 * Shows ajax for retrieving intalio tasks
	 * @param page page id
	 * @param numPerPage number of tasks per page to be shown
	 * @param taskTypeStr intalio task type
	 * @param keyword keyword for searching/filtering
	 * @param orderType order type of task
	 * @param modelmap
	 * @param request
	 * @param response
	 * @return ajax view with intalio tasks and related information in json format
	 */
	@RenderMapping(params = "myaction=ajax")
	public String showAjax(@RequestParam(value = "page") int page,
			@RequestParam(value = "numPerPage") int numPerPage,
			@RequestParam(value = "taskType") String taskTypeStr,
			@RequestParam(value = "keyword") String keyword,
			@RequestParam(value = "orderType") String orderType,
			ModelMap modelmap, PortletRequest request, PortletResponse response) {
		PortletSession portletSession = request.getPortletSession();
		String token = (String) portletSession.getAttribute("USER_token");
		String username = (String) portletSession.getAttribute("USER_username");
		int taskType = getTaskType(taskTypeStr);
		JSONObject jsonModel = getJsonModel(taskType, page, numPerPage, keyword, orderType, token, username);
		modelmap.addAttribute("response", jsonModel);

		return AjaxViewResolver.AJAX_PREFIX;
	}

	/**
	 * Gets the task state
	 * @param taskId intalio task id
	 * @param modelmap
	 * @param request
	 * @param response
	 * @return ajax view with task state in json format
	 */
	@RenderMapping(params = "myaction=getState")
	public String getTaskState(@RequestParam(value = "taskId") String taskId,
			ModelMap modelmap, PortletRequest request, PortletResponse response) {
		PortletSession portletSession = request.getPortletSession();
		String token = (String) portletSession.getAttribute("USER_token");
		String username = (String) portletSession.getAttribute("USER_username");
		JSONObject jsonModel = new JSONObject();

		if(token == null) {
			jsonModel.put("tokenStatus", "INVALID");
		}else {
			try {
				TaskHandle taskhandle = new TaskHandle(token, username);
				String taskState = taskhandle.getTaskStatus(taskId);
				jsonModel.put("taskState", taskState);
			} catch (IntalioException e) {
				LOG.error("Retrieving taskState failed. taskId: '"+taskId+"'", e);
				jsonModel.put("tokenStatus", "INVALID");
			}
		}

		modelmap.addAttribute("response", jsonModel);
		return AjaxViewResolver.AJAX_PREFIX;
	}

	/**
	 * Processes task query and gets task list
	 * @param taskType task type
	 * @param page page id
	 * @param numPerPage number of tasks per page to be shown
	 * @param keyword keyword for searching/filtering
	 * @param orderType order type of tasks
	 * @param token user participant token
	 * @param username user name
	 * @return task information in Json format
	 */
	public JSONObject getJsonModel(int taskType, int page, int numPerPage, String keyword, String orderType, String token, String username) {
		JSONObject jsonModel = new JSONObject();

		if(token == null) {
			jsonModel.put("tokenStatus", "INVALID");
		} else {
			try {
				TaskHandle taskhandle;
				taskhandle = new TaskHandle(token, username);
				int totalTasksNum;
				int totalPages;
				List<Task> tasks;
				String first = String.valueOf((page-1)*numPerPage);
				String max =  String.valueOf(numPerPage);
				tasks = taskhandle.getTasksByParams(taskType, keyword, orderType, first, max);
				totalTasksNum = taskhandle.getTotalTasksNumber(taskType, keyword);
				totalPages = (totalTasksNum == 0) ? 1:(int) Math.ceil((double)totalTasksNum/numPerPage);
				jsonModel.put("totalItems", totalTasksNum);
				jsonModel.put("totalPages", totalPages);
				jsonModel.put("tasks", tasks);
				jsonModel.put("tokenStatus", "VALID");
			} catch (IntalioException e) {
				LOG.error("Failed to get tasks", e);
				jsonModel.put("tokenStatus", "INVALID");
			}
		}

		return jsonModel;
	}

	/**
	 * Converts task type string to integer
	 * @param taskTypeStr task type string
	 * @return task type
	 */
	private int getTaskType(String taskTypeStr) {

		if(taskTypeStr.equals("task")) {
			return TaskUtil.TASK;
		}else if(taskTypeStr.equals("notification")) {
			return TaskUtil.NOTIFICATION;
		}else if(taskTypeStr.equals("process")) {
			return TaskUtil.PROCESS;
		}else {
			return TaskUtil.PROCESS;
		}
	}


}
