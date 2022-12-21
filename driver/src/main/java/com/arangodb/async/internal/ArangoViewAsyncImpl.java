/*
 * DISCLAIMER
 *
 * Copyright 2018 ArangoDB GmbH, Cologne, Germany
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

package com.arangodb.async.internal;

import com.arangodb.async.ArangoViewAsync;
import com.arangodb.entity.ViewEntity;
import com.arangodb.internal.InternalArangoView;
import com.arangodb.protocol.internal.ArangoExecutorAsync;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author Mark Vollmary
 */
public class ArangoViewAsyncImpl extends
        InternalArangoView<ArangoDBAsyncImpl, ArangoDatabaseAsyncImpl, ArangoExecutorAsync> implements ArangoViewAsync {

    ArangoViewAsyncImpl(final ArangoDatabaseAsyncImpl db, final String name) {
        super(db, name);
    }

    @Override
    public CompletableFuture<Boolean> exists() {
        return getInfo().thenApply(Objects::nonNull).exceptionally(Objects::isNull);
    }

    @Override
    public CompletableFuture<Void> drop() {
        return executor.execute(dropRequest(), Void.class);
    }

    @Override
    public synchronized CompletableFuture<ViewEntity> rename(final String newName) {
        return executor.execute(renameRequest(newName), ViewEntity.class);
    }

    @Override
    public CompletableFuture<ViewEntity> getInfo() {
        return executor.execute(getInfoRequest(), ViewEntity.class);
    }

}
