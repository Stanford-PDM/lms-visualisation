#!/usr/bin/env bash
set -x

find . -name '*.scala' | xargs -t -n 1 -J %% scalafmt --style "Scala.js" --maxColumn 100 --in-place --files %%
