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

<%@ include file="init.jsp" %>

<%
/* Get page parameters from session if exists */
String currentPage = (String)request.getSession().getAttribute("currentPage");
String numPerPage = (String)request.getSession().getAttribute("numPerPage");
String taskType = (String)request.getSession().getAttribute("taskType");
String keyword = (String)request.getSession().getAttribute("keyword");
String orderType = (String)request.getSession().getAttribute("orderType");
%>

<portlet:renderURL var="ajaxURL"
	windowState="<%= LiferayWindowState.EXCLUSIVE.toString() %>">
	<portlet:param name="myaction" value="ajax" />
</portlet:renderURL>

<portlet:renderURL var="formURL" windowState="<%= WindowState.MAXIMIZED.toString() %>" >
	<portlet:param name="myaction" value="taskform" />
	<portlet:param name="tasklink" value= "CONSTANT_TASK_FORM_LINK" />
	<portlet:param name="currentPage" value= "CONSTANT_TASK_CURRENT_PAGE" />
	<portlet:param name="numPerPage" value= "CONSTANT_TASK_NUMBER_PER_PAGE" />	
	<portlet:param name="taskType" value= "CONSTANT_TASK_TASK_TYPE" />
	<portlet:param name="keyword" value= "CONSTANT_TASK_KEYWORD" />
	<portlet:param name="orderType" value= "CONSTANT_TASK_ORDER_TYPE" />
</portlet:renderURL>

<portlet:renderURL var="popupURL"
	windowState="<%= LiferayWindowState.POP_UP.toString() %>">
	<portlet:param name="myaction" value="popup" />
	<portlet:param name="tasklink" value= "CONSTANT_TASK_FORM_LINK" />
</portlet:renderURL>


