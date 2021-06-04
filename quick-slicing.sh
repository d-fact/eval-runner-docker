#!/usr/bin/env sh
docker-compose -f docker-compose.yml build slicing-driver
docker-compose up slicing-driver
cd /var/lib/docker/volumes/diffbase_datavol/_data || exit
echo "check ./resources/file-level/output/facts for generated facts"
echo "check ./grok_run/grok_results for slicing results"
