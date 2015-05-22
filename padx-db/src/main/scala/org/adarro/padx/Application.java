package org.adarro.padx;

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
