# clojure-example-cli

An example clojure app template demonstrating a subcommand CLI like [git][0].

## Usage

    git clone https://github.com/indraniel/clojure-example-cli
    cd clojure-example-cli
    lein uberjar

    java -jar target/app-standalone.jar 
    java -jar target/app-standalone.jar --help
    java -jar target/app-standalone.jar cmd1 --help
    java -jar target/app-standalone.jar cmd2 --help
    java -jar target/app-standalone.jar cmd1 --verbose
    java -jar target/app-standalone.jar cmd2 --file foo baz
    java -jar target/app-standalone.jar cmd2 baz

## License

Copyright © 2016 Indraniel Das

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[0]: https://git-scm.com
