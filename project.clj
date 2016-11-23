(defproject clojure-example-cli "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [clj-sub-command "0.3.0"]]
  :uberjar-name "app-standalone.jar"
  :aot [clojure-example-cli.core]
  :main clojure-example-cli.core)
