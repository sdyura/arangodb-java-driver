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

package com.arangodb.model;

import com.arangodb.internal.serde.UserData;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 * @see <a href="https://www.arangodb.com/docs/stable/http/transaction-js-transaction.html#execute-transaction">API
 * Documentation</a>
 */
public final class TransactionOptions {

    private final TransactionCollectionOptions collections;
    private String action;
    private Object params;
    private Integer lockTimeout;
    private Boolean waitForSync;
    private Long maxTransactionSize;

    public TransactionOptions() {
        super();
        collections = new TransactionCollectionOptions();
    }

    public TransactionCollectionOptions getCollections() {
        return collections;
    }

    public String getAction() {
        return action;
    }

    /**
     * @param action the actual transaction operations to be executed, in the form of stringified JavaScript code
     * @return options
     */
    TransactionOptions action(final String action) {
        this.action = action;
        return this;
    }

    @UserData
    public Object getParams() {
        return params;
    }

    /**
     * @param params optional arguments passed to action
     * @return options
     */
    public TransactionOptions params(final Object params) {
        this.params = params;
        return this;
    }

    public Integer getLockTimeout() {
        return lockTimeout;
    }

    /**
     * @param lockTimeout a numeric value that can be used to set a timeout in seconds for
     *                    waiting on collection locks. This option is only meaningful when using
     *                    exclusive locks. If not specified, a default value of 900 seconds will be
     *                    used. Setting lockTimeout to 0 will make ArangoDB not time out
     *                    waiting for a lock.
     * @return options
     */
    public TransactionOptions lockTimeout(final Integer lockTimeout) {
        this.lockTimeout = lockTimeout;
        return this;
    }

    public Boolean getWaitForSync() {
        return waitForSync;
    }

    /**
     * @param waitForSync an optional boolean flag that, if set, will force the transaction to write all data to disk
     *                    before
     *                    returning
     * @return options
     */
    public TransactionOptions waitForSync(final Boolean waitForSync) {
        this.waitForSync = waitForSync;
        return this;
    }

    /**
     * @param read contains the array of collection-names to be used in the transaction (mandatory) for read
     * @return options
     */
    public TransactionOptions readCollections(final String... read) {
        collections.read(read);
        return this;
    }

    /**
     * @param write contains the array of collection-names to be used in the transaction (mandatory) for write
     * @return options
     */
    public TransactionOptions writeCollections(final String... write) {
        collections.write(write);
        return this;
    }

    /**
     * @param exclusive contains the array of collection-names to be used in the transaction (mandatory) for
     *                  exclusive write
     * @return options
     * @since ArangoDB 3.4.0
     */
    public TransactionOptions exclusiveCollections(final String... exclusive) {
        collections.exclusive(exclusive);
        return this;
    }

    /**
     * @param allowImplicit Collections that will be written to in the transaction must be declared with the write
     *                      attribute or it
     *                      will fail, whereas non-declared collections from which is solely read will be added
     *                      lazily. The
     *                      optional attribute allowImplicit can be set to false to let transactions fail in case of
     *                      undeclared
     *                      collections for reading. Collections for reading should be fully declared if possible, to
     *                      avoid
     *                      deadlocks.
     * @return options
     */
    public TransactionOptions allowImplicit(final Boolean allowImplicit) {
        collections.allowImplicit(allowImplicit);
        return this;
    }

    public Long getMaxTransactionSize() {
        return maxTransactionSize;
    }

    /**
     * @param maxTransactionSize Transaction size limit in bytes. Honored by the RocksDB storage engine only.
     * @return options
     * @since ArangoDB 3.2.0
     */
    public TransactionOptions maxTransactionSize(final Long maxTransactionSize) {
        this.maxTransactionSize = maxTransactionSize;
        return this;
    }

}
