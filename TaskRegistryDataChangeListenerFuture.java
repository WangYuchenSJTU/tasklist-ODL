/*
 * Copyright © 2015 Ruan HE and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.tasklist.impl;

import com.google.common.util.concurrent.AbstractFuture;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.tasklist.rev150105.TaskRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.tasklist.rev150105.task.registry.TaskRegistryEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.tasklist.rev150105.task.registry.TaskRegistryEntryKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskRegistryDataChangeListenerFuture extends AbstractFuture<TaskRegistryEntry> implements DataChangeListener, AutoCloseable{

	private static final Logger LOG = LoggerFactory.getLogger(TaskRegistryDataChangeListenerFuture.class);

	private String name;
	private ListenerRegistration<DataChangeListener> registration;


	public TaskRegistryDataChangeListenerFuture(DataBroker db,String name) {
		super();
		this.name = name;

		InstanceIdentifier<TaskRegistryEntry> iid =
			InstanceIdentifier.create(TaskRegistry.class).child(TaskRegistryEntry.class);
		this.registration = db.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, iid, this, DataChangeScope.BASE);
	}

	@Override
	public void close() throws Exception {
		if(registration != null) {
		  registration.close();
		}
	}

	@Override
	public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> event) {
		//LOG.info("Data has changed !");
		/** task-registery/task-registery-entry/name **/
		InstanceIdentifier<TaskRegistryEntry> iid =
				InstanceIdentifier.create(TaskRegistry.class)
				.child(TaskRegistryEntry.class,new TaskRegistryEntryKey(this.name));

		/** Task if the returned set of apth contains iid **/
		if(event.getCreatedData().containsKey(iid)) {
			/** Task if the node pointed by the path is TaskRegistryEntry **/
			if(event.getCreatedData().get(iid) instanceof TaskRegistryEntry) {
				this.set((TaskRegistryEntry) event.getCreatedData().get(iid));
				LOG.info("TaskRegistry tree has been changed");
				LOG.info("New entry {} ", event.toString());
				System.out.println("onData created");
			}
			quietClose();
		} else if(event.getUpdatedData().containsKey(iid)) {
			if(event.getUpdatedData().get(iid) instanceof TaskRegistryEntry) {
				this.set((TaskRegistryEntry) event.getUpdatedData().get(iid));
				LOG.info("TaskRegistry tree has been changed");
				LOG.info("New entry {} ", event.toString());
				System.out.println("onData updated");
			}
			quietClose();
		}
	}

	private void quietClose() {
		try {
			this.close();
		} catch (Exception e) {
			throw new IllegalStateException("Unable to close registration",e);
		}
	}
}
