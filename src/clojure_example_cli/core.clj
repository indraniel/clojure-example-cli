(ns clojure-example-cli.core
  (:require [trptcolin.versioneer.core :as version]
            [clojure.tools.cli :as cli]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.edn :as edn])
  (:gen-class))

(def prg-name "foo")

(def prg-short-description 
  (some-> (io/resource "project.clj")
          (slurp)
          (edn/read-string)
          (nthrest 3)
          (#(apply hash-map %))
          (:description)))

(def prg-cmds
  {:root {:options [["-h" "--help" "Show overall command functionality"]],
          :required-options #{},
          :available-commands #{:cmd1 :cmd2}}
   :cmd1 {:doc "cmd1 short description"
          :options [["-v" "--verbose" "Enable verbose mode"]
                    ["-h" "--help" "Show cmd1 functionality"]],
          :required-options #{},
          :usage-string (string/join " " ["Usage:" prg-name "cmd1 [options]"])}
   :cmd2 {:doc "cmd2 short description"
          :options [["-v" "--verbose" "Enable verbose mode"]
                    ["-f" "--file FILE" "Input file path (REQUIRED)"
                     :parse-fn #(identity %)]
                    ["-h" "--help" "Show cmd2 functionality"]],
          :required-options #{:file},
          :usage-string (string/join " " ["Usage:" prg-name "cmd2 [options] args"])}})

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join "\n" errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

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

(defn root-usage [options-summary subcmd-summary]
  (->> [(string/join " " [prg-short-description "-"
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

;; "cmd1" subcommand parser
(defn cmd1 [args]
  (let [ {opts :options usage-str :usage-string required-opts :required-options} (:cmd1 prg-cmds)  
         {:keys [options arguments errors summary]} (cli/parse-opts args opts) 
         usage (cmd-usage usage-str summary)
         missing-opts? (missing-required-opts? required-opts options) ]
    (cond
      (:help options) (exit 0 usage)
      missing-opts?   (exit 0 (str "Missing required param!\n" usage))
      (not (empty? arguments)) (exit 1 str "Too many extra args!\n" usage)
      errors (exit 1 (error-msg errors)))

    (if (:verbose options)
      (println "In verbose mode"))))

;; "cmd2" subcommand parser
(defn cmd2 [args]
  (let [ {opts :options usage-str :usage-string required-opts :required-options} (:cmd2 prg-cmds)  
         {:keys [options arguments errors summary]} (cli/parse-opts args opts) 
         usage (cmd-usage usage-str summary)
         missing-opts? (missing-required-opts? required-opts options) ]
    (cond
      (:help options) (exit 0 usage)
      missing-opts?   (exit 0 (str "Missing required param!\n" usage))
      (empty? arguments) (exit 1 (str "Please pass in a arg\n" usage))
      errors (exit 1 (error-msg errors)))

    (if (:verbose options)
      (println "In verbose mode")) 

    (if (:file options)
      (println (str "Got file: '" (:file options) "'")))
    
    (println "arguments:" arguments)))

;; "root" program parser
(defn -main [& args]
  (let [ {opts :options required-opts :required-options} (:root prg-cmds)
         {:keys [options arguments errors summary]} (cli/parse-opts args opts :in-order true)
         subcmds (vec (-> prg-cmds :root :available-commands sort))
         subcmd-docs (vec (map #(:doc (% prg-cmds)) subcmds)) 
         subcmd-summary (generate-subcommand-usage-string subcmds subcmd-docs)
         usage (root-usage summary subcmd-summary) 
         subcommand (keyword (first arguments)) ]
    (cond
      (:help options) (exit 0 usage)
      (not (>= (count arguments) 1))  (exit 1 (str "Please use a valid subcommand! "
                                                   "See '" prg-name " --help'."))
      (not (contains? (set subcmds) subcommand)) (exit 1 (str "Invalid subcommand "
                                                              "(" (name subcommand) "). "
                                                              "See '" prg-name " --help'."))
      errors (exit 1 (error-msg errors)))
    (case subcommand
      :cmd1 (cmd1 (rest arguments))
      :cmd2 (cmd2 (rest arguments)))))
