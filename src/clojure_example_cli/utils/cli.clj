(ns clojure-example-cli.utils.cli
  (:require [trptcolin.versioneer.core :as version]
            [clojure.string :as string]))

(defn missing-required-opts?
  [required-opts opts]
  (if (empty? required-opts)
    false
    (not-every? opts required-opts)))

(defn generate-subcommand-usage-string 
  [subcmds subcmd-docs]
  (let [ printer (fn [x y] (format "    %-10s %-50s" (name x) y))]
    (->> (map printer subcmds subcmd-docs)
         (string/join "\n"))))

(defn cmd-usage [usage-string options-summary]
  (->> [usage-string
        ""
        "Options:"
        options-summary]
       (string/join "\n")))

(defn root-usage [prg-name short-description options-summary subcmd-summary]
  (->> [(string/join " " [short-description "-"
                          (version/get-version "clojure-example-cli" "clojure-example-cli")]) 
        ""
        (str "Usage: " prg-name " [subcommand] [options]")
        "Options:"
        options-summary
        ""
        "Subcommands:"
        subcmd-summary
        ""
        (str "See '" prg-name " <subcommand> --help' for more specific options.")]
       (string/join "\n")))

