#!/bin/sh

# Make
./scripts/make.sh

# Copy to cluster
scp $HOME/.bds/bds $HOME/.bds/BigDataScript.jar eq8302@ehs.grid.wayne.edu:.bds/

