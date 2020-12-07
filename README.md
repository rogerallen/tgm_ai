# tgm_ai

Tweegeemee AI Image Creation

## Usage

In parallel:

  # Run Python AI server
  > python3 ai/ai.py  ai/*.pkl
  # Run Clojure tweegeemee generator
  > lein run -- -r 10 -t 100 -o "images/foo"

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

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
