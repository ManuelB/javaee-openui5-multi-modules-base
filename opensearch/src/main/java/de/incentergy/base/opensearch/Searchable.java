package de.incentergy.base.opensearch;

import org.jboss.resteasy.plugins.providers.atom.Entry;

public interface Searchable {
	public Entry toEntry();
}
