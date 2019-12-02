package de.incentergy.base.menu;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Singleton
@Startup
public class Bootstrap {

	@PersistenceContext
	EntityManager em;

	@PostConstruct
	public void init() {
		em.persist(Data.createNavigationListItem());
	}
}
