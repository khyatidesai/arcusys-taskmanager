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

package fi.arcusys.oulu.intalio;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import fi.arcusys.intalio.tms.TaskMetadata;
import fi.arcusys.oulu.util.TaskUtil;
import fi.arcusys.oulu.util.Util;

/**
 * Handle the intalio task processing including querying tasks, formatting task
 * to be presented to web
 * @author Jinhua Chen
 * May 9, 2011
 */

public class TaskHandle {
	private String message;
	private String participantToken;
	private String username;
	private Properties props;

	/**
	 * Constructor and initialization
	 */
	public TaskHandle() {
		this.message = "";
		this.participantToken = null;
		this.props = Util.loadProperties();
		if (props == null) {
			throw new RuntimeException("Couldn't load properties file");
		}
	}

	/**
	 * Constructor with intalio participant token and username
	 */
	public TaskHandle(String token, String username) {
		this.participantToken = token;
		this.username = username;
		this.props = Util.loadProperties();
		if (props == null) {
			throw new RuntimeException("Couldn't load properties file");
		}
	}

	/**
	 * Get available tasks by the given parameters and return task list
	 * @param taskType the intalio task type
	 * @param keyword the keyword for searching/filetering
	 * @param orderType order type of tasks
	 * @param first the beginning index of the tasks 
	 * @param max the maximum tasks to be queried
	 * @return available task list
	 */
	public List<Task> getTasksByParams(int taskType, String keyword, 
			String orderType, String first, String max) {
		List<Task> tasks = null;
		String taskTypeStr = TaskUtil.getTaskType(taskType);
		String subQuery = "";				
		subQuery = createTaskSubQuery(taskType, keyword, orderType);
		tasks = getTasksFromServ(taskTypeStr, subQuery, first, max);
		
		return tasks;
	}
	
	/**
	 * Get tasks from task management service
	 * @param taskType the intalio task type
	 * @param subQuery the sql string for intalio tasks database
	 * @param first the beginning index of the tasks 
	 * @param max the maximum tasks to be queried
	 * @return a list of tasks
	 */
	public List<Task> getTasksFromServ(String taskType, String subQuery, String first, String max) {
		List<Task> myTasklist = new ArrayList<Task>();
		TaskManagementService taskMngServ = new TaskManagementService(
				props.getProperty(Util.TMS_WSDL_KEY),
				props.getProperty(Util.TOKEN_WSDL_KEY));
		List<TaskMetadata> tasklist = taskMngServ.getAvailableTasks(participantToken, taskType, subQuery, 
        		first, max);
		myTasklist = createTask(tasklist);
		
		return myTasklist;
	}

	/**
	 * Get task status such as 'READY', 'CLAIMED', 'COMPLETED'
	 * @param taskId intalio task id
	 * @return the intalio task status
	 */
	public String getTaskStatus(String taskId) {
		String status;
		TaskManagementService taskMngServ = new TaskManagementService(
				props.getProperty(Util.TMS_WSDL_KEY),
				props.getProperty(Util.TOKEN_WSDL_KEY));
		status = taskMngServ.getTask(taskId, participantToken).getMetadata().getTaskState();
		
		return status;
	}
	
	/**
	 * Create task model to be shown in portlet from intalio task
	 * 
	 * @param tasklist a list of intalio tasks
	 * @return formatted task list to be presented on web
	 */
	public List<Task> createTask(List<TaskMetadata> tasklist) {
		List<Task> myTasklist = new ArrayList<Task>();
		Task myTask = new Task();
		Iterator<TaskMetadata> it = tasklist.iterator();
		
		while (it.hasNext()) {
			TaskMetadata task = it.next();
			myTask = new Task();
			myTask.setDescription(task.getDescription());
			
			if (task.getTaskState() != null) {
				myTask.setState(task.getTaskState().toString());
			} else {
				myTask.setState("");

			}
			myTask.setCreationDate(formatTaskDate(task.getCreationDate()));
			myTask.setLink(createTaskLink(task));
			myTasklist.add(myTask);
		}

		return myTasklist;
	}
	
	/**
	 * Format the task date with given format and Helsinki timezone
	 * @param xmlGregorianCalendar
	 * @return formatted date string
	 */
	public String formatTaskDate(XMLGregorianCalendar xmlGregorianCalendar) {
		Calendar cal = xmlGregorianCalendar.toGregorianCalendar();
		SimpleDateFormat dataformat = new SimpleDateFormat(
		"d.M.yyyy HH:mm:ss");
		dataformat.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"));
		String dateStr = dataformat.format(cal.getTime());
		
		return dateStr;		
	}
	
