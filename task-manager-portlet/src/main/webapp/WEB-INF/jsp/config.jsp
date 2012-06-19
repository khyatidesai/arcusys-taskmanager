<%--

  $Id$
  
  Copyright (C) 2011 Arcusys Oy - http://www.arcusys.fi/
  
  This file is part of Arcusys Taskmanager.
  
  Arcusys Taskmanager is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  Arcusys Taskmanager is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
  
  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.

 --%>

<%@ include file="init.jsp"%>

<liferay-portlet:actionURL portletConfiguration="true"
	var="configActionURL" />
<div>
	<form name="configForm"
		action="<liferay-portlet:actionURL portletConfiguration="true" />"
		method="post">
		<table class="task-config-table">
			<tr>
				<td>
					<liferay-ui:message key="config.taskFilter" />
				</td>
				<td>
					<input type="text" name="taskFilter" value="<%=taskFilter%>" style="width: 100px;" />
				</td>
			</tr>
			<tr class="evenRow">
				<td>
					<liferay-ui:message key="config.notifFilter" />
				</td>
				<td>
					<input type="text" name="notifFilter" value="<%=notifFilter%>" style="width: 100px;" />
				</td>
			</tr>
			<tr>
				<td>
					<liferay-ui:message key="config.refreshDuration" />
				</td>
				<td><select name="refreshDuration" id="refreshDuration">
						<option value="10">10s</option>
						<option value="30">30s</option>
						<option value="60">1min</option>
						<option value="900">15min</option>
						<option value="1800">30min</option>
				</select>
				</td>
			</tr>
			<tr class="evenRow">
				<td>
					<liferay-ui:message key="config.openForm" />
				</td>
				<td><select name="openForm" id="openForm">
						<option value="1">In portlet</option>
						<option value="2">New window</option>
						<option value="3">Pop-up</option>
				</select>
				</td>
			</tr>
			<tr class="evenRow">
				<td>
					<liferay-ui:message key="config.user.username" />
				</td>
				<td>
					<input type="text" name="username" value="<%=username%>" style="width: 100px;" />
				</td>
			</tr>
			
			<tr class="evenRow">
				<td>
					<liferay-ui:message key="config.user.password" />
				</td>			
				<td>
					<input type="text" name="password" value="<%=password%>" style="width: 100px;" />
				</td>
			</tr>

			<tr>
				<td>
					<input type="submit" value="<liferay-ui:message key="config.save" />" />
				</td>
				<td></td>
			</tr>
		</table>
	</form>
</div>
<script type="text/javascript">
	// set the default selected option for selectors refreshDuration and openForm
	$('#refreshDuration').val("<%=refreshDuration%>");
	$('#openForm').val("<%=openForm%>");
</script>