#!/usr/bin/env sh
docker-compose -f docker-compose.yml build rts-driver
docker-compose up rts-driver
cd /var/lib/docker/volumes/diffbase_datavol/_data || exit
echo "check ./run_grok/grok_results/Lang-28.affected for selected test classes"
