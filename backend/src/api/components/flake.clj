(ns api.components.flake
  (:require [com.stuartsierra.component :as component]
            [api.flake.core :as flake]
            [api.config :refer [config]]))

(defrecord Flake []
  component/Lifecycle
  (start [component]
    (println "flake started!")
    (flake/init! (:flake-path config))
    component)
  (stop [component]
    (println "flake stoped!")
    component))

(defn new-flake []
  (map->Flake {}))
