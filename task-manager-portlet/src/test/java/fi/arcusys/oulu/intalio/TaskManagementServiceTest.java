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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import fi.arcusys.intalio.tms.TaskMetadata;
import fi.arcusys.oulu.util.Util;

public class TaskManagementServiceTest {
	TaskManagementService tester;
	Properties props;
	
	@BeforeClass  
	public static void runBeforeClass() {  
		System.out.println("*** Test TaskManagementService class starts ***");		
	} 
	
	@AfterClass  
	public static void runAfterClass() {  
		System.out.println("*** Test TaskManagementService class ends ***");
	} 
	
	@Before
    public void setUp() throws Exception {
		props = Util.loadProperties();
		if (props == null) {
			throw new RuntimeException("Couldn't load properties file.");
		}
		tester = new TaskManagementService(
				props.getProperty(Util.TMS_WSDL_KEY), 
				props.getProperty(Util.TOKEN_WSDL_KEY));		
    }
	
	@After
    public void tearDown() throws Exception {
		
    }
	
	@Test
	public void getParticipantToken() {
		String username = "example\\test";
		String password = "test";
		String participantToken = tester.getParticipantToken(username, password);
		assertNotNull("Correct account, authentication failed", participantToken);
		
		String wrongUsername = "wrongusername";
		String wrongPassword = "wrongpassword";	
		participantToken = tester.getParticipantToken(wrongUsername, wrongPassword);
		assertNull("Incorrect account, authentication failed", participantToken);
	}

	@Test
	public void getAvailableTasks() {
		String participantToken = getToken();
		String taskType = "PATask";
		String subQuery = "T._state = TaskState.READY";
		String first = "0";
		String max = "5";
		List<TaskMetadata> tasklist = tester.getAvailableTasks(participantToken, taskType, subQuery, first, max);
		assertTrue("Corrent params,get available tasks failed", tasklist.size() > 0);
		
		String wrongTaskType = "wrongType";
		tasklist = tester.getAvailableTasks(participantToken, wrongTaskType, subQuery, first, max);
		assertTrue("Incorrect params, get available tasks failed", tasklist.size() == 0);
		
		String wrongToken = "wrongToken";
		tasklist = tester.getAvailableTasks(wrongToken, taskType, subQuery, first, max);
		assertTrue("Incorrect participant token, get available tasks failed", tasklist.size() == 0);
		
	}
	
	@Test
	public void getTotalTasksNumber() {
		String participantToken = getToken();
		String taskTypeStr = "PATask";
		String subQuery = "T._state = TaskState.READY";
		String totalNumStr;
		totalNumStr = tester.getTotalTasksNumber(participantToken, taskTypeStr, subQuery);
		int totalNum = Integer.parseInt(totalNumStr);
		assertTrue("Correct params, get total tasks failed", totalNum > 0);
		
		String wrongToken = "wrongToken";
		totalNumStr = tester.getTotalTasksNumber(wrongToken, taskTypeStr, subQuery);
		totalNum = Integer.parseInt(totalNumStr);
		assertTrue("Incorrect token, get available tasks failed", totalNum == 0);
		
	}
	
	@Ignore
	@Test
	public void getTask() {
		String taskId = "77630ab9-ad62-41c2-8fdf-dc875a0795e3";
		//taskId = "e795b3bfa0de38ff:-23df788e:13064213459:-75a210.5.12.2331554"; // incorrect form url
		// taskId = "ebc49ec8-ff7c-4455-b5bf-f90e9596e5f0";
		String participantToken = getToken();
		fi.arcusys.intalio.tms.Task task = tester.getTask(taskId, participantToken);
		String newTaskId = task.getMetadata().getTaskId();
		assertEquals("getTask failed", taskId, newTaskId);
		
		taskId = "wrong task id";
		task = tester.getTask(taskId, participantToken);
		assertNull("getTask with wrong task id failed", task);
	}
	
	private String getToken() {
		String username = "example\\test";
		String password = "test";
		String participantToken = tester.getParticipantToken(username, password);
		assertNotNull("Get authentication failed", participantToken);
		return participantToken;
	}

}
