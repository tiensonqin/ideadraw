(ns api.components.hikari
  (:require [com.stuartsierra.component :as component]
            [hikari-cp.core :as hikari]
            [clojure.java.jdbc :as j]))

(defrecord Hikari [db-spec datasource]
  component/Lifecycle
  (start [component]
    (let [s (or datasource (hikari/make-datasource db-spec))]
      ;; set time zone
      (j/execute! {:datasource s} ["set time zone 'UTC'"])
      (assoc component :datasource s)))
  (stop [component]
    (when datasource
      (hikari/close-datasource datasource))
    (assoc component :datasource nil)))

(defn new-hikari-cp [db-spec]
  (map->Hikari {:db-spec db-spec}))
