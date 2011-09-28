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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import fi.arcusys.intalio.tms.CountAvailableTasksRequest;
import fi.arcusys.intalio.tms.GetAvailableTasksRequest;
import fi.arcusys.intalio.tms.GetAvailableTasksResponse;
import fi.arcusys.intalio.tms.GetTaskRequest;
import fi.arcusys.intalio.tms.GetTaskResponse;
import fi.arcusys.intalio.tms.InvalidInputMessageFault_Exception;
import fi.arcusys.intalio.tms.InvalidParticipantTokenFault_Exception;
import fi.arcusys.intalio.tms.Task;
import fi.arcusys.intalio.tms.TaskManagementServices;
import fi.arcusys.intalio.tms.TaskMetadata;
import fi.arcusys.intalio.tms.UnavailableTaskFault_Exception;
import fi.arcusys.intalio.token.TokenService;

/**
 * Handle tasks processing via intalio web services
 * @author Jinhua Chen
 * May 9, 2011
 */
public class TaskManagementService {
	
	private URL tokenURL;
	private URL tmsURL;

	/**
	 * Constructor
	 */
	public TaskManagementService(String tmsURL, String tokenURL) {
		try {
			this.tmsURL = new URL(tmsURL);
			this.tokenURL = new URL(tokenURL);
		} catch (MalformedURLException e) {
			throw new RuntimeException("Failed to create WSDL URLs. " + e);
		}
	}


	/**
	 * Get participant token from intalio bpms server. The server authenticates 
	 * user by username and password, then generates token.
	 * @param username username of intalio user
	 * @param password password of intalio user
	 * @return Intalio participant token
	 */
	public String getParticipantToken(String username, String password) {
		String participantToken = null;
		
		try {     
			TokenService ts = new TokenService(tokenURL);
			participantToken = ts.getService().authenticateUser(username, password);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
		
		return participantToken;
	}

	/**
	 * Get Tasks from WS /axis2/services/TaskManagementServices using
	 * getAvailableTasks operation.
	 * @param participantToken intalio participant token
	 * @param taskType the intalio task type: "PATask", "PIPATask", "Notification"
	 * @param subQuery the sql string for intalio tasks database
	 * @param first the beginning index of the tasks 
	 * @param max the maximum tasks to be queried
	 * @return a list of intalio tasks
	 */

	public List<TaskMetadata> getAvailableTasks(String participantToken,
			String taskType, String subQuery, String first, String max) {
		TaskManagementServices tms;
		List<TaskMetadata> taskList = new ArrayList<TaskMetadata>();
				
		try {
			tms = new TaskManagementServices(tmsURL);
			GetAvailableTasksRequest getAvailTasksReq = new GetAvailableTasksRequest();
			getAvailTasksReq.setParticipantToken(participantToken);
			getAvailTasksReq.setTaskType(taskType);
			getAvailTasksReq.setSubQuery(subQuery);
			getAvailTasksReq.setFirst(first);
			getAvailTasksReq.setMax(max);			
			GetAvailableTasksResponse availTasksRes;
			availTasksRes = tms.getTaskManagementServicesSOAP().getAvailableTasks(getAvailTasksReq);
			taskList = availTasksRes.getTask();
		} catch (InvalidParticipantTokenFault_Exception e1) {
			throw new RuntimeException(e1);
		} catch (InvalidInputMessageFault_Exception e1) {
			throw new RuntimeException(e1);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return taskList;
	}

	/**
	 * Get the total number of tasks
	 * @param participantToken intalio participant token
	 * @param taskType the intalio task type: "PATask", "PIPATask", "Notification"
	 * @param subQuery the sql string for intalio tasks database
	 * @return total number, returns '0' if no results found
	 */
	public String getTotalTasksNumber(String participantToken, String taskType, String subQuery) {
		String totalNum = "0";
		TaskManagementServices tms;
		
		try {
			tms = new TaskManagementServices(tmsURL);
			CountAvailableTasksRequest countAvailTasksReq = new CountAvailableTasksRequest();
			countAvailTasksReq.setParticipantToken(participantToken);
			countAvailTasksReq.setTaskType(taskType);
			countAvailTasksReq.setSubQuery(subQuery);
			totalNum = tms.getTaskManagementServicesSOAP().countAvailableTasks(countAvailTasksReq);
		} catch (InvalidParticipantTokenFault_Exception e) {
			throw new RuntimeException(e);
		} catch (InvalidInputMessageFault_Exception e) {
			throw new RuntimeException(e);
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
			
		
		return totalNum;
	}

	/**
	 * Get a Intalio task with task id and participant token
	 * @param taskId intalio task id
	 * @param participantToken intalio participant token
	 * @return Intalio task
	 */
	public Task getTask(String taskId, String participantToken) {
		Task task = null;
		TaskManagementServices tms;
		
		try {
			tms = new TaskManagementServices(tmsURL);		
			GetTaskRequest req = new GetTaskRequest();
			req.setTaskId(taskId);
			req.setParticipantToken(participantToken);
			GetTaskResponse res = tms.getTaskManagementServicesSOAP().getTask(req);
			task = res.getTask();
		} catch (InvalidParticipantTokenFault_Exception e) {
			throw new RuntimeException(e);
		} catch (InvalidInputMessageFault_Exception e) {
			throw new RuntimeException(e);
		} catch (UnavailableTaskFault_Exception e) {
			throw new RuntimeException(e);
		}
		
		return task;		
	}
	

}
