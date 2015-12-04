sml-token-service
========

This project contains sml-token-service used by DVLA.

## Requirements

 * Java JDK 1.7+
 * Maven 3

## Build

To build this project simply execute the following Maven goals:

```bash
  mvn clean install
```

## Run

To run the jar file use the following command in your terminal from the sml-token-service folder (making sure Mongo is running on your system):

```bash
java -jar target/sml-token-service-[SML-VERSION].jar server target/classes/config.yaml
```
