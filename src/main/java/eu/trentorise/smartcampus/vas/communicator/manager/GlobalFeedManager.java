package eu.trentorise.smartcampus.vas.communicator.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.communicator.model.Notification;
import eu.trentorise.smartcampus.communicator.model.Preference;
import eu.trentorise.smartcampus.network.JsonUtils;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.vas.communicator.storage.CommunicatorStorage;

@Component
@SuppressWarnings("rawtypes")
public class GlobalFeedManager {

	@Autowired
	CommunicatorStorage storage;
	@Value("${globalfeed.file}")
	private Resource feedFile;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Scheduled(fixedRate = 20000)
	public void updateMessages() {
		
		if (feedFile == null) return;
		
		try {
			String json = readJson();
			List<Map> list = JsonUtils.toObjectList(json, Map.class);
			processFeed(list);
		} catch (Exception e) {
			logger .error("Problem reading file: "+ e.getMessage());
		}

	}


	private String readJson() throws IOException {
		String json = "";
		BufferedReader bin = null;
		try {
			bin = new BufferedReader(new InputStreamReader(feedFile.getInputStream()));
			String line = null;
			while ((line = bin.readLine()) != null) {
				json += line + "\n";
			}
		} finally {
			if (bin != null) bin.close();
		}
		return json;
	}

	
	private void processFeed(List<Map> list) throws DataException {
		if (list != null) {
			 List<Preference> prefs = storage.getObjectsByType(Preference.class);
			for (Map<String,Object> map : list) {
				Notification n = JsonUtils.convert(map, Notification.class);
				n.setType(CommunicatorManager.GLOBAL_FEED);
				n.setTimestamp(System.currentTimeMillis());
				try {
					Notification old = storage.getObjectById(n.getId(), Notification.class);
					if (old != null) continue; 
				} catch (NotFoundException e) {
				}
				storage.storeObject(n);
				for (Preference p : prefs) {
					if (p.getUser().equals("37")) {
						Notification un = n.copy("37");
						storage.storeObject(un);
					}
				}
			}
		}
		
	}
	
}
