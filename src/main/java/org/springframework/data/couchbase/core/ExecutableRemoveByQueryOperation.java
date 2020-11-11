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

import org.springframework.data.couchbase.core.query.Query;

import com.couchbase.client.java.query.QueryScanConsistency;

public interface ExecutableRemoveByQueryOperation {

	<T> ExecutableRemoveByQuery<T> removeByQuery(Class<T> domainType);

	interface TerminatingRemoveByQuery<T> {

		List<RemoveResult> all();

	}

	interface RemoveByQueryWithQuery<T> extends TerminatingRemoveByQuery<T> {

		TerminatingRemoveByQuery<T> matching(Query query);

	}

	interface RemoveByQueryConsistentWith<T> extends RemoveByQueryWithQuery<T> {

		RemoveByQueryWithQuery<T> consistentWith(QueryScanConsistency scanConsistency);

	}

	interface RemoveByQueryInCollection<T> extends RemoveByQueryConsistentWith<T> {

		RemoveByQueryConsistentWith<T> inCollection(String collection);

	}

	interface ExecutableRemoveByQuery<T> extends RemoveByQueryInCollection<T> {}

}