package orbits;

import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings("serial")
public class CommandSet extends ArrayList<String> {
	public Date base;
	private int timeStepSeconds;
	
	public CommandSet(Date base, int timeStepSeconds){
		this.base = base;
		this.timeStepSeconds = timeStepSeconds;
	}
	
	public Date getBase(){
		return base;
	}
	
	public int getTimeStep(){
		return timeStepSeconds;
	}
}
