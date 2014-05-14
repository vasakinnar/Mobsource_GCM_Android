package com.mobsource.gcm.lib;

/**
 * 
 * @author Kinnar Vasa
 *
 */
public interface GCMListener {

	public void onRegister(RegisterEnum registerEnum, String message);
	
	public void OnNotificationReceived(String message, String gcmType);
}
