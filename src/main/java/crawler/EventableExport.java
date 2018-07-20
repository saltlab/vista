package crawler;

import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification.How;

public class EventableExport {
	public How getHow;
	public String getValue;
	public String getSource;
	public String getTarget;

	public EventableExport(Eventable eventable) {
		this.getHow = eventable.getIdentification().getHow();
		this.getValue = eventable.getIdentification().getValue();
		this.getSource = eventable.getSourceStateVertex().getName();
		this.getTarget = eventable.getTargetStateVertex().getName();
	}
}
