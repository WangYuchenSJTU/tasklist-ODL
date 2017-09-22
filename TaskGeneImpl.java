/*
 * Copyright © 2017 Yuchen Wang. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.tasklist.impl;

import java.util.concurrent.Future;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.tasklist.rev150105.TasklistService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.tasklist.rev150105.TaskGeneInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.tasklist.rev150105.TaskGeneOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.tasklist.rev150105.TaskGeneOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.tasklist.rev150105.TaskRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.tasklist.rev150105.TaskRegistryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.tasklist.rev150105.task.registry.TaskRegistryEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.tasklist.rev150105.task.registry.TaskRegistryEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.tasklist.rev150105.task.registry.TaskRegistryEntryKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;

public class TaskGeneImpl implements TasklistService{
    private DataBroker db;
    private static Logger LOG = LoggerFactory.getLogger(TasklistProvider.class);

    public TaskGeneImpl(DataBroker db) {
        this.db = db;
        initializeDataTree(this.db);
    }
    @Override
    public Future<RpcResult<TaskGeneOutput>> taskGene(TaskGeneInput input) {
	LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	String startpoint = now.format(formatter);
        TaskGeneOutput output = new TaskGeneOutputBuilder()
                .setStartpoint(startpoint)
                .build();

	writeToTaskRegistry(input, output);

        return RpcResultBuilder.success(output).buildFuture();
    }

    private void initializeDataTree(DataBroker db) {
        LOG.info("Preparing to initialize the task registry");

        /** Allocates new write-only transaction based on latest state of data tree **/
        WriteTransaction transaction = db.newWriteOnlyTransaction();

        /** /task-registery **/
        InstanceIdentifier<TaskRegistry> iid = InstanceIdentifier.create(TaskRegistry.class);
        /** {} **/
        TaskRegistry taskRegistry = new TaskRegistryBuilder().build();

        /** Stores a piece of data at the specified path **/
        /** /task-registery{} **/
        transaction.put(LogicalDatastoreType.OPERATIONAL, iid, taskRegistry);

        /** Submits this transaction to be asynchronously applied to update the logical data tree **/
        CheckedFuture<Void, TransactionCommitFailedException> future = transaction.submit();
        Futures.addCallback(future, new LoggingFuturesCallBack<>("Failed to create task registry", LOG));
    }

    private void writeToTaskRegistry(TaskGeneInput input, TaskGeneOutput output) {
        WriteTransaction transaction = db.newWriteOnlyTransaction();

        InstanceIdentifier<TaskRegistryEntry> iid = toInstanceIdentifier(input);

        /** {input,output} **/
        TaskRegistryEntry task = new TaskRegistryEntryBuilder()
                .setStartpoint(output.getStartpoint())
                .setName(input.getName())
		.setDeadline(input.getDeadline())
                .build();
        /** task-registery/task-registery-entry/input{input,output} **/
        transaction.put(LogicalDatastoreType.OPERATIONAL, iid, task);

        CheckedFuture<Void, TransactionCommitFailedException> future = transaction.submit();
        Futures.addCallback(future, new LoggingFuturesCallBack<Void>("Failed to write task to task registry", LOG));
    }

    private InstanceIdentifier<TaskRegistryEntry> toInstanceIdentifier(TaskGeneInput input) {
        /** task-registery/task-registery-entry/input **/
        InstanceIdentifier<TaskRegistryEntry> iid = InstanceIdentifier.create(TaskRegistry.class)
                .child(TaskRegistryEntry.class, new TaskRegistryEntryKey(input.getName()));

        return iid;
    }

}
