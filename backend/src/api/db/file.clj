(ns api.db.file
  (:refer-clojure :exclude [get update])
  (:require [clojure.java.jdbc :as j]
            [api.db.util :as util]
            [clojure.string :as str]
            [honeysql.core :as sql]))

(defonce ^:private table :files)
(def ^:private fields [:*])

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
  (util/update db table id m))

(defn delete
  [db id m]
  (util/update db table id {:del true}))
