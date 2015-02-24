/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.cogchar.name.lifter;

import org.cogchar.name.dir.AssumedQueryDir;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class UserCN {
	
	public static final String LOGIN_PAGE_QUERY_URI = AssumedQueryDir.LOGIN_PAGE_QUERY_URI; // "ccrt:find_login_page_99";
	public static final String USER_QUERY_URI = AssumedQueryDir.USER_QUERY_URI; //  "ccrt:find_users_99";
	
	public static final String LOGIN_PAGE_VAR_NAME = "loginPage";
	public static final String USER_VAR_NAME = "user";
	public static final String PASSWORD_VAR_NAME = "password";
	public static final String SALT_VAR_NAME = "salt";
	public static final String START_PAGE_VAR_NAME = "startPage";
	public static final String USER_CLASS_VAR_NAME = "userClass";
}
