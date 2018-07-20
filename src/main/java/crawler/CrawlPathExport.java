package crawler;

import java.util.ArrayList;
import java.util.List;

import com.crawljax.core.state.CrawlPath;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification.How;

public class CrawlPathExport {

	public List<EventableExport> eventableExportList = new ArrayList<EventableExport>();

	public CrawlPathExport(CrawlPath path) {
		for (Eventable eventable : path) {
			EventableExport eventableExport = new EventableExport(eventable);
			this.eventableExportList.add(eventableExport);
		}
	}

}

