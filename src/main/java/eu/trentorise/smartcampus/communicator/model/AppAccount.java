package eu.trentorise.smartcampus.communicator.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AppAccount {
	private String id;
	private String appName;

	@XmlElementWrapper
	@XmlElement(name = "configuration")
	private List<Configuration> configurations;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public List<Configuration> getConfigurations() {
		return configurations;
	}

	public void setConfigurations(List<Configuration> configurations) {
		this.configurations = configurations;
	}

	public List<CloudToPushType> getCloudToPushTypeConfigured() {
		List<Configuration> list = getConfigurations();
		List<CloudToPushType> result = new ArrayList<CloudToPushType>();
		for (Configuration c : list) {
			if (c.getType() != null)
				result.add(c.getType());
		}
		return result;
	}

}
