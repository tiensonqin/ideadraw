(ns api.db.refresh-token
  (:require [clojure.java.jdbc :as j]
            [api.db.util :as util]
            [honeysql.core :as sql]
            [api.util :refer [uuid]]))

(defonce ^:private table :refresh_tokens)

(defn get-token
  [db user-id]
  (util/select-one-field db table [:= :user_id user-id] :token))

(defn exists?
  [db token]
  (util/exists? db table [:= :token token]))

(defn get-user-id-by-token
  [db token]
  (util/select-one-field db table [:= :token token] :user_id))

(defn create
  [db user-id]
  (if-let [token (get-token db user-id)]
    token
    (loop [token (uuid)]
      (if (exists? db token)
        (recur (uuid))
        (do
          (util/create db table {:user_id user-id
                                 :token token})
          token)))))

(defn delete
  [db user-id]
  (if user-id
    (j/delete! db table ["user_id = ?" user-id])))
