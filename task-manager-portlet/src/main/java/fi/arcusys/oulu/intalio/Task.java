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

/**
 * task data model for task manager, modified from to intalio task object
 * @author Jinhua Chen
 * May 9, 2011
 */
public class Task {

	private String id;
	private String type;
	private String state;
	private String processId;
	private String description;
	private String creationDate;
	private String link;	
	
	public Task() {}
	
	public String getId() {
		return id;
	}
	
	public String getType() {
		return type;
	}
	
	public String getState() {
		return state;
	}
	
	public String getProcessId() {
		return processId;
	}
	public String getDescription() {
		return description;
	}
	
	public String getCreationDate() {
		return creationDate;
	}
	
	public String getLink() {
		return link;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setState(String state) {
		this.state = state;
	}
	
	public void setProcessId(String processId) {
		this.processId = processId;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}
	
	public void setLink(String link) {
		this.link = link;
	}

}
