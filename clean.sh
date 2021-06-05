#!/usr/bin/env sh
echo "Will remove built containers and destroy data volumes."
docker container rm facts-rts
docker container rm facts-slicing
docker volume rm diffbase_datavol