	/**
	 * Create form operation link of task
	 * @param task intalio task object
	 * @return intalio task form string
	 */
	public String createTaskLink(TaskMetadata task) {
		String link = "";		
		String taskType = "";
        String url = "";
        String formParam = "";
        String prefix = "http";
        Object[] params = null;
        String type = task.getTaskType().toString();
		
        url = task.getFormUrl();
        
		if (type.equals("ACTIVITY")) { // tasks	
			taskType = TaskUtil.TASK_TYPE;	
			if(!url.startsWith(prefix)) {
				formParam = "xFormsManager/act";
			}
		} else if (type.equals("NOTIFICATION")) { // notifications
			taskType = TaskUtil.NOTIFICATION_TYPE;
			if(!url.startsWith(prefix)) {
				formParam = "xFormsManager/notification";
			}
		} else if (type.equals("INIT")) { // processes
			taskType = TaskUtil.PROCESS_TYPE;
			if(!url.startsWith(prefix)) {
				formParam = "xFormsManager/init";
			}
		} else {
			taskType = TaskUtil.TASK_TYPE;			
		}
		
		if(!url.startsWith(prefix)) {
			url = props.getProperty(Util.INTALIO_URL) + formParam;
		}
		
		try {
			params = new Object[] { url, task.getTaskId(), taskType, 
					URLEncoder.encode(task.getFormUrl().toString(), "UTF-8"), participantToken,
			        URLEncoder.encode(username, "UTF-8"), false };
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		link =  MessageFormat.format("{0}?id={1}&type={2}&url={3}&token={4}&user={5}&claimTaskOnOpen={6}", params);

		return link;
	}

	/**
	 * Get total tasks number
	 * @param taskType the intalio task type
	 * @param subQuery the sql string for intalio tasks database
	 * @return total number of total tasks
	 */
	public int getTotalTasksNumber(int taskType, String keyword) {
		int totalNum = 0;
		String subQuery;
		String totalNumStr;
		String taskTypeStr;
		taskTypeStr = TaskUtil.getTaskType(taskType);
		subQuery = createTotalNumSubQuery(taskType, keyword);
		TaskManagementService taskMngServ = new TaskManagementService(
				props.getProperty(Util.TMS_WSDL_KEY),
				props.getProperty(Util.TOKEN_WSDL_KEY));
		totalNumStr = taskMngServ.getTotalTasksNumber(participantToken, taskTypeStr, subQuery);
		totalNum = Integer.parseInt(totalNumStr);
		
		return totalNum;		
		
	}
	
	/**
	 * Create subquery to get total number of tasks
	 * @param taskType the intalio task type
	 * @param keyword the keyword for searching/filetering
	 * @return subquery the sql string for intalio tasks database
	 */
	public String createTotalNumSubQuery(int taskType, String keyword) {
		String subQuery = "";

		switch (taskType) {

		case TaskUtil.TASK:
			subQuery = "(T._state = TaskState.READY OR T._state = TaskState.CLAIMED)" 
					 + " AND T._description like '%" + keyword + "%'";
			break;			
		case TaskUtil.NOTIFICATION:
			subQuery = "T._state = TaskState.READY" + " AND T._description like '%" + keyword + "%'";
			break;
		case TaskUtil.PROCESS:
			subQuery = "T._description like '%" + keyword + "%'";;
			break;
		default:
			break;
		}

		return subQuery;

	}
	
	/**
	 * Create subquery to get available tasks
	 * @param taskType the intalio task type
	 * @param keyword the keyword for searching/filetering
	 * @param orderType order type of tasks
	 * @return query string for intalio database
	 */
	public String createTaskSubQuery(int taskType, String keyword, String orderType) {
		String subQuery;
		String orderTypeStr = getOrderTypeStr(orderType);
		switch (taskType) {

		case TaskUtil.TASK:
			subQuery = "(T._state = TaskState.READY OR T._state = TaskState.CLAIMED)" 
				+ " AND T._description like '%" + keyword + "%'"
				+ " ORDER BY " + orderTypeStr;
			break;
		case TaskUtil.NOTIFICATION:
			subQuery = "T._state = TaskState.READY" 
				+ " AND T._description like '%" + keyword + "%'"
				+ " ORDER BY " + orderTypeStr;
			break;
		case TaskUtil.PROCESS:
			subQuery = "T._description like '%" + keyword + "%'" 
				+ " ORDER BY " + orderTypeStr;
			break;
		default:
			subQuery = "";
			break;
		}

		return subQuery;

	}
	
	/**
	 * Get query order type according to order string from jsp page
	 * @param orderType order type of tasks
	 * @return order type query for intalio tasks
	 */
	public String getOrderTypeStr(String orderType) {
		String orderTypeStr;
		
		if(orderType.equals("description_desc")) {
			orderTypeStr = "T._description DESC";
		}else if(orderType.equals("description_asc")) {
			orderTypeStr = "T._description ASC";
		}else if(orderType.equals("state_desc")) {
			orderTypeStr = "T._state DESC";
		}else if(orderType.equals("state_asc")) {
			orderTypeStr = "T._state ASC";
		}else if(orderType.equals("creationDate_desc")) {
			orderTypeStr = "T._creationDate DESC";
		}else if(orderType.equals("creationDate_asc")) {
			orderTypeStr = "T._creationDate ASC";
		}else {
			orderTypeStr = "T._creationDate DESC";
		}
		
		return orderTypeStr;
	} 
	
	/**
	 * Get token authenticated by username and password
	 * @param username username of intalio user
	 * @param password password of intalio user
	 * @return intalio participant token
	 */
	public String getTokenByUser(String username, String password) {
		String token = null;
		TaskManagementService taskMngServ = new TaskManagementService(
				props.getProperty(Util.TMS_WSDL_KEY),
				props.getProperty(Util.TOKEN_WSDL_KEY));
		token = taskMngServ.getParticipantToken(username, password);

		return token;
	}

	/**
	 * Get participant token
	 * @return intalio participant token
	 */
	public String getToken() {
		return participantToken;
	}

	/**
	 * Set participant token 
	 * @param token intalio participant token
	 */
	public void setToken(String token) {
		this.participantToken = token;
	}

	/**
	 * Show handling message e.g. error message
	 * @return message log
	 */
	public String getMessage() {
		return message;
	}

}
