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

package com.arangodb.http;

import com.arangodb.internal.net.CommunicationProtocol;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.internal.InternalRequest;
import com.arangodb.internal.InternalResponse;

import java.io.IOException;

/**
 * @author Mark Vollmary
 */
public class HttpProtocol implements CommunicationProtocol {

    private final HttpCommunication httpCommunitaction;

    public HttpProtocol(final HttpCommunication httpCommunitaction) {
        super();
        this.httpCommunitaction = httpCommunitaction;
    }

    @Override
    public InternalResponse execute(final InternalRequest request, final HostHandle hostHandle) {
        return httpCommunitaction.execute(request, hostHandle);
    }

    @Override
    public void setJwt(String jwt) {
        // no-op: jwt is updated in the host handlers
    }

    @Override
    public void close() throws IOException {
        httpCommunitaction.close();
    }

}
