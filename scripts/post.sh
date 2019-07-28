#!/bin/sh

PDPURL=http://localhost:8080/pdp/

curl -v --header "Content-Type:application/json" -d @$1 $PDPURL | python -m json.tool
