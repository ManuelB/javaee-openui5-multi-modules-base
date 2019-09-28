package de.incentergy.base.opensearch.hibernate;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;

@Singleton
@Startup
public class Indexer {
	
	private static Logger log = Logger.getLogger(Indexer.class.getName());

	@PersistenceContext
	EntityManager em;
	
	@PostConstruct
	public void init() {
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
		try {
			log.info("Starting Hibernate Search Indexer");
			fullTextEntityManager.createIndexer().startAndWait();
		} catch (InterruptedException e) {
			log.log(Level.SEVERE, "Could not start indexer", e);
		}
	}
}
