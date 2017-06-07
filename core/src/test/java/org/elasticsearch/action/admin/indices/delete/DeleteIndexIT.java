/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.action.admin.indices.delete;

import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.test.ESIntegTestCase;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;

public class DeleteIndexIT extends ESIntegTestCase {

    public void testDeleteIndexAccpetsOnlyIndices() {
        assertAcked(prepareCreate("foo_foo").addAlias(new Alias("foo")));
        assertAcked(prepareCreate("bar_bar").addAlias(new Alias("foo")));
        ensureGreen();

        // if a single non-existing index is passed to DELETE, DELETE is a no-op
        assertAcked(admin().indices().delete(new DeleteIndexRequest("foo")).actionGet());
        assertTrue(client().admin().indices().exists(new IndicesExistsRequest("foo_foo")).actionGet().isExists());
        assertTrue(client().admin().indices().exists(new IndicesExistsRequest("bar_bar")).actionGet().isExists());

        IndexNotFoundException infe = expectThrows(IndexNotFoundException.class,
                () -> admin().indices().delete(new DeleteIndexRequest("foo", "bar_bar")).actionGet());
        assertEquals("foo", infe.getIndex().getName());
        assertTrue(client().admin().indices().exists(new IndicesExistsRequest("foo_foo")).actionGet().isExists());
        assertTrue(client().admin().indices().exists(new IndicesExistsRequest("bar_bar")).actionGet().isExists());

        assertAcked(internalCluster().coordOnlyNodeClient().admin().indices().delete(new DeleteIndexRequest("foo*")).actionGet());
        assertFalse(client().admin().indices().exists(new IndicesExistsRequest("foo_foo")).actionGet().isExists());
        assertTrue(client().admin().indices().exists(new IndicesExistsRequest("bar_bar")).actionGet().isExists());
    }
}
