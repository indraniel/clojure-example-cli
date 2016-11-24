(defproject clojure-example-cli "0.1.0-SNAPSHOT"
  :description "My awesome example CLI program"
  :url "http://github.com/indraniel/clojure-example-cli"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [trptcolin/versioneer "0.2.0"]]
  :uberjar-name "app-standalone.jar"
  :aot [clojure-example-cli.core]
  :main clojure-example-cli.core)
