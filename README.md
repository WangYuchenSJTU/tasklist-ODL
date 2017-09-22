# Task List App (ODL)
A simple ODL example that produce RESTful task list service on MD-SAL. It consists of four main features: rpc, data container, date change listener and a JSP web UI consuming the service.

## Project Creation
- create the `tasklist` project:
```bash
Snapshot-Type=opendaylight.release 
Archetype-Version=1.3.0-Carbon
wget https://nexus.opendaylight.org/content/repositories/opendaylight.release/archetype-catalog.xml
mv archetype-catalog.xml ~/.m2/
mvn archetype:generate -DarchetypeGroupId=org.opendaylight.controller \
    -DarchetypeArtifactId=opendaylight-startup-archetype \
    -DarchetypeVersion=1.3.0-Carbon \
    -DarchetypeRepository=http://nexus.opendaylight.org/content/repositories/opendaylight.release/ \
    -DarchetypeCatalog=remote
```
- respond to the prompts as follows:
```bash
Define value for property 'groupId': : org.opendaylight.tasklist
Define value for property 'artifactId': : tasklist
Define value for property 'package':  org.opendaylight.tasklist: : 
Define value for property 'classPrefix':  tasklist: : 

## YANG Modeling
- model *Tasklist RPC* in the file `task/api/src/main/yang/tasklist.yang`: [tasklist.yang](tasklist.yang)
- compile the YANG model
```bash
cd task/api
mvn clean install -DskipTests
```

## Implementation
- add the RPC Registry reference to `impl-blueprint.xml` in `tasklist/impl/src/main/resources/org/opendaylight/blueprint`: [impl-blueprint.xml](impl-blueprint.xml)
-  modify the code in the file: `tasklist/impl/src/main/java/org/opendaylight/tasklist/impl/TasklistProvider.java`:[TasklistProvider.java](TasklistProvider.java)
- create the `tasklist/impl/src/main/java/org/opendaylight/tasklist/impl/TaskGeneImpl.java` file: [TaskGeneImpl.java](TaskGeneImpl.java)
-create the: `tasklist/impl/src/main/java/org/opendaylight/tasklist/impl/LoggingFuturesCallBack.java` file as [LoggingFuturesCallBack.java](LoggingFuturesCallBack.java) 
- create the `tasklist/impl/src/main/java/org/opendaylight/tasklist/impl/TaskRegistryDataChangeListenerFuture.java` file as [TaskRegistryDataChangeListenerFuture.java](TaskRegistryDataChangeListenerFuture.java)

## Compilation
- compile the project 
```bash
cd task/
mvn clean install -DskipTests
```

## Test
- launch karaf `task/karaf/target/assembly/bin/karaf`
- install feature: `feature:install odl-dluxapps-applications`
- access the page: `http://localhost:8181/index.html` (login: admin, pwd: admin)
- use `YangGUI/tasklist/operations/task-gene` to add new entry
- use `YangGUI/tasklist/operational/task-registry` to get stored entry

