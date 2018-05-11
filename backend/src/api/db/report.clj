(ns api.db.report
  (:require [api.db.util :as util]))

(defonce ^:private table :reports)

(defn create
  [db m]
  (util/create db table m :flake? true))

(defn delete
  [db report]
  (util/delete db table (:id report)))
