/**
 * @author azraellong
 * @date 2012-11-13
 */
package com.alloyteam.net;

import java.util.ArrayList;

/**
 * @author azraellong
 *
 */
public class ConnectionManager {
	
	public static final int MAX_CONNECTIONS = 5;

	private ArrayList<Runnable> active = new ArrayList<Runnable>();
	private ArrayList<Runnable> queue = new ArrayList<Runnable>();

	private static ConnectionManager instance;

	public static ConnectionManager getInstance() {
		if (instance == null)
			instance = new ConnectionManager();
		return instance;
	}

	public void push(Runnable runnable) {
		queue.add(runnable);
		if (active.size() < MAX_CONNECTIONS)
			next();
	}

	private void next() {
		if (!queue.isEmpty()) {
			Runnable next = queue.get(0);
			queue.remove(0);
			active.add(next);

			Thread thread = new Thread(next);
			thread.start();
		}
	}

	public void didComplete(Runnable runnable) {
		active.remove(runnable);
		next();
	}
}
