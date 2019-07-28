#!/bin/bash

mvn -B -DignoreSnapshots=true release:prepare release:perform -Darguments="-Dmaven.deploy.skip=true"
