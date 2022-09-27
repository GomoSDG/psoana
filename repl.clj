#!/usr/bin/env bb

(require '[babashka.process :as p]
         '[clojure.tools.cli :as cli]
         '[clojure.string :as str]
         '[clojure.java.io :as io]
         '[clojure.edn :as edn])

(defn io-repl [cmd dir]
  (println (str/join " " cmd))
  (p/process cmd {:dir dir
                  :out :inherit
                  :err :inherit
                  :shutdown p/destroy-tree}))

(defn ppp [form]
  (println (pr-str form))
  (println form))

(defn io-loop [cmd dir init-form]
  (let [p (io-repl cmd dir)
        input-writer (io/writer (:in p))]
    (binding [*out* input-writer]
      (ppp init-form)
      (loop []
        (when-let [v (read-line)]
          (println v))
        (recur)))))



(defn start-dev-repl
  ([]
   (io-loop ["clj" "-M:dev:test:repl"] "." "")))

(start-dev-repl)
