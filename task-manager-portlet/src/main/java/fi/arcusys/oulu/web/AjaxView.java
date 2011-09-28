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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.view.AbstractView;

/**
 * Implements ajax view for ajax response in json format
 * @author Jinhua Chen
 * May 11, 2011
 */
public class AjaxView extends AbstractView {

    private static final String DEFAULT_AJAX_CONTENT_TYPE = "text/plain; charset=UTF-8";

    public AjaxView() {
		super();
		setContentType(DEFAULT_AJAX_CONTENT_TYPE);
	}
    
    @Override
    public String getContentType () {
        String orgiContentType = super.getContentType ();
        
        if (StringUtils.isEmpty (orgiContentType)) {
            orgiContentType = DEFAULT_AJAX_CONTENT_TYPE;
        }

        return orgiContentType;
    }

    @Override
    public void setContentType (String contentType) {
        super.setContentType (contentType);
    }

    @Override
    protected void renderMergedOutputModel (Map<String, Object> map, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        if (map == null || map.isEmpty ()) {
            JSONObject.fromObject ("{}").write (response.getWriter ());
            return;
        }

        JSON json = JSONSerializer.toJSON (map);
        json.write (response.getWriter ());
    }

}
