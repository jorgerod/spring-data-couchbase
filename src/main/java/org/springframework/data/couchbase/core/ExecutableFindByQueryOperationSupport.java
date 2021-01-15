/*
 * Copyright 2012-2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.couchbase.core;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.couchbase.core.ReactiveFindByQueryOperationSupport.ReactiveFindByQuerySupport;
import org.springframework.data.couchbase.core.query.Query;

import com.couchbase.client.java.query.QueryScanConsistency;

/**
 * {@link ExecutableFindByQueryOperation} implementations for Couchbase.
 *
 * @author Michael Nitschinger
 * @author Michael Reiche
 */
public class ExecutableFindByQueryOperationSupport implements ExecutableFindByQueryOperation {

	private static final Query ALL_QUERY = new Query();

	private final CouchbaseTemplate template;

	public ExecutableFindByQueryOperationSupport(final CouchbaseTemplate template) {
		this.template = template;
	}

	@Override
	public <T> ExecutableFindByQuery<T> findByQuery(final Class<T> domainType) {
		return new ExecutableFindByQuerySupport<>(template, domainType, ALL_QUERY, QueryScanConsistency.NOT_BOUNDED,
				"_default._default");
	}

	static class ExecutableFindByQuerySupport<T> implements ExecutableFindByQuery<T> {

		private final CouchbaseTemplate template;
		private final Class<T> domainType;
		private final Query query;
		private final ReactiveFindByQuerySupport<T> reactiveSupport;
		private final QueryScanConsistency scanConsistency;
		private final String collection;

		ExecutableFindByQuerySupport(final CouchbaseTemplate template, final Class<T> domainType, final Query query,
				final QueryScanConsistency scanConsistency, final String collection) {
			this.template = template;
			this.domainType = domainType;
			this.query = query;
			this.reactiveSupport = new ReactiveFindByQuerySupport<T>(template.reactive(), domainType, query, scanConsistency,
					collection);
			this.scanConsistency = scanConsistency;
			this.collection = collection;
		}

		@Override
		public T oneValue() {
			return reactiveSupport.one().block();
		}

		@Override
		public T firstValue() {
			return reactiveSupport.first().block();
		}

		@Override
		public List<T> all() {
			return reactiveSupport.all().collectList().block();
		}

		@Override
		public TerminatingFindByQuery<T> matching(final Query query) {
			QueryScanConsistency scanCons;
			if (query.getScanConsistency() != null) {
				scanCons = query.getScanConsistency();
			} else {
				scanCons = scanConsistency;
			}
			return new ExecutableFindByQuerySupport<>(template, domainType, query, scanCons, collection);
		}

		@Override
		public FindByQueryConsistentWith<T> consistentWith(final QueryScanConsistency scanConsistency) {
			return new ExecutableFindByQuerySupport<>(template, domainType, query, scanConsistency, collection);
		}

		@Override
		public FindByQueryInCollection<T> inCollection(final String collection) {
			return new ExecutableFindByQuerySupport<>(template, domainType, query, scanConsistency, collection);
		}

		@Override
		public Stream<T> stream() {
			return reactiveSupport.all().toStream();
		}

		@Override
		public long count() {
			return reactiveSupport.count().block();
		}

		@Override
		public boolean exists() {
			return count() > 0;
		}

	}

}
