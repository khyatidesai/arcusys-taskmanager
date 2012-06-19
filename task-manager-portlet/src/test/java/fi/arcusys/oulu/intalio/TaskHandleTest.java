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
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import fi.arcusys.intalio.tms.TaskMetadata;
import fi.arcusys.oulu.exception.IntalioException;
import fi.arcusys.oulu.util.Util;


public class TaskHandleTest{
	TaskHandle tester;
	Properties props;

	@BeforeClass
	public static void runBeforeClass() {
		System.out.println("*** Test TaskHandle class starts ***");
	}

	@AfterClass
	public static void runAfterClass() {
		System.out.println("*** Test TaskHandle class ends ***");
	}

	@Before
    public void setUp() throws Exception {
		String username = "oulu_virkailija\\stkaistoa";
		String token = getTestToken();
		tester = new TaskHandle(token, username);
		props = Util.loadProperties();
		if (props == null) {
			throw new RuntimeException("Couldn't load properties file.");
		}
    }

	@After
    public void tearDown() throws Exception {
    }

	@Test
	public void getTasksByParams() throws IntalioException {
		int taskType = 1;
		String keyword = "";
		String orderType = "creationDate_desc";
		String first = "0";
		String max = "5";
		List<Task> tasklist = tester.getTasksByParams(taskType, keyword, orderType, first, max);
		assertTrue("GetTasksByParams failed", tasklist.size() > 0);
	}

	@Test
	public void getTasksFromServ() throws IntalioException {
		String taskType = "PATask";
		String subQuery = "T._state = TaskState.READY AND T._description like '%%' ORDER BY T._creationDate DESC";
		String first = "0";
		String max = "5";
		List<Task> tasklist = tester.getTasksFromServ(taskType, subQuery, first, max);
		assertTrue("GetTasksFromServ failed", tasklist.size() > 0);
	}

	@Test
	public void createTask() {
		List<TaskMetadata> tasklist = new ArrayList<TaskMetadata>();
		TaskMetadata task = new TaskMetadata();
		List<Task> myTasklist = new ArrayList<Task>();
		task.setTaskType("ACTIVITY");
		task.setDescription("task test 1");
		task.setTaskState(null);
		GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
		cal.set(2011, 4, 20, 10, 30, 20);
		XMLGregorianCalendar xmlGregorianCalendar;
		try {
			xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
			task.setCreationDate(xmlGregorianCalendar);
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}

		task.setTaskId("task-id-1");
		String url = "http://localhost:8080/form.htm";
		task.setFormUrl(url);
		tasklist.add(task);
		task.setDescription("task test 1");
		tasklist.add(task);
		myTasklist = tester.createTask(tasklist);
		Task myTask = myTasklist.get(0);
		String expected = "task test 1";
		String actual = myTask.getDescription();
		assertEquals("createTask first description failed", expected, actual);
		expected = "20.5.2011 10:30:20";
		actual = myTask.getCreationDate();
		assertEquals("createTask first creation date failed", expected, actual);
	}

	@Test
	public void formatTaskDate() {
		GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
		cal.set(2011, 4, 20, 10, 30, 20);
		XMLGregorianCalendar xmlGregorianCalendar = null;
		try {
			xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}
		String expected = "20.5.2011 10:30:20";
		String actual = tester.formatTaskDate(xmlGregorianCalendar);
		assertEquals("formatTaskDate failed", expected, actual);
	}

	@Test
	public void createTaskLink() {
		TaskMetadata task = new TaskMetadata();
		task.setTaskType("ACTIVITY");
		task.setDescription("task test 1");
		task.setTaskState(null);
		GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
		cal.set(2011, 4, 20, 10, 30, 20);
		XMLGregorianCalendar xmlGregorianCalendar = null;
		try {
			xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}
		task.setCreationDate(xmlGregorianCalendar);
		task.setTaskId("task-id-1");
		String url = "http://localhost:8080/form.htm";
		task.setFormUrl(url);
		tester.setToken("testtoken");
		String expected = "http://localhost:8080/form.htm?id=task-id-1&type=PATask&url=http%3A%2F%2Flocalhost%3A8080%2Fform.htm&token=testtoken&user=example%5Ctesta&claimTaskOnOpen=false";
		String actual = tester.createTaskLink(task);
		assertEquals("createTask task link failed", expected, actual);
	}

	@Ignore("Not Ready to Run")
	@Test
	public void getTotalTasksNumber() throws IntalioException {
		int taskType = 1;
		String keyword = "Marko";
		int expected = 3;
		int actual = tester.getTotalTasksNumber(taskType, keyword);
		assertEquals("getTotalTasksNumber failed", expected, actual);
	}

