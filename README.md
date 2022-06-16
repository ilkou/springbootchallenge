# SpringBoot Challenge

## To-Add

* [x] configure gradle for sonarqube (better code quality / overview of tests coverage)
* [x] Build a docker image with JIB
* [ ] Logging with (EFK Stack):
  * [x] `Fluentd`
  * [ ] `Elasticsearch`
  * [ ] `Kibana` 
* [ ] Monitoring with `Actuator`, `Prometheus`, `Grafana`
* [ ] ...

## Build a docker image

In order to build a docker image for the app and push it to the registry, I've used `JIB` from google.

we start by including the plugin:

  ```
  plugins {
    ...
    id 'com.google.cloud.tools.jib' version '3.1.4'
    ...
  }
  ```

then configure it:

  ```gradle
  jib {
    allowInsecureRegistries = true
    from {
      image = 'openjdk:8-jdk-oraclelinux7'
    }
    to {
      tags = ['latest', project.version]
      image = 'server-ip-addr:server-port/cirestech'
      auth {
        username = ''
        password = ''
      }
    }
  }
  ```

and we can modify some values like credentials by passing them as flags when building the image:

  ```sh
  ./gradlew jib -Djib.to.image=server-ip-addr:server-port/cirestech -Djib.to.auth.username=username -Djib.to.auth.password=password
  ```

## Tests Coverage (intellij)

![alt text](https://github.com/ilkou/springbootchallenge/blob/main/images/coverage.png "TESTS COVERAGE")

### [Swagger endpoint](http://localhost:9090/swagger-ui/index.html)
