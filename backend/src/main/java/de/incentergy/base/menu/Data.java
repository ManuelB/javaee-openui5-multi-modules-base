package de.incentergy.base.menu;

import java.util.HashSet;
import java.util.Set;

import de.incentergy.base.menu.entities.NavigationListItem;
import de.incentergy.base.menu.entities.NavigationListItemLocalized;

public class Data {

	public static NavigationListItem createNavigationListItem() {
		NavigationListItem navigationListItem = new NavigationListItem();
		navigationListItem.setId("93f02341-0b7b-4fd7-8690-ef19a8fe9ad3");
		
		navigationListItem.getNavigationListItemLocalized().put("de", new NavigationListItemLocalized("de", "Entwickler Test", navigationListItem));
		navigationListItem.getNavigationListItemLocalized().put("en", new NavigationListItemLocalized("en", "Developer Test", navigationListItem));
		
		navigationListItem.setRoleAllowed("Developer" );
		
		NavigationListItem navigationListItemSub = new NavigationListItem();
		navigationListItemSub.setId("5178af38-0c8d-4a19-9dfe-be3dadad95c4");
		navigationListItemSub.setTopLevelItem(false);
		navigationListItemSub.getNavigationListItemLocalized().put("de", new NavigationListItemLocalized("de", "Entwickler Sub Test", navigationListItemSub));
		navigationListItemSub.getNavigationListItemLocalized().put("en", new NavigationListItemLocalized("en", "Developer Sub Test", navigationListItemSub));
		
		navigationListItemSub.setRoleAllowed("Developer" );
		
		navigationListItem.getChildren().add(navigationListItemSub);
		navigationListItemSub.setParent(navigationListItem);
		
		return navigationListItem ;
	}

}
