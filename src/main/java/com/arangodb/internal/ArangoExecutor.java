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

package com.arangodb.internal;

import com.arangodb.QueueTimeMetrics;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.Response;

import java.lang.reflect.Type;

/**
 * @author Mark Vollmary
 */
public abstract class ArangoExecutor {

    protected <T> T createResult(final Type type, final Response response) {
        return serde.deserialize(response.getBody(), type);
    }

    private final QueueTimeMetricsImpl qtMetrics;
    private final InternalSerde serde;
    private final String timeoutS;

    protected ArangoExecutor(final InternalSerde serde, final QueueTimeMetricsImpl qtMetrics, final int timeoutMs) {
        super();
        this.qtMetrics = qtMetrics;
        this.serde = serde;
        timeoutS = timeoutMs >= 1000 ? Integer.toString(timeoutMs / 1000) : null;
    }

    public interface ResponseDeserializer<T> {
        T deserialize(Response response);
    }

    protected final void interceptResponse(Response response) {
        String queueTime = response.getMeta().get("X-Arango-Queue-Time-Seconds");
        if (queueTime != null) {
            qtMetrics.add(Double.parseDouble(queueTime));
        }
    }

    protected final Request interceptRequest(Request request) {
        request.putHeaderParam("X-Arango-Queue-Time-Seconds", timeoutS);
        return request;
    }

    public QueueTimeMetrics getQueueTimeMetrics() {
        return qtMetrics;
    }
}
