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

package com.arangodb.protocol.vst.internal;

import com.arangodb.commons.ArangoDBException;
import com.arangodb.protocol.internal.net.HostDescription;

import javax.net.ssl.SSLContext;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * @author Mark Vollmary
 */
public class VstConnectionSync extends VstConnection<Message> {

    private VstConnectionSync(final HostDescription host, final Integer timeout, final Long ttl,
                              final Integer keepAliveInterval,
                              final Boolean useSsl, final SSLContext sslContext, final MessageStore messageStore) {
        super(host, timeout, ttl, keepAliveInterval, useSsl, sslContext, messageStore);
    }

    @Override
    public Message write(final Message message, final Collection<Chunk> chunks) {
        final FutureTask<Message> task = new FutureTask<>(() -> messageStore.get(message.getId()));
        messageStore.storeMessage(message.getId(), task);
        super.writeIntern(message, chunks);
        try {
            return timeout == null || timeout == 0L ? task.get() : task.get(timeout, TimeUnit.MILLISECONDS);
        } catch (final ExecutionException e) {
            throw ArangoDBException.wrap(e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ArangoDBException(e);
        } catch (final Exception e) {
            throw ArangoDBException.wrap(e);
        }
    }

    @Override
    protected void doKeepAlive() {
        sendKeepAlive();
    }

    public static class Builder {

        private HostDescription host;
        private MessageStore messageStore;
        private Integer timeout;
        private Long ttl;
        private Integer keepAliveInterval;
        private Boolean useSsl;
        private SSLContext sslContext;

        public Builder host(final HostDescription host) {
            this.host = host;
            return this;
        }

        public Builder messageStore(final MessageStore messageStore) {
            this.messageStore = messageStore;
            return this;
        }

        public Builder timeout(final Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder useSsl(final Boolean useSsl) {
            this.useSsl = useSsl;
            return this;
        }

        public Builder sslContext(final SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        public Builder ttl(final Long ttl) {
            this.ttl = ttl;
            return this;
        }

        public Builder keepAliveInterval(final Integer keepAliveInterval) {
            this.keepAliveInterval = keepAliveInterval;
            return this;
        }

        public VstConnectionSync build() {
            return new VstConnectionSync(host, timeout, ttl, keepAliveInterval,
                    useSsl, sslContext, messageStore);
        }
    }

}
