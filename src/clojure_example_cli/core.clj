(ns clojure-example-cli.core
  (:require [clojure.tools.cli :as cli]
            [clj-sub-command.core :as sc]
            [clojure.string :as string])
  (:gen-class))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join "\n" errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

;; "cmd1" command

(def cmd1-options
  [["-v" "--verbose"]
   ["-h" "--help"]])

;;(def required-cmd1-opts #{})

(defn cmd1-usage [options-summary]
  (->> ["Usage: foo cmd1 [options] file"
        ""
        "Options:"
        options-summary]
       (string/join "\n")))

; (defn missing-required-cmd1-opts?
;  [opts]
;  (not-every? opts required-cmd1-opts))

(defn cmd1 [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cmd1-options)]
    (println options)
    (println arguments)
    (cond
      (:help options) (do (println "Foo") (exit 0 (cmd1-usage summary))) 
 ;;     (missing-required-cmd1-opts? options) (cmd1-usage summary)
      ((complement empty?) arguments) (do (println "Baz") (exit 1 (cmd1-usage summary)))
      errors (exit 1 (error-msg errors)))
    (if (:verbose options)
      (println "In verbose mode"))))

;; "cmd2" command

(def cmd2-options
  [["-v" "--verbose"]
   ["-f" "--file FILE" "Input File Path"
    :parse-fn #(identity %)]
   ["-h" "--help"]])

(def required-cmd2-opts #{:file})

(defn cmd2-usage [options-summary]
  (->> ["Usage: foo cmd2 [options] file"
        ""
        "Options:"
        options-summary]
       (string/join "\n")))

(defn missing-required-cmd2-opts?
  [opts]
  (not-every? opts required-cmd2-opts))

(defn cmd2 [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cmd2-options)]
    (cond
      (:help options) (exit 0 (cmd2-usage summary))
      (missing-required-cmd2-opts? options) (cmd2-usage summary)
      ((complement empty?) arguments) (exit 1 (cmd2-usage summary))
      errors (exit 1 (error-msg errors)))
    (if (:verbose options)
      (println "In verbose mode"))
    (if (:file options)
      (println "Got file" (:file options)))))

(defn -main [& args]
  (let [[opts cmd args help cands]
        (sc/sub-command args
                        "Usage: foo [-h] {cmd1,cmd2} ..."
                        :options [["-h" "--help" "Show help" :default false :flag true]]
                        :commands [["cmd1" "Description for cmd1"]
                                   ["cmd2" "Description for cmd2"]])]
    (when (:help opts)
      (exit 0 help))
    (case cmd
      :cmd1 (cmd1 args)
      :cmd2 (cmd2 args)
      (exit 1 (str "Invalid command.  See 'foo --help'.\n\n"
                   (sc/candidate-message cands))))))
