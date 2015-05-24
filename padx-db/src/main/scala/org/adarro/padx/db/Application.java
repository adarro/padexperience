package org.adarro.padx.db;

/*
 * #%L
 * padx-db
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 TruthEncode
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.IOException;

import com.couchbase.lite.Context;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.JavaContext;
import com.couchbase.lite.Manager;
import com.couchbase.lite.util.Log;

public class Application {

	private Manager manager;
	private static Context mContext;
	private static Object lock = new Object();

	private Application() {
		mContext = getApplicationContext();
		try {
			/*
			 * In Java the Manager instance and all the objects descending from
			 * it may be used on any thread.
			 */
			manager = new Manager(new JavaContext("data"),
					Manager.DEFAULT_OPTIONS);

		} catch (IOException e) {
			Log.e(Log.TAG_DATABASE, "Cannot create Manager instance", e);

		}
	}

	private boolean isInitialized = false;

	private Context getApplicationContext() {
		Context c = null;
		synchronized (lock) {
			if (!isInitialized) {
				c = new JavaContext("data");
				isInitialized = true;
			}
		}

		return c == null ? mContext : c;
	}
	

	public static void main(String[] args) {
		Application app = new Application();
		try {
			Database db = app.manager.getDatabase("padx");
			
		} catch (CouchbaseLiteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
