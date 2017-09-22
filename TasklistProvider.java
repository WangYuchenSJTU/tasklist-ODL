/*
 * Copyright © 2017 Yuchen Wang and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.tasklist.impl;

import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.tasklist.rev150105.TasklistService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;

public class TasklistProvider {

    private static final Logger LOG = LoggerFactory.getLogger(TasklistProvider.class);

    private final DataBroker dataBroker;
    private RpcProviderRegistry rpcProviderRegistry;
    private RpcRegistration serviceRegistration;

    public TasklistProvider(final DataBroker dataBroker, RpcProviderRegistry rpcProviderRegistry) {
        this.dataBroker = dataBroker;
        this.rpcProviderRegistry = rpcProviderRegistry;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        serviceRegistration = rpcProviderRegistry.addRpcImplementation(TasklistService.class, new TaskGeneImpl(dataBroker));

        LOG.info("Yuchen TasklistProvider Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        serviceRegistration.close();
        LOG.info("TasklistProvider Closed");
    }
}
