(ns clojure-example-cli.core
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as string])
  (:gen-class))

(def prg-name "foo")

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

(defn cmd-usage [usage-string options-summary]
  (->> [usage-string
        ""
        "Options:"
        options-summary]
       (string/join "\n")))


;; "cmd1" command

(def cmd1-options
  [["-v" "--verbose"]
   ["-h" "--help"]])

(def cmd1-required-opts #{})

(def cmd1-usage-str (str "Usage: " prg-name " cmd1 [options]"))

(defn cmd1 [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cmd1-options)]
    (cond
      (:help options)
        (do (println "Foo") (exit 0 (cmd-usage cmd1-usage-str summary))) 
      (missing-required-opts? cmd1-required-opts options)
        (cmd-usage cmd1-usage-str summary)
      ((complement empty?) arguments)
        (do (println "Baz") (exit 1 (cmd-usage cmd1-usage-str summary)))
      errors (exit 1 (error-msg errors)))

    (if (:verbose options)
      (println "In verbose mode"))))

;; "cmd2" command

(def cmd2-options
  [["-v" "--verbose"]
   ["-f" "--file FILE" "Input File Path"
    :parse-fn #(identity %)]
   ["-h" "--help"]])

(def cmd2-required-opts #{:file})

(def cmd2-usage-str (str "Usage: " prg-name " cmd2 [options] file"))

(defn cmd2 [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cmd2-options)]

    (cond
      (:help options)
        (do (println "Foo") (exit 0 (cmd-usage cmd2-usage-str summary))) 
      (missing-required-opts? cmd2-required-opts options)
        (cmd-usage cmd2-usage-str summary)
      ((complement empty?) arguments)
        (do (println "Baz") (exit 1 (cmd-usage cmd2-usage-str summary)))
      errors (exit 1 (error-msg errors)))

    (if (:verbose options)
      (println "In verbose mode"))

    (if (:file options)
      (println "Got file" (:file options)))))

(def master-cmd-options
  [["-h" "--help" (str "Overall help for " prg-name)]])

(defn master-cmd-usage [options-summary]
  (->> ["This is my awesome program."
        ""
        (str "Usage: " prg-name " [action] [options]")
        "Options:"
        options-summary
        ""
        "Actions:"
        "    cmd1    cmd1 short description"
        "    cmd2    cmd2 short description"]
       (string/join "\n")))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args master-cmd-options :in-order true)]
    (cond
      (:help options) (exit 0 (master-cmd-usage summary))
      (not (>= (count arguments) 1))  (exit 1 (master-cmd-usage summary))
      errors (exit 1 (error-msg errors)))
    (case (keyword (first arguments))
      :cmd1 (cmd1 (rest arguments))
      :cmd2 (cmd2 (rest arguments))
      (exit 1 (str "Invalid command (" (first arguments) ")  See '" prg-name " --help'.")))))
