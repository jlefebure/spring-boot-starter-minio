#!/bin/bash

mvn release:prepare release:perform -Darguments="-Dmaven.deploy.skip=true"
