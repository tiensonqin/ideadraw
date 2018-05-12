(ns api.db.user
  (:refer-clojure :exclude [get update])
  (:require [clojure.java.jdbc :as j]
            [api.db.util :as util]
            [clojure.string :as str]
            [honeysql.core :as sql]
            [api.db.refresh-token :as token]
            [api.jwt :as jwt]
            [api.cookie :as cookie]
            [api.util]
            [share.util :as su]))

(defonce ^:private table :users)
(def ^:private fields [:*])

(defonce ^:private base-map {:select fields
                             :from [table]})

(defn db-get
  [db id]
  (util/get db base-map id))

(defn get
  "Get user's info by id."
  ([db id]
   (get db id :all))
  ([db id keys]
   (let [id (if (uuid? id) id
                (and (string? id)
                     (util/get-id-by-field db table {:screen_name id})))]
     (db-get db id))))

(defn create
  [db m]
  (util/create db table m :flake? true))

(defn update
  [db id m]
  (let [result (util/update db table id (dissoc m :screen_name :oauth_id :block :flake_id))]
    (get db id)))

(defn oauth-authenticate
  [db type id]
  (db-get db {:oauth_type (name type)
              :oauth_id id}))

(defn generate-tokens
  [db user]
  (cookie/token-cookie
   {:access-token  (jwt/sign (select-keys user [:id :screen_name]))
    :refresh-token (token/create db (:id user))}))

(defn block
  [db id]
  (util/update db table id {:block true}))

(defn unblock
  [db id]
  (util/update db table id {:block false}))
