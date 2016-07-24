(ns {{name}}.core
  (:require [samsara-core.core :as sam] ; default pipeline
            [moebius.core :refer :all]  ; processing functions
            [moebius.kv :as kv]         ; state management
            ))


;;
;; Enrichment example.
;; You can compute any additional field and inject it
;; directly into the event.
;;
(defenrich game-name
  [event]
  (assoc event :game-name "Apocalypse Now"))


;;
;; Filtering example.
;; You can tell the pipeline to discard events
;; which match a particular condition.
;;
(deffilter no-ads [{:keys [eventName]}]
  (not= eventName "game.ad.displayed"))

;;
;; Correlation example.
;; Based on the events you are processing
;; you can produce new events
;;
(defcorrelate new-player
  [{:keys [eventName level timestamp sourceId] :as event}]

  (when (and (= eventName "game.started")
             (= level 1))
    [{:timestamp timestamp :sourceId sourceId :eventName "game.new.player"}]))

;;
;; Pipelines.
;; Finally you can compose your pipelines
;; chaining your processing functions in the order
;; you wish process them.
;;
(def my-pipeline
  (pipeline
    (sam/make-samsara-pipeline {})
    game-name
    no-ads
    new-player))


;;
;; Finally you can produce a moebius
;; function which is it used by Samsara-CORE
;; to process incoming events.
;;
(defn make-processor [config]
  (moebius my-pipeline))