#!/bin/sh
mvn clean package && docker build -t de.incentergy.base.rte/base-rte .
docker rm -f base-rte || true && docker run -d -p 8080:8080 -p 4848:4848 --name base-rte de.incentergy.base.rte/base-rte 
