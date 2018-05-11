(ns api.db.comment
  (:refer-clojure :exclude [get update])
  (:require [clojure.java.jdbc :as j]
            [api.db.util :as util]
            [api.util :as au]
            [api.db.user :as u]))

(defonce ^:private table :comments)
(defonce ^:private fields [:*])

(defonce ^:private base-map {:select fields
                             :from [table]})

(defn get
  [db id]
  (util/get db base-map id))

(defn create
  [db m]
  (util/create db table m :flake? true))

(defn update
  [db id m]
  (util/update db table id (dissoc m :flake_id)))

(defn delete
  [db id]
  (when-let [comment (get db id)]
    (update db id {:del true})))

(defn get-file-comments
  [db file-id {:keys [after order limit order-key]
               :or {order-key "flake_id"
                    order "asc"
                    limit 10
                    after 0}
               :as cursor}]
  )
