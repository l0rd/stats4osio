# OpenShift.io stats generator

This repository contain an command line application that extracts some stats from Openshift.io 
GitHub repository.

## Classical

- Build app: `mvn clean compile assembly:single`
- Run app: `java -jar target/stats4osio-1.0.0-SNAPSHOT.jar [<github-access-token>]`

## With Docker

- Build image: `docker build -t mariolet/stats4osio .`
- Run container: `docker run --rm mariolet/stats4osio [<github-access-token>]`

