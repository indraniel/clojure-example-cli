(ns clojure-example-cli.core
  (:require [clojure-example-cli.utils.common :as c]
            [clojure-example-cli.utils.cli :as helper]
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

;; subcommand parsers
(defmulti subcommand-parser (fn [args subcmd] subcmd))

; "cmd1" subcommand parser
(defmethod subcommand-parser :cmd1
  [args subcmd]
  (let [ {opts :options usage-str :usage-string required-opts :required-options} (subcmd prg-cmds)  
         {:keys [options arguments errors summary]} (cli/parse-opts args opts) 
         usage (helper/cmd-usage usage-str summary)
         missing-opts? (helper/missing-required-opts? required-opts options) ]
    (cond
      (:help options) (c/exit 0 usage)
      missing-opts?   (c/exit 0 (str "Missing required param!\n" usage))
      (not (empty? arguments)) (c/exit 1 str "Too many extra args!\n" usage)
      errors (c/exit 1 (c/error-msg errors)))

    (if (:verbose options)
      (println "In verbose mode"))))

;; "cmd2" subcommand parser
(defmethod subcommand-parser :cmd2 
  [args subcmd]
  (let [ {opts :options usage-str :usage-string required-opts :required-options} (subcmd prg-cmds)  
         {:keys [options arguments errors summary]} (cli/parse-opts args opts) 
         usage (helper/cmd-usage usage-str summary)
         missing-opts? (helper/missing-required-opts? required-opts options) ]
    (cond
      (:help options) (c/exit 0 usage)
      missing-opts?   (c/exit 0 (str "Missing required param!\n" usage))
      (empty? arguments) (c/exit 1 (str "Please pass in a arg\n" usage))
      errors (c/exit 1 (c/error-msg errors)))

    (if (:verbose options)
      (println "In verbose mode")) 

    (if (:file options)
      (println (str "Got file: '" (:file options) "'")))
    
    (println "arguments:" arguments)))

;; "default"/catchall subcommand parser
(defmethod subcommand-parser :default
  [args subcmd]
  (c/exit 1 (format "Subcommand '%s' not available. %s"
                    (name subcmd)
                    "Please implement the subcommand parser!")))

;; "root" program parser
(defn -main [& args]
  (let [ {opts :options required-opts :required-options} (:root prg-cmds)
         {:keys [options arguments errors summary]} (cli/parse-opts args opts :in-order true)
         subcmds (vec (-> prg-cmds :root :available-commands sort))
         subcmd-docs (vec (map #(:doc (% prg-cmds)) subcmds)) 
         subcmd-summary (helper/generate-subcommand-usage-string subcmds subcmd-docs)
         usage (helper/root-usage prg-name prg-short-description summary subcmd-summary) 
         subcommand (keyword (first arguments)) ]
    (cond
      (:help options) (c/exit 0 usage)
      (not (>= (count arguments) 1))  (c/exit 1 (str "Please use a valid subcommand! "
                                                   "See '" prg-name " --help'."))
      (not (contains? (set subcmds) subcommand)) (c/exit 1 (str "Invalid subcommand "
                                                              "(" (name subcommand) "). "
                                                              "See '" prg-name " --help'."))
      errors (c/exit 1 (c/error-msg errors)))
    (subcommand-parser (rest arguments) subcommand)))
