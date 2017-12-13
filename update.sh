#!/bin/bash

cd "$(dirname "$(realpath "$0")")";

GH_TOKEN=${1:-${GH_TOKEN}}

docker pull mariolet/stats4osio
docker run --rm mariolet/stats4osio ${GH_TOKEN} > index.html

git add index.html
git commit -m "Automatic update of index.html"
git push origin gh-pages
