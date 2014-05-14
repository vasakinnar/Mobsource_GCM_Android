package com.mobsource.gcm.lib;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Kinnar Vasa
 *
 */
public class StateChangeManager {
	private final Set<GCMListener> listeners = new HashSet<GCMListener>();

	public void registerListener(GCMListener listener) {
		listeners.add(listener);
	}

	public void unRegisterListener(GCMListener listener) {
		listeners.remove(listener);
	}

	public void clearListeners() {
		listeners.clear();
	}

	public void notifyRegisterChange(RegisterEnum registerEnum, String txt) {

		for (GCMListener listener : listeners) {
			listener.onRegister(registerEnum, txt);
		}
	}

	public void notificationReceivedChange(String txt, String type) {

		for (GCMListener listener : listeners) {
			listener.OnNotificationReceived(txt, type);
		}
	}
}
