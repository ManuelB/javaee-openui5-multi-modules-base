package de.incentergy.base.opensearch.jaxrs;

import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;

import de.incentergy.base.opensearch.Searchable;

@Stateless
@Path("/search")
public class Search {

	@PersistenceContext
	EntityManager em;

	@GET
	public Feed search(@QueryParam("q") String q) {
		Feed feed = new Feed();

		FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);

		Query query;
		try {
			query = new QueryParser(q, new StandardAnalyzer()).parse(q);
		} catch (ParseException e) {
			throw new WebApplicationException("Could not parse query: " + e.getMessage(), Response.Status.BAD_REQUEST);
		}
		javax.persistence.Query fullTextQuery = fullTextEntityManager.createFullTextQuery(query);

		@SuppressWarnings("unchecked")
		List<Object> result = fullTextQuery.getResultList(); // return a list of managed objects

		feed.getEntries()
				.addAll(result.stream().map(o -> (o instanceof Searchable ? ((Searchable) o).toEntry() : toEntry(o)))
						.collect(Collectors.toList()));

		return feed;
	}

	private Entry toEntry(Object o) {
		Entry entry = new Entry();
		entry.setTitle(o.toString());
		return entry;
	}

}
