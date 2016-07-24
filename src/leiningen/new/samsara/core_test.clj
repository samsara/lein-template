(ns {{name}}.core-test
  (:require [{{name}}.core :refer :all]
            [midje.sweet :refer :all]))


(fact "ENRICHMENT: the game-name must be injected into all events"
      (game-name
        {:eventName "game.started"
         :timestamp 1430760258401
         :sourceId "device1"
         :level 1})
      => (contains {:game-name "Apocalypse Now"}))



(fact "FILTERING: filtering should drop game.ad.displayed"

      ;; no surprise here the predicates work like in filter function
      (no-ads {:eventName "game.level.completed"
               :timestamp 1430760258403
               :sourceId "device1"
               :levelCompleted 1})
      => true

      ;; when it doesn't match `false` or `nil` is returned
      (no-ads  {:eventName "game.ad.displayed"
                :timestamp 1430760258402
                :sourceId "device1"})
      => false)



(fact "CORRELATION: when a game.started event if found with a level=1,
       we could infer that a new player started play with our game."

      (new-player {:eventName "game.started"
                   :timestamp 1430760258401
                   :sourceId "device1"
                   :level 1})
      =>
      [{:timestamp 1430760258401, :sourceId "device1", :eventName "game.new.player"}]

      (new-player {:eventName "game.started"
                   :timestamp 1430760258401
                   :sourceId "device1"
                   :level 5})
      => nil)