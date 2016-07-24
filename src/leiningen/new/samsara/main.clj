(ns {{name}}.main
  (:require [{{name}}.core :refer :all]
            [samsara-core.main :as sam])
  (:gen-class))


(defn -main [config-file]
  (println "Starting streaming processing.")
  (sam/start-processing! (sam/init! config-file)))