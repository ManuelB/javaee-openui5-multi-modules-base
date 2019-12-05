package de.incentergy.base.menu.domain;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import de.incentergy.base.menu.entities.NavigationListItem;

@XmlRootElement
public class NavigationListItemList {
	private List<NavigationListItem> items = new ArrayList<>();

	public List<NavigationListItem> getItems() {
		return items;
	}

	public void setItems(List<NavigationListItem> items) {
		this.items = items;
	}
	
	public NavigationListItemList addItem(NavigationListItem navigationListItem) {
		items.add(navigationListItem);
		return this;
	}
}