<script type="text/javascript"> 
/*
 * Handle action for task manager
 * @Author: Jinhua Chen
 */
	var tokenStatus = "${tokenStatus}";
	var refreshTimer; // global refresh timer
	var configObj = new config();
	var pageObj = new paging();	
	checkPageSession();
	
	$(document).ready(function(){
		/* Ajax activity support call. Show the ajax loading icon */
	    $('#task-manager-operation-loading')
	    .hide()  // hide it initially
	    .ajaxStart(function() {
			$(this).show();
	    })
	    .ajaxStop(function() {
	    	$(this).hide();
	    });
		    
		changeTasks();
		/* User is logged in and participant token for intalio is valid */
		if(tokenStatus == 'VALID') {
			ajaxGetTasks();		
			resetRefreshTimer();
		}else {
			var message = "<spring:message code="error.invalidToken" />";
			showErrorMessage(message);
		}
		
		/* remove the timer when user is operating on the page */
		$('#task-manager-wrap').click(function(){
			resetRefreshTimer();
	    });
								
	});
	
	/**
	 * Handle the event when user clicks the field to change task type. Add the
	 * onmousemove and onmouse out events for changing background, and click 
	 * event to start a new task query
	 */
	function changeTasks() {
		var taskLi = $('#task-manager-navi ul li');

		for ( var i = 0; i < taskLi.length; i++) {
			$(taskLi[i]).addClass('task-bg-normal');
			
			if(i == pageObj.taskTypeNum)
				$(taskLi[i]).addClass('task-bg-focus');
			taskLi[i].i = i;
			
			taskLi[i].onmousemove = function() {
				if(this.i != pageObj.taskTypeNum)
					$(this).addClass('task-bg-focus');
			}

			taskLi[i].onmouseout = function() {
				if(this.i != pageObj.taskTypeNum)				
					$(this).removeClass('task-bg-focus');
			}
			
			taskLi[i].onclick = function() {
				$(taskLi).removeClass('task-bg-focus');
				$(this).addClass('task-bg-focus');
				getTasks(this.i);
			}
		}
	}

	/**
	 * Set task query parameters and execute task query in Ajax way
	 */
	function getTasks(type) {
		pageObj.setTaskParams(type); // set taskType and related initialization		
		ajaxGetTasks();
	}

	/**
	 * Execute ajax query in Post way, and parse the Json format response, and
	 * then create tasks in table and task page filed.
	 */
	function ajaxGetTasks() {
		
		if(tokenStatus != 'VALID') {
			return;
		}
		/* For the Liferay portlet that the session is expired */
		if(typeof Liferay == "object"  && typeof Liferay.Session == "object" &&  Liferay.Session._currentTime == 0) {
			return;
		}
		
		var url="<%= ajaxURL %>";

		$.post(url, {page:pageObj.currentPage, numPerPage:pageObj.numPerPage, taskType:pageObj.taskType, 
			keyword:pageObj.keyword, orderType:pageObj.orderType}, function(data) {
			var obj = eval('(' + data + ')');
			var json = obj.response;
			tokenStatus = json["tokenStatus"];
			
			if(tokenStatus == 'VALID') {
				pageObj.totalPages = json["totalPages"];
				pageObj.totalItems = json["totalItems"];
				var tasks = json["tasks"];
				var taskHtml = createTasksTable(tasks);
				$('#task-manager-tasklist').html(taskHtml);	
				var pageHtml = createTasksPage();
				$('#task-manager-operation-page').html(pageHtml);
				decorateTaskManager();
			}else {
				var message = "<spring:message code="error.invalidToken" />";
				showErrorMessage(message);
			}
		});
		
	}

	/**
	 * Create tasks table in Html
	 */
	function createTasksTable(tasks) {
		var taskHtml = "";
		var formLink = "";
		
		taskHtml = '<table class="task-manager-table">'
				+ '<tr class="task-manager-table trheader">'
				+ '<td><a href="javascript:void(0)" onclick="orderTask(\'description\')"><spring:message code="task.description" /></a></td>';
		/* process does not have state field */
		if(pageObj.taskType != 'process') {
			taskHtml += '<td><a href="javascript:void(0)" onclick="orderTask(\'state\')"><spring:message code="task.state" /></a></td>';
		}
		
		taskHtml += '<td><a href="javascript:void(0)" onclick="orderTask(\'creationDate\')"><spring:message code="task.creationDate" /></a></td>'								
				 + '</tr> ';
				 
		for ( var i = 0; i < tasks.length; i++) {
			formLink = createFormLink(tasks[i]["link"], tasks[i]["description"]);
			
			if((i+1)%2 == 0) {
				taskHtml += '<tr class="evenRow">'
			}else {
				taskHtml += '<tr>' 
			}
			
			taskHtml +=  '<td class="left">' + formLink + '</td>';
			/* process does not have state field */
			if(pageObj.taskType != 'process') {
				taskHtml += '<td>' + getTaskState(tasks[i]["state"]) + '</td>';
			}
			
			taskHtml += '<td>' + tasks[i]["creationDate"] + '</td>' 
					 + '</tr>';
		}

		taskHtml += '</table>';

		return taskHtml;
	}
	
	/**
	 * Decorate the task manager by adding background class when mousemove, mouseout, etc
	 */
	function decorateTaskManager() {
		var tr = $('.task-manager-table tr');

		for ( var i = 1; i < tr.length; i++) {			
			tr[i].onmousemove = function() {
				$(this).addClass('focusRow');
			}

			tr[i].onmouseout = function() {			
				$(this).removeClass('focusRow');
			}
			
			tr[i].onclick = function() {
				tr.removeClass('clickRow');
				$(this).addClass('clickRow');
			}
		}
		// select the default option according to number of page
		$('#num_per_page option[value='+pageObj.numPerPage+']').attr('selected', 'selected');
	}
	
	/**
	 * Return task state string for different language
	 */
	function getTaskState(state) {

		//var lang = "${pageContext.request.locale.language}";
		var newState = '';
		
		if(state == 'READY') {
			
			if(pageObj.taskType == 'task') { /* for the task, use 'new' to replace 'ready' */
				newState = "<spring:message code="task.newState" />";	
			} else {
				newState = "<spring:message code="task.readyState" />";	
			}
			
		} else if (state == 'CLAIMED') {
			newState = "<spring:message code="task.claimedState" />";	
		}
		
		return newState;
	}
	
	/**
	 * Create initial tasks table Html
	 */
	function createInitTable() {
		var taskHtml = "";
		taskHtml = '<table class="task-manager-table">'
				+ '<tr class="task-manager-table trheader">'
				+ '<td><spring:message code="task.description" /></td>'
				+ '<td><spring:message code="task.state" /></td>'
				+ '<td><spring:message code="task.creationDate" /></td>'								
				+ '</tr></table>';
				
		return taskHtml;
	}
	
	/**
	 * Create task form Html link for different open form types
	 */
	function createFormLink(link, description) {
		var linkHtml;
		
		/* 3 type values, 1: in portlet, 2: new window, 3: pop-up window */
		if(configObj.openForm == '1') {
			/* save the page parameters in session for returning back in order to keep the page unchanged*/
			var formUrl = "<%= formURL %>";
			var formUrl = formUrl.replace("CONSTANT_TASK_FORM_LINK", escape(link));
			var formUrl = formUrl.replace("CONSTANT_TASK_CURRENT_PAGE", pageObj.currentPage);
			var formUrl = formUrl.replace("CONSTANT_TASK_NUMBER_PER_PAGE", pageObj.numPerPage);
			var formUrl = formUrl.replace("CONSTANT_TASK_TASK_TYPE", pageObj.taskType);
			var formUrl = formUrl.replace("CONSTANT_TASK_KEYWORD", pageObj.keyword);
			var formUrl = formUrl.replace("CONSTANT_TASK_ORDER_TYPE", pageObj.orderType);
			linkHtml = '<a href="'+ formUrl + '">';
		}else if(configObj.openForm == '2') {
			linkHtml = '<a href="' + link + '" target="_blank">';
		}else if(configObj.openForm == '3') {
			linkHtml = '<a href="javascript:void(0)" onclick="popupTaskForm(\'' + link + '\')">';
		}
		
		if(description == '') { // no description for the task
			description = "<spring:message code="task.noDescription" />";
		}
		
		linkHtml += description + '</a>';
		
		return linkHtml;
	}

	/**
	 * Set auto refresh timer, which updates the task list automatically
	 */
	function setRefreshTimer() {
		var duration = parseInt(configObj.refreshDuration) * 1000; // convert to ms
		refreshTimer = setInterval('ajaxGetTasks()', duration);
	}
	
	/**
	 * Remove the auto refresh timer
	 */
	function removeRefreshTimer() {
		clearInterval(refreshTimer);
	}
	
	/**
	 * Reset the auto refresh timer
	 */
	function resetRefreshTimer() {
		removeRefreshTimer();
		setRefreshTimer();
	}
	/**
	 * Get the parameters stored in session when returns from the task form page,
	 * which is in order to keep the page unchanged 
	 */
	function checkPageSession() {
		var currentPage = "<%= currentPage %>";
		var numPerPage = "<%= numPerPage %>";
		var taskType = "<%= taskType %>";
		var keyword = "<%= keyword %>";
		var orderType = "<%= orderType %>";
		
		if(currentPage != 'null') {
			pageObj.currentPage = parseInt(currentPage);
			pageObj.numPerPage = numPerPage;
			pageObj.taskType = taskType;
			pageObj.keyword = keyword;
			pageObj.orderType = orderType;
			pageObj.taskTypeNum = getTaskTypeNum(taskType);
		}
		
	}
	
	/**
	 * Get task type number for the value in tasks type tab 
	 */
	function getTaskTypeNum(taskType) {		
		var taskTypeNum = 0;
		
		if(taskType == 'task') {
			taskTypeNum = 0;
		}else if(taskType == 'notification') {
			taskTypeNum = 1;
		}else if(taskType == 'process') {
			taskTypeNum = 2;
		}
		
		return taskTypeNum;
	}
	/**
	 * Config object to handle parameters in configuration mode
	 */	
	function config() {
		this.taskFilter = "<%= taskFilter %>";
		this.notifFilter = "<%= notifFilter %>";
		this.refreshDuration = "<%= refreshDuration %>";
		/* 3 type values, 1: in portlet, 2: new window, 3: pop-up window */
		this.openForm = "<%= openForm %>";
	}
	/**
	 * paging object to handle page operations
	 */
	function paging() {
		this.currentPage = 1;
		this.totalPages = 1;
		this.numPerPage = 10;
		this.totalItems;
		/* 3 types: task, notification, process */
		this.taskType = 'task';
		/* 0:task, 1:notification, 2: process */
		this.taskTypeNum = 0;
		/* keyword for searching and filter */
		this.keyword = configObj.taskFilter;
		/* 6 types: by description_desc, by description_asc, by state_desc, 
		by state_asc, by creationDate_desc, by creationDate_asc */
		this.orderType = 'creationDate_desc'; 
	}

	/**
	 * Set task type and do initialization to update task list
	 */
	paging.prototype.setTaskParams = function(type) {
		this.taskTypeNum = type;
		
		if (this.taskTypeNum == 0) {
			this.taskType = 'task';
			this.keyword = configObj.taskFilter;
		} else if (this.taskTypeNum == 1) {
			this.taskType = 'notification';
			this.keyword = configObj.notifFilter;
		} else if (this.taskTypeNum == 2) {
			this.taskType = 'process';
			this.keyword = '';
			this.setProcessOrderType();
		}
		
		this.currentPage = 1;
	}
	
	/** 
	 * Set process order type. Since process does not have state field, set 
	 * default order type creationDate_desc for process if current order type
	 * is related to state
	 */
	paging.prototype.setProcessOrderType = function() {
		if(this.orderType == 'state_desc' || this.orderType == 'state_asc') {
			this.orderType = 'creationDate_desc';
		}
	}
	
	/* get the first page number */
	paging.prototype.getFirstPage = function() {
		var firstPage = this.currentPage != 1 ? 1 : null;
		return firstPage;
	}
	/* get the previous page number */
	paging.prototype.getPrePage = function() {
		var prePage = this.currentPage > 1 ? this.currentPage - 1 : null;
		return prePage;
	}
	/* get the next page number */
	paging.prototype.getNextPage = function() {
		var nextPage = this.currentPage < this.totalPages ? this.currentPage + 1 : null;
		return nextPage;
	}
	/* get the last page number */
	paging.prototype.getLastPage = function() {
		var lastPage = this.currentPage != this.totalPages ? this.totalPages : null;
		return lastPage;
	}
	
	/* move to the the first page */
	paging.prototype.moveFirst = function() {
		var firstPage = this.getFirstPage();
		
		if(firstPage != null) {
			this.currentPage = firstPage;
			ajaxGetTasks();
		}
	}
	/* move to the the previous page */
	paging.prototype.movePre = function() {
		var prePage = this.getPrePage();
		
		if(prePage != null) {
			this.currentPage = prePage;
			ajaxGetTasks();
		}
	}
	/* move to the the next page */
	paging.prototype.moveNext = function() {
		var nextPage = this.getNextPage();
		
		if(nextPage != null) {
			this.currentPage = nextPage;
			ajaxGetTasks();
		}
	}
	/* move to the the last page */
	paging.prototype.moveLast = function() {
		var lastPage = this.getLastPage();
		
		if(lastPage != null) {
			this.currentPage = lastPage;
			ajaxGetTasks();
		}
	}
	
	/**
	 * handle the page changing in Html page
	 */
	function movePage(pageNum) {
		switch (pageNum) {
		case 'first':
			pageObj.moveFirst();
			break;
		case 'previous':
			pageObj.movePre();
			break;
		case 'next':
			pageObj.moveNext();
			break;
		case 'last':
			pageObj.moveLast();
			break;
		}
	}
	
	/**
	 * Show/hide search user interface
	 */
	function showSearchUI() {

		$('#task-manager-search').toggle('fast');
	}

	/**
	 * Perform search tasks
	 */
	function searchTasks() {
		var keyword = $("input#keyword").val();
		pageObj.keyword = keyword;
		ajaxGetTasks();
		return false;
	}
	
 	/**
 	 * Reset the search result and clear the keyword
 	 */
	function resetSearch() {
		$("input#keyword").val('');
		var taskHtml = createInitTable();
		$('#task-manager-tasklist').html(taskHtml);
	}
	
	/**
 	 * Create frame Html
 	 */
	function createFrame(formLink) {
		var frameHtml = '<iframe src="' + formLink + '" style="width:100%; height:100%" frameborder="0" id="taskform" scrolling="auto"></iframe>';
		return frameHtml;
	}
	
	/**
	 * Show task form in a frame in portlet in the current window state of portlet
	 */
	function showTaskForm(formLink) {
		removeRefreshTimer();
		var frameHtml = createFrame(formLink);
		$('#task-manager-tasklist').html(frameHtml);
		var pageHtml = createFormPage();
		$('#task-manager-operation-page').html(pageHtml);
	}
	
	/**
	 * Show task form in pop up window
	 */
	function popupTaskForm(formLink) {		
		var w = screen.width; // var w = 900;
		var h = screen.height; // var h = 650;
		var left = (screen.width/2)-(w/2);
		var top = (screen.height/2)-(h/2);
		
		var popupUrl = "<%= popupURL %>";
		var popupUrl = popupUrl.replace("CONSTANT_TASK_FORM_LINK", escape(formLink));
		var pWindow = window.open(popupUrl, 'popwindow','scrollbars=no, resizable=yes, width='+w+', height='+h+', top='+top+', left='+left);
		// var pWindow = window.open(formLink, 'popwindow','scrollbars=yes, resizable=yes, width='+w+', height='+h+', top='+top+', left='+left);
		var popupObj = new popupWindow(pWindow);
		popupObj.run();
	}
	
	/**
	 * Popup window object, which checks the status itself. If the popup window
	 * is closed, then refresh the task list to be updated.
	 */
	 function popupWindow(pWindow) {
		this.pWindow = pWindow;
		this.timer;
		this.duration = 500;
		this.check = popupCheck;
		this.run = popupRun;
	}
		
	function popupRun() {
		var self = this;
		this.timer = setInterval(function(){self.check();}, this.duration);
	}

	function popupCheck() {
		if(this.pWindow.closed == true) {
			ajaxGetTasks();
			clearInterval(this.timer);
		}
	}
	
	/**
	 * Create task manager operation part including changing page number and search field
	 */
	function createTasksPage() {
		var pageHtml = '<ul>'
					 + '<li><input type="button" value="<spring:message code="task.search"/>"  onclick="showSearchUI()" /></li>'
					 + '<li><select id="num_per_page" onchange="changeNumberPerPage(this.options[this.selectedIndex].value)"><option value="10">10</option><option value="15">15</option><option value="20">20</option><option value="25">25</option><option value="40">40</option></select></li>'
					 + '<li><a><img src="<%= request.getContextPath() %>/images/first.gif" onclick="movePage(\'first\')"/></a></li>'
					 + '<li><a><img src="<%= request.getContextPath() %>/images/prev.gif" onclick="movePage(\'previous\')"/></a></li>'
					 + '<li><spring:message code="task.page"/> ' + '<form id="page_num" style="display:inline;" onsubmit="changePageNumber(); return false;"><input type="text" value="'+ pageObj.currentPage +'" size="2"></form>' + '/' + pageObj.totalPages + '</li>'
					 + '<li><a><img src="<%= request.getContextPath() %>/images/next.gif" onclick="movePage(\'next\')"/></a></li>'
					 + '<li><a><img src="<%= request.getContextPath() %>/images/last.gif" onclick="movePage(\'last\')"/></a></li>'
					 + '<li><spring:message code="task.displaying"/> ' + createDisplayingTasksNum()  + '</li>'
					 + '</ul>';
								
		return pageHtml;
	}
	/**
	 * Changes the number of items to be shown in one page
	 */
	function changeNumberPerPage(numberperpage) {
		pageObj.numPerPage = numberperpage;
		pageObj.currentPage = 1;
		ajaxGetTasks();
	}	
	/**
	 * Changes the page number
	 */
	function changePageNumber() {
		var pageNum = $('#page_num input').val();
		
		if(!isNaN(pageNum)) {
			if(pageNum < 1) {
				pageObj.currentPage = 1;				
			}else if(pageNum > pageObj.totalPages) {
				pageObj.currentPage = pageObj.totalPages;
			}else {
				pageObj.currentPage = pageNum;
			}
			ajaxGetTasks();
		}else {
			$('#page_num input').val(pageObj.currentPage);
		}
		
	}
	/**
	 * Show error message to inform user
	 */
	function showErrorMessage(message) {
		var msgHtml = '<div class="task-error-message" >' + message + '</div>';
		$('#task-manager-operation-page').html(msgHtml);
	}
	
	/**
	 * Create task statistics information
	 */
	function createDisplayingTasksNum() {
		var displayTask;
		var startid, endid;
		var numPerPage = parseInt(pageObj.numPerPage);	
		
		if(parseInt(pageObj.totalItems) == 0) {
			return "<spring:message code="task.noItems"/>";
		}
		
		startid = (pageObj.currentPage-1)*numPerPage + 1;
		
		if(pageObj.currentPage < pageObj.totalPages) {
			endid = startid + numPerPage -1;
		}else {
			endid = startid + pageObj.totalItems - (pageObj.currentPage-1)* numPerPage -1;
		}
		
		displayTask = startid + '-' + endid + '/' + pageObj.totalItems;
		
		return displayTask;
	}
	
	/**
	 * Create 'Return' button Html
	 */
	function createFormPage() {
		var pageHtml = '<ul><li><input type="button" value="Return" onclick="returnPage()" /></li></ul>';
		return pageHtml;
	}
	
	/**
	 * Return from task form page to main page
	 */
	function returnPage() {
		setRefreshTimer();
		ajaxGetTasks();
	}
	
	/**
	 * Order tasks operation
	 */
	function orderTask(type) {
		var orderType = pageObj.orderType;
		var newOrderType;
		
		if(type == 'description') {
			if(orderType == 'description_asc') {
				newOrderType = 'description_desc';
			}else {
				newOrderType = 'description_asc';
			}			
		}else if(type == 'state') {
			if(orderType == 'state_asc') {
				newOrderType = 'state_desc';
			}else {
				newOrderType = 'state_asc';
			}
		}else if(type == 'creationDate') {
			if(orderType == 'creationDate_desc') {
				newOrderType = 'creationDate_asc';
			}else {
				newOrderType = 'creationDate_desc';
			}
		}
		
		pageObj.orderType = newOrderType;
		
		ajaxGetTasks();
	}
	
</script>

<div id="task-manager-wrap">
	<%@ include file="navi.jsp"%>
	<div id="task-manager-head">
		<div id="task-manager-search" class="task-manager-operation-part">
			<form name="searchForm" onsubmit="searchTasks(); return false;">		
				<spring:message code="task.searchKeyword" />
				<input type="text" name="keyword" id="keyword" style="width: 160px;" /> 
				<input type="submit" value="<spring:message code="task.search"/>" />
				<input type="button" value="<spring:message code="task.searchReset"/>" onclick="resetSearch()" />	
			</form>
		</div>
		<div id="task-manager-operation" class="task-manager-operation-part">
			<div id="task-manager-operation-page"></div>
			<div id="task-manager-operation-loading"><spring:message code="taskPage.loading"/></div>
		</div>
	</div>
	<div id="task-manager-tasklist">
		<table class="task-manager-table">
			<tr class="task-manager-table trheader">
				<td><spring:message code="task.description" /></td>
				<td><spring:message code="task.state" /></td>
				<td><spring:message code="task.creationDate" /></td>								
			</tr> 
		</table>
	</div>
</div>


