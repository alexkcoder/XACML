#!/bin/sh

PDPURL=http://localhost:8080/pdp/

pol=$(cat $1)
curl -X PUT --upload-file $1 $PDPURL
