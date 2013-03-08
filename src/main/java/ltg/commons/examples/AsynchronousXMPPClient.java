package ltg.commons.examples;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import ltg.commons.MessageListener;
import ltg.commons.SimpleXMPPClient;

/**
 * This example demonstrates the use of SimpleXMPPClient class.
 * The class is used to ashynchronously process packets.
 * This is useful whenever the main thread is already busy doing 
 * stuff and we want to interrupt.
 * 
 * @author tebemis
 *
 */
public class AsynchronousXMPPClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleXMPPClient sc = null;
		try {
			sc = new SimpleXMPPClient("fg-test@ltg.evl.uic.edu", 
					"fg-test", 
					"fg-pilot-oct12@conference.ltg.evl.uic.edu");
		} catch (XMPPException e) {
			System.exit(-1);
		}
		
		// We are now connected and in the group chat room. If we don't do something
		// the main will terminate... 
		
		// ... so let's go ahead and make ourself busy... but before we do that... 
		 
		// ... let's register the packet handler with our XMPP client...
		sc.registerEventListener(new MessageListener() {
			
			@Override
			public void processMessage(Message m) {
				System.out.println(m.getBody());
			}
		});
		
		// ... and now we can make ourselves busy
		while(true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				sc.disconnect();
			}
		}
		
		// NOTE: unless the main thread is suspended we will never get to this point 
	}

}
