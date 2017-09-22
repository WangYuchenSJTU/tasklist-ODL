# Task List Web UI
A JSP web UI consuming the RESTful ODL service.
Note: In order to get full funtionality, pleas use browsers that support HTML5 (ex: Chrome)
The UI should be look like this with HTML5 supported:
![Image](https://github.com/WangYuchenSJTU/tasklist-ODL/blob/master/jspui/uisample.png)

## Build
```bash
cd jspui/
mvn clean install -DskipTests
```

## Install
Copy the folder in your user home folder `.m2/repository/net/yuchen/jspui` into  `tasklist/karaf/target/assembly/system/net/yuchen/jspui`
Then install the bundle in ODL's karaf:
```bash
feature:install war
install -s mvn:net.yuchen/jspui/1.0-SNAPSHOT/war
```

## Test
Run and test the web UI at: http://localhost:8181/jsp/index.jsp

