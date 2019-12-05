package de.incentergy.base.menu.jms;

import de.incentergy.base.menu.domain.NavigationListItemList;

public class DeploymentEvent {
	private Lifecycle lifecycle;
	private String applicationName; 
	private NavigationListItemList navigationListItemList = new NavigationListItemList();
	
	public DeploymentEvent() {
		
	}

	public DeploymentEvent(Lifecycle lifecycle, String applicationName) {
		super();
		this.lifecycle = lifecycle;
		this.applicationName = applicationName;
	}

	public Lifecycle getLifecycle() {
		return lifecycle;
	}
	public void setLifecycle(Lifecycle lifecycle) {
		this.lifecycle = lifecycle;
	}
	public NavigationListItemList getNavigationListItemList() {
		return navigationListItemList;
	}
	public void setNavigationListItemList(NavigationListItemList navigationListItemList) {
		this.navigationListItemList = navigationListItemList;
	}
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
}
