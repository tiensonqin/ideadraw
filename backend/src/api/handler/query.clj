(ns api.handler.query
  (:require [taoensso.timbre :as timbre]
            [api.config :as config]
            [api.util :as util]
            [api.db.user :as u]
            [api.db.file :as file]
            [api.db.comment :as comment]
            [api.db.util :as du]
            [api.db.refresh-token :as refresh-token]
            [api.db.report :as report]
            [bidi.bidi :as bidi]
            [api.jwt :as jwt]
            [clojure.java.jdbc :as j]
            [clojure.string :as str]
            [clojure.set :as set]
            [share.util :as su]
            [api.services.slack :as slack]))

;; TODO: support recurisve
(defn get-current-user
  [{:keys [uid datasource]} data]
  (if uid
    (j/with-db-connection [conn datasource]
      (u/get conn uid))))

(defn get-user
  [{:keys [uid datasource]} data]
  (j/with-db-connection [conn datasource]
    (u/get conn (:screen_name data))))

(defn wrap-end?
  [result limit]
  {:result result
   :end? (if (= limit (count result)) false true)})

(def resolvers
  {
   ;; get current user
   :current-user get-current-user

   ;; get specific user
   :user get-user})

(defn one-to-many?
  [field]
  (contains? #{:comments :files} field))

(defn skip-relation
  [key]
  (contains? #{} key))

(defn select
  [col ks]
  (if (= (seq ks) '(:*))
    col
    (if (sequential? col)
      (mapv #(select-keys % ks) col)
      (select-keys col ks))))

;; TODO: parallel
(defn rel-resolve
  [result parent context rel-fields args]
  (if (skip-relation parent)
    result
    (loop [rel-fields rel-fields result result]
      (if (seq rel-fields)
        (let [[rel-field {:keys [fields]
                          :as opts}] (first rel-fields)
              [fk param-key] (if (one-to-many? rel-field)
                               [:id (keyword (str (name parent) "_id"))]
                               [(keyword (str (name rel-field) "_id")) :id])
              id (get result fk)]
          (if id
            (let [resolve-f (get resolvers rel-field)
                  new-args (-> (get args rel-field)
                               (merge (dissoc opts :fields))
                               (assoc param-key id))
                  entity (resolve-f context new-args)
                  rel-entity (if (boolean? (:end? entity))
                               (update entity :result (fn [v] (select v fields)))
                               (select entity fields))]
              (recur (rest rel-fields)
                     (assoc result rel-field rel-entity)))
            ;; TODO: log error, tell this to client
            (recur (rest rel-fields)
                   result)))
        result))))

(defn query
  [context q args]
  (if (seq q)
    (loop [q q m {}]
      (if (seq q)
        (let [[resolver-key {:keys [fields cursor]
                             :as opts}] (first q)]
          (if-let [resolver-f (get resolvers resolver-key)]
            (let [opts (dissoc opts :fields)
                  new-args (merge opts (get args resolver-key))
                  [key-fields rel-fields] (partition-by coll? fields)
                  all-fields (set/union (set key-fields) (set (mapv first rel-fields)))
                  result (some-> (resolver-f context new-args)
                                 (rel-resolve resolver-key context rel-fields (merge opts (dissoc args resolver-key))))

                  result (if (boolean? (:end? result))
                           (update result :result (fn [v] (select v all-fields)))
                           (select result all-fields))]
              (recur (rest q)
                     (assoc m resolver-key result)))
            [:bad "no resolver found"]))
        [:ok m]))
    [:bad "q can't be empty."]))


(defn handler
  [{:keys [context params]
    :as req}]
  (let [{:keys [q args]} params]
    (let [[query result] (query context q args)]
      (if (= query :ok)
        (util/ok result)
        (util/bad result)))))
