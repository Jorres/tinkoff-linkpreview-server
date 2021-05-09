#!/bin/zsh

if [ "$#" -ne 1 ]; then 
    echo "Please enter exactly one argument specifying the tag"
    exit 1
fi

echo "Name: ${1}" 

docker build -t jorres/tinkoff-linkpreview:"${1}" .
docker push jorres/tinkoff-linkpreview:"${1}"
