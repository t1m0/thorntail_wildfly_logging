# Thorntail Logging
Simple project to show how log4j can be used with thorntail.
Execute below command to launch thorntail with log4j.properties beeing applied.
```
mvn thorntail:run -pl thorntail-webapp
```

Execute below command to launch thorntail with log4j_json.properties beeing applied.
```
mvn thorntail:run -pl thorntail-webapp -Dlog4j.configuration=log4j_json.properties
```

Execute below command to trigger some logging
```
curl localhost:8080/time/now
```

Currently the issue is, that log4j_json.properties are not applied.
