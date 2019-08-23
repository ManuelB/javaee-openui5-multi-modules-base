#!/bin/sh
mvn clean package && docker build -t de.incentergy/base-backend .
docker rm -f base-backend || true && docker run -d -p 8080:8080 -p 4848:4848 --name base-backend de.incentergy/base-backend 
