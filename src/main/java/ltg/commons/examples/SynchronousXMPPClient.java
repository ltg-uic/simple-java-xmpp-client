package ltg.commons.examples;

import org.jivesoftware.smack.XMPPException;

import ltg.commons.SimpleAndroidXMPPClient;

/**
 * This example demonstrates the use of SimpleXMPPClient class.
 * The class is used to synchronously wait for packets and then process them,
 * a typical scenario when designing simple agents.
 * 
 * @author tebemis
 *
 */
public class SynchronousXMPPClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleAndroidXMPPClient sc = null;
		try {
			sc = new SimpleAndroidXMPPClient("fg-test@ltg.evl.uic.edu", 
					"fg-test", 
					"fg-pilot-oct12@conference.ltg.evl.uic.edu");
		} catch (XMPPException e) {
			System.exit(-1);
		}
		
		// We are now connected and in the group chat room. If we don't do something
		// the main will terminate... 
		
		// ... so let's go ahead and wait for a message to arrive...
		while (!Thread.currentThread().isInterrupted()) {
			// ... and process it ...
			System.out.println(sc.nextMessage());
		}
		// ... and finally disconnect.
		sc.disconnect();
	}

}
