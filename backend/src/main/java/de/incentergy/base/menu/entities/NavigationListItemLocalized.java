package de.incentergy.base.menu.entities;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "xsi:type")
@Entity
public class NavigationListItemLocalized {

	@Id
	private String id = UUID.randomUUID().toString();
	private String locale;
	private String text;

	@ManyToOne
	private NavigationListItem navigationListItem;

	public NavigationListItemLocalized() {

	}

	public NavigationListItemLocalized(String locale, String text, NavigationListItem navigationListItem) {
		super();
		this.locale = locale;
		this.text = text;
		this.navigationListItem = navigationListItem;
	}

	@XmlID
	// required by Jackson
	@XmlAttribute(name = "id")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	@XmlIDREF
	public NavigationListItem getNavigationListItem() {
		return navigationListItem;
	}

	public void setNavigationListItem(NavigationListItem navigationListItem) {
		this.navigationListItem = navigationListItem;
	}
}
