/**
 * 
 */
package ltg.commons;

/**
 * @author tebemis
 *
 */
public class Event {
	
	public String origin = null;
	public String payload = null;	
	
	
	public Event(String origin, String payload) {
		this.origin = origin;
		this.payload = payload;
	}
	
	
	@Override
	public String toString() {
		return "origin : " + origin + ", payload : " + payload;
	}


}
