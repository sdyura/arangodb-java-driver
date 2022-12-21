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

package com.arangodb.protocol.vst;

import com.arangodb.commons.ArangoDBException;
import com.arangodb.protocol.internal.net.ArangoDBRedirectException;
import com.arangodb.protocol.internal.net.HostDescription;
import com.arangodb.protocol.internal.net.HostHandle;
import com.arangodb.protocol.internal.net.HostHandler;
import com.arangodb.serde.InternalSerde;
import com.arangodb.protocol.internal.HostUtils;
import com.arangodb.protocol.vst.internal.AuthenticationRequest;
import com.arangodb.protocol.vst.internal.JwtAuthenticationRequest;
import com.arangodb.protocol.vst.internal.Message;
import com.arangodb.protocol.vst.internal.VstConnectionSync;
import com.arangodb.velocypack.exception.VPackParserException;
import com.arangodb.protocol.internal.InternalRequest;
import com.arangodb.protocol.internal.InternalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;

/**
 * @author Mark Vollmary
 */
public class VstCommunicationSync extends VstCommunication<InternalResponse, VstConnectionSync> {

    private static final Logger LOGGER = LoggerFactory.getLogger(VstCommunicationSync.class);

    protected VstCommunicationSync(final HostHandler hostHandler, final Integer timeout, final String user,
                                   final String password, final String jwt, final Boolean useSsl,
                                   final SSLContext sslContext, final InternalSerde util,
                                   final Integer chunksize, final Integer maxConnections, final Long ttl) {
        super(timeout, user, password, jwt, useSsl, sslContext, util, chunksize, hostHandler);
    }

    @Override
    protected InternalResponse execute(final InternalRequest request, final VstConnectionSync connection) {
        return execute(request, connection, 0);
    }

    @Override
    protected InternalResponse execute(final InternalRequest request, final VstConnectionSync connection, final int attemptCount) {
        try {
            final Message requestMessage = createMessage(request);
            if (LOGGER.isDebugEnabled()) {
                String body = request.getBody() == null ? "" : util.toJsonString(request.getBody());
                LOGGER.debug("Send Request [id={}]: {} {}", requestMessage.getId(), request, body);
            }
            final Message responseMessage = send(requestMessage, connection);
            final InternalResponse response = createResponse(responseMessage);
            if (LOGGER.isDebugEnabled()) {
                String body = response.getBody() == null ? "" : util.toJsonString(response.getBody());
                LOGGER.debug("Received Response [id={}]: {} {}", responseMessage.getId(), response, body);
            }
            checkError(response);
            return response;
        } catch (final VPackParserException e) {
            throw new ArangoDBException(e);
        } catch (final ArangoDBRedirectException e) {
            if (attemptCount >= 3) {
                throw e;
            }
            final String location = e.getLocation();
            final HostDescription redirectHost = HostUtils.createFromLocation(location);
            hostHandler.failIfNotMatch(redirectHost, e);
            return execute(request, new HostHandle().setHost(redirectHost), attemptCount + 1);
        }
    }

    private Message send(final Message message, final VstConnectionSync connection) {
        return connection.write(message, buildChunks(message));
    }

    @Override
    protected void authenticate(final VstConnectionSync connection) {
        InternalRequest authRequest;
        if (jwt != null) {
            authRequest = new JwtAuthenticationRequest(jwt, ENCRYPTION_JWT);
        } else {
            authRequest = new AuthenticationRequest(user, password != null ? password : "", ENCRYPTION_PLAIN);
        }
        final InternalResponse response = execute(authRequest, connection);
        checkError(response);
    }

    public static class Builder {

        private final HostHandler hostHandler;
        private Integer timeout;
        private Long connectionTtl;
        private String user;
        private String password;
        private String jwt;
        private Boolean useSsl;
        private SSLContext sslContext;
        private Integer chunksize;
        private Integer maxConnections;

        public Builder(final HostHandler hostHandler) {
            super();
            this.hostHandler = hostHandler;
        }

        public Builder(final Builder builder) {
            this(builder.hostHandler);
            timeout(builder.timeout).user(builder.user).password(builder.password).jwt(builder.jwt)
                    .useSsl(builder.useSsl).sslContext(builder.sslContext).chunksize(builder.chunksize)
                    .maxConnections(builder.maxConnections);
        }

        public Builder timeout(final Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder user(final String user) {
            this.user = user;
            return this;
        }

        public Builder password(final String password) {
            this.password = password;
            return this;
        }

        public Builder jwt(final String jwt) {
            this.jwt = jwt;
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

        public Builder chunksize(final Integer chunksize) {
            this.chunksize = chunksize;
            return this;
        }

        public Builder maxConnections(final Integer maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public Builder connectionTtl(final Long connectionTtl) {
            this.connectionTtl = connectionTtl;
            return this;
        }

        public VstCommunication<InternalResponse, VstConnectionSync> build(final InternalSerde util) {
            return new VstCommunicationSync(hostHandler, timeout, user, password, jwt, useSsl, sslContext, util,
                    chunksize,
                    maxConnections, connectionTtl);
        }

    }

}
