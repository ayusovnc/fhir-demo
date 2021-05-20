#!/bin/sh
gzip -dc pids.100k.gz | sed '1d' | shuf -n 100 | while read n; do
    tm=`curl -s -w '%{time_connect}\t%{time_starttransfer}\t%{time_total}\n' -o /dev/zero http://localhost:8080/fhir/Patient/${n}`
    echo ${n} ${tm}
done
