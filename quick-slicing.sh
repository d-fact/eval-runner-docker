#!/usr/bin/env sh
docker-compose -f docker-compose.yml build slicing-driver
docker-compose up slicing-driver
DATAVOL_LOC=/var/lib/docker/volumes/diffbase_datavol/_data
echo "check $DATAVOL_LOC/resources/file-level/output/facts for generated facts"
echo "check $DATAVOL_LOC/grok_run/grok_results for slicing results"
