#!/usr/bin/env bash

find . -name '*.scala' | xargs -n 1 -J %% scalafmt --style "Scala.js" --in-place --files %%
