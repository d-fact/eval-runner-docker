#!/usr/bin/env sh
docker-compose -f docker-compose.yml build rts-driver
docker-compose up rts-driver
DATAVOL_LOC=/var/lib/docker/volumes/diffbase_datavol/_data
echo "check $DATAVOL_LOC/run_grok/grok_results/Lang-28.affected for selected test classes"
