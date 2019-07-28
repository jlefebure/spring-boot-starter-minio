#!/bin/bash

mvn -B release:prepare release:perform -Darguments="-Dmaven.deploy.skip=true"
