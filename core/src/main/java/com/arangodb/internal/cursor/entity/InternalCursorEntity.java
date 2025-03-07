/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.internal.cursor.entity;

import com.arangodb.entity.CursorStats;
import com.arangodb.entity.CursorWarning;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Mark Vollmary
 * @see <a href="https://www.arangodb.com/docs/stable/http/aql-query-cursor-accessing-cursors.html#create-cursor">API
 * Documentation</a>
 */
public final class InternalCursorEntity {

    private final Extras extra = new Extras();
    private String id;
    private Integer count;
    private Boolean cached;
    private Boolean hasMore;
    private JsonNode result;
    private Boolean pontentialDirtyRead;
    private String nextBatchId;

    public String getId() {
        return id;
    }

    /**
     * @return the total number of result documents available (only available if the query was executed with the count
     * attribute set)
     */
    public Integer getCount() {
        return count;
    }

    /**
     * @return an optional object with extra information about the query result contained in its stats sub-attribute.
     * For data-modification queries, the extra.stats sub-attribute will contain the number of modified
     * documents and the number of documents that could not be modified due to an error (if ignoreErrors query
     * option is specified)
     */
    public Extras getExtra() {
        return extra;
    }

    /**
     * @return a boolean flag indicating whether the query result was served from the query cache or not. If the query
     * result is served from the query cache, the extra return attribute will not contain any stats
     * sub-attribute and no profile sub-attribute.
     */
    public Boolean getCached() {
        return cached;
    }

    /**
     * @return A boolean indicator whether there are more results available for the cursor on the server
     */
    public Boolean getHasMore() {
        return hasMore;
    }

    /**
     * @return an array of result documents (might be empty if query has no results)
     */
    public JsonNode getResult() {
        return result;
    }

    /**
     * @return true if the result is a potential dirty read
     * @since ArangoDB 3.10
     */
    public Boolean isPontentialDirtyRead() {
        return pontentialDirtyRead;
    }

    public void setPontentialDirtyRead(final Boolean pontentialDirtyRead) {
        this.pontentialDirtyRead = pontentialDirtyRead;
    }

    /**
     * @return The ID of the batch after the current one. The first batch has an ID of 1 and the value is incremented by
     * 1 with every batch. Only set if the allowRetry query option is enabled.
     * @since ArangoDB 3.11
     */
    public String getNextBatchId() {
        return nextBatchId;
    }

    public static final class Extras {
        private final Collection<CursorWarning> warnings = Collections.emptyList();
        private CursorStats stats;

        public CursorStats getStats() {
            return stats;
        }

        public Collection<CursorWarning> getWarnings() {
            return warnings;
        }

    }

}

