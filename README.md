# tgm_ai

Tweegeemee AI Image Creation

## Usage

You'll need to create an AI model that can tell you if a picture is good or not.
Those models are not included with this code.

In parallel:

  # Run Python AI server using the AI models
  > python3 ai/ai.py  ai/*.pkl
  # Run Clojure tweegeemee generator
  > lein run -- -r 10 -t 100 -o images/foo

## Options

When running with lein, use "--" prior to any of these

  -h, --help                            Print this help
  -r, --num-random-images R  5          Number of initial random images to create
  -t, --num-total-images T   10         Number of total images to create
  -o, --output-path PATH     images/00  Directory to store the images

## License

Copyright © 2020 Roger Allen

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

