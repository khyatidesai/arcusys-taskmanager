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

package fi.arcusys.oulu.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Util {
	
	public static final String TMS_WSDL_KEY = "tms.wsdl.url";
	public static final String TOKEN_WSDL_KEY = "token.wsdl.url";
	public static final String INTALIO_URL = "intalio.url";
	public static final String INTALIO_REALM = "intalio.realm";
	
	private Util() {
		
	}
	
	public static Properties loadProperties() {
		Properties props = null;
		InputStream is = null;
		try {
			is = Util.class.getResourceAsStream("/intalio.properties");
			props = new Properties();
			props.load(is);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}  finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ioe) {
					//ignore
				}
			}
		}		
		return props;
	}
}
