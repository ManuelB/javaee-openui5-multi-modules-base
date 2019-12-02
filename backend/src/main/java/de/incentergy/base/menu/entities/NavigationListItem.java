package de.incentergy.base.menu.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.servlet.http.HttpServletRequest;

@Entity
public class NavigationListItem {

	private static final Logger log = Logger.getLogger(NavigationListItem.class.getName());

	@Id
	private String id = UUID.randomUUID().toString();

	@ManyToOne()
	private NavigationListItem parent;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<NavigationListItem> children = new ArrayList<>();

	@OneToMany(mappedBy = "navigationListItem", cascade = CascadeType.ALL, orphanRemoval = true)
	@MapKeyColumn(name = "locale")
	private Map<String, NavigationListItemLocalized> navigationListItemLocalized = new HashMap<>();

	private String roleAllowed = null;

	// Necessary for olingo, even when it is not possible to save it
	private String text;
	
	private String route;
	
	private Double sort = 0.0;
	
	private Boolean topLevelItem = true;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public NavigationListItem getParent() {
		return parent;
	}

	public void setParent(NavigationListItem parent) {
		this.parent = parent;
	}

	public List<NavigationListItem> getChildren() {
		return children;
	}

	public void setChildren(List<NavigationListItem> children) {
		this.children = children;
	}

	public Map<String, NavigationListItemLocalized> getNavigationListItemLocalized() {
		return navigationListItemLocalized;
	}

	public void setNavigationListItemLocalized(Map<String, NavigationListItemLocalized> navigationListItemLocalized) {
		this.navigationListItemLocalized = navigationListItemLocalized;
	}

	public String getText() {
		NavigationListItemLocalized nlil = navigationListItemLocalized.get(getCurrentLocale());
		return nlil == null ? null : nlil.getText();
	}

	public void setText(String text) {
		log.log(Level.WARNING, "Tried to set directly text property on {0} to {1}", new Object[] { getId(), text });
	}

	public String getRoleAllowed() {
		return roleAllowed;
	}

	public void setRoleAllowed(String roleAllowed) {
		if(!getTopLevelItem()) {
			log.warning("Only top level items can have an allowed role");
		}
		this.roleAllowed = roleAllowed;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public Double getSort() {
		return sort;
	}

	public void setSort(Double sort) {
		this.sort = sort;
	}

	public Boolean getTopLevelItem() {
		return topLevelItem;
	}

	public void setTopLevelItem(Boolean topLevelItem) {
		this.topLevelItem = topLevelItem;
	}

	public String getCurrentLocale() {
		BeanManager beanManager = getBeanManager();
		@SuppressWarnings("unchecked")
		Bean<HttpServletRequest> bean = (Bean<HttpServletRequest>) beanManager.getBeans(HttpServletRequest.class)
				.iterator().next();

		CreationalContext<HttpServletRequest> ctx = beanManager.createCreationalContext(bean);

		HttpServletRequest httpServletRequest = (HttpServletRequest) beanManager.getReference(bean,
				HttpServletRequest.class, ctx);

		String acceptLanguage = httpServletRequest.getHeader("Accept-Language");

		return acceptLanguage != null && acceptLanguage.length() > 2 ? acceptLanguage.substring(0, 2) : "en";
	}

	public BeanManager getBeanManager() {
		try {
			InitialContext initialContext = new InitialContext();
			return (BeanManager) initialContext.lookup("java:comp/BeanManager");
		} catch (NamingException e) {
			log.log(Level.SEVERE, "Couldn't get BeanManager through JNDI", e);
			return null;
		}

	}
}
