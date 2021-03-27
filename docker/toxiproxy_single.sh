#!/bin/bash

## proxy for start_db_single.sh exposed at 172.28.3.100:5555
docker run -d --network arangodb --ip 172.28.3.100 --name toxi shopify/toxiproxy
docker exec toxi /go/bin/toxiproxy-cli create arangodb -l [::]:5555 -u 172.28.3.1:8529

## reduce the connection bandwidth to 0
# docker exec -it toxi /go/bin/toxiproxy-cli toxic add arangodb --toxicName block --type bandwidth --attribute rate=0

## restore to normal
# docker exec -it toxi /go/bin/toxiproxy-cli toxic remove arangodb --toxicName block

## add delay
# docker exec -it toxi /go/bin/toxiproxy-cli toxic add arangodb --toxicName delay --type latency --attribute latency=100000

## restore to normal
# docker exec -it toxi /go/bin/toxiproxy-cli toxic remove arangodb --toxicName delay