	@Test
	public void createTotalNumSubQuery() {
		int taskType = 1;
		String keyword = "";
		String expected = "(T._state = TaskState.READY OR T._state = TaskState.CLAIMED) AND T._description like '%%'";
		String actual = tester.createTotalNumSubQuery(taskType, keyword);
		assertEquals("createTotalNumSubQuery for task failed", expected, actual);

		taskType = 2;
		keyword = "key";
		expected = "T._state = TaskState.READY AND T._description like '%key%'";
		actual = tester.createTotalNumSubQuery(taskType, keyword);
		assertEquals("createTotalNumSubQuery for notification failed", expected, actual);

		taskType = 3;
		keyword = "key";
		expected = "T._description like '%key%'";
		actual = tester.createTotalNumSubQuery(taskType, keyword);
		assertEquals("createTotalNumSubQuery for process failed", expected, actual);

		taskType = 5;
		keyword = "key";
		expected = "";
		actual = tester.createTotalNumSubQuery(taskType, keyword);
		assertEquals("createTotalNumSubQuery for other invalid processes failed", expected, actual);
	}

	@Test
	public void createTaskSubQuery() {
		int taskType = 1;
		String keyword = "";
		String orderType = "description_desc";
		String expected = "(T._state = TaskState.READY OR T._state = TaskState.CLAIMED) AND T._description like '%%' ORDER BY T._description DESC";
		String actual = tester.createTaskSubQuery(taskType, keyword, orderType);
		assertEquals("createTaskSubQuery for task failed", expected, actual);

		taskType = 2;
		keyword = "key";
		expected = "T._state = TaskState.READY AND T._description like '%key%' ORDER BY T._description DESC";
		actual = tester.createTaskSubQuery(taskType, keyword, orderType);
		assertEquals("createTaskSubQuery for notification failed", expected, actual);

		taskType = 3;
		keyword = "key";
		expected = "T._description like '%key%' ORDER BY T._description DESC";
		actual = tester.createTaskSubQuery(taskType, keyword, orderType);
		assertEquals("createTaskSubQuery for process failed", expected, actual);

		taskType = 5;
		keyword = "key";
		expected = "";
		actual = tester.createTaskSubQuery(taskType, keyword, orderType);
		assertEquals("createTaskSubQuery for other invalid processes failed", expected, actual);
	}

	@Test
	public void getOrderTypeStr() {
		String orderType = "description_desc";
		String expected = "T._description DESC";
		String actual = tester.getOrderTypeStr(orderType);
		assertEquals("getOrderTypeStr " + orderType + " failed", expected, actual);

		orderType = "description_asc";
		expected = "T._description ASC";
		actual = tester.getOrderTypeStr(orderType);
		assertEquals("getOrderTypeStr " + orderType + " failed", expected, actual);

		orderType = "state_desc";
		expected = "T._state DESC";
		actual = tester.getOrderTypeStr(orderType);
		assertEquals("getOrderTypeStr " + orderType + " failed", expected, actual);

		orderType = "state_asc";
		expected = "T._state ASC";
		actual = tester.getOrderTypeStr(orderType);
		assertEquals("getOrderTypeStr " + orderType + " failed", expected, actual);

		orderType = "creationDate_desc";
		expected = "T._creationDate DESC";
		actual = tester.getOrderTypeStr(orderType);
		assertEquals("getOrderTypeStr " + orderType + " failed", expected, actual);

		orderType = "creationDate_asc";
		expected = "T._creationDate ASC";
		actual = tester.getOrderTypeStr(orderType);
		assertEquals("getOrderTypeStr " + orderType + " failed", expected, actual);

		orderType = "other";
		expected = "T._creationDate DESC";
		actual = tester.getOrderTypeStr(orderType);
		assertEquals("getOrderTypeStr " + orderType + " failed", expected, actual);

	}

	@Test
	public void getTokenByUser() throws IntalioException {
		String username = "example/test";
		String password = "test";
		String participantToken = tester.getTokenByUser(username, password);
		System.out.println(participantToken);
		assertNotNull("getTokenByUser failed", participantToken);

		username = "wrong";
		password = "wrong";
		participantToken = tester.getTokenByUser(username, password);
		assertNull("getTokenByUser failed", participantToken);
	}

	@Ignore("Not Ready to Run")
	@Test
	public void getToken() {
		fail("Not yet implemented");
	}

	@Ignore("Not Ready to Run")
	@Test
	public void setToken() {
		fail("Not yet implemented");
	}

	@Ignore("Not Ready to Run")
	@Test
	public void getMessage() {
		fail("Not yet implemented");
	}

	private String getTestToken() throws IntalioException {
		TaskManagementService tms = new TaskManagementService(
				props.getProperty(Util.TMS_WSDL_KEY),
				props.getProperty(Util.TOKEN_WSDL_KEY));
		String username = "example\\test";
		String password = "test";
		String participantToken = tms.getParticipantToken(username, password);

		return participantToken;
	}

}
