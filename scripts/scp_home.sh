#!/bin/sh

# Make
./scripts/make.sh

# Copy to cluster
scp -P 2222 $HOME/.bds/bds $HOME/.bds/BigDataScript.jar localhost:.bds/
