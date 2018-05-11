(ns api.db.util
  (:refer-clojure :exclude [get update])
  (:require [environ.core :refer [env]]
            [clj-time
             [coerce :refer [to-sql-time]]
             [core :refer [now]]]
            [clojure.java.jdbc :as j]
            [honeysql.core :as sql]
            [honeysql.format :as fmt]
            [api.util :refer [flake-id]]
            [api.pg.json]
            [api.pg.types]
            [clojure.string :as str]))


(defn query
  [db honey-data]
  (let [v (sql/format honey-data)]
    ;; (prn {:v v})
    (j/query db v)))

(defn execute!
  [db honey-data]
  (j/execute! db (sql/format honey-data)))

(defn ->sql-time
  [datetime]
  (^java.sql.Timestamp to-sql-time datetime))

(defn sql-now
  []
  (->sql-time (now)))

(defn with-timestamp
  [m k-or-ks]
  {:pre [(map? m) (not (empty? m))]}
  (let [ks (if (sequential? k-or-ks) k-or-ks [k-or-ks])
        time (sql-now)]
    (merge (zipmap ks (repeat time))
           m)))

(defn build-where
  [m]
  (cond
    (map? m)
    (into [:and]
          (for [[k v] m] [:= k v]))

    (coll? m)
    m

    ;; id
    :else
    [:= :id m]))

(defn exists?
  [db table where]
  (-> (j/query db (-> (sql/format {:exists {:select [:1]
                                            :from [table]
                                            :where (build-where where)}})
                      (clojure.core/update 0 (fn [s] (str "SELECT " s)))))
      first
      :exists))

(defn id-exists?
  [db table id]
  (exists? db table id))


(defn get
  [db base-map id-or-where]
  (-> (query db (assoc base-map :where (build-where id-or-where)))
      (first)))

(defn get-fields
  [db table id-or-where fields]
  (-> (query db {:select fields
                 :from [table]
                 :where (build-where id-or-where)})
      (first)))

(defn get-id-by-field
  [db table where]
  (-> (query db {:select [:id]
                 :from [table]
                 :where (build-where where)})
      (first)
      (:id)))

(defn get-all
  [db table & {:keys [where]}]
  (query db (cond-> {:from [table]
                     :select [:*]}
              where
              (assoc :where where))))

(defn get-by-ids
  [db table ids {:keys [fields order-key order where order? id-key]
                 :or {fields [:*]
                      order-key :flake_id
                      id-key :id
                      order :desc
                      order? true}}]
  (if (seq ids)
    (query db (cond->
                {:from [table]
                 :select fields
                 :where (if where
                          (conj where [:in id-key ids])
                          [:in id-key ids])}
                order?
                (assoc :order-by (if (not= order-key :flake_id)
                                   [[order-key order] [:flake_id order]]
                                   [[order-key order]]))))))

(defn select-one-field
  [db table id-or-where field]
  (-> (query db {:select [field]
                 :from [table]
                 :where (build-where id-or-where)})
      (first)
      (clojure.core/get field)))

(defn create
  [db table m & {:keys [flake?]}]
  (-> (j/insert! db table
                 (cond-> m
                   flake? (assoc :flake_id (flake-id))))
      first))

(defn update
  [db table id m]
  (try
    (-> (j/update! db table m ["id = ?" id])
        first)
    ;; TODO why not throw DuplicateException
    (catch java.sql.BatchUpdateException e
      [:error :duplicated])))

(defn delete
  [db table id-or-where]
  (-> (j/execute! db (sql/format {:delete-from table
                                  :where (build-where id-or-where)}))
      first))

(defn cursor-conditions-transform
  [{:keys [order-key after before order where]
    :or {order-key :flake_id
         order :desc}
    :as cursor}]
  (let [less-than (if (= order-key :rank)
                    :<=
                    :<)]
    (cond
      where
      where

      (and after before)
      (cond
        (= :desc order)
        [:and [:< order-key after] [:>= order-key before]]
        (= :asc order)
        [:and [:> order-key after] [:<= order-key before]])

      (some? after)
      (cond
        (= :desc order)
        [less-than order-key after]
        (= :asc order)
        [:> order-key after])

      (some? before)
      (cond
        (= :desc order)
        [:> order-key before]
        (= :asc order)
        [:< order-key before])

      :else
      nil)))

(defn split-condition
  "[:and [:> :id 5] [:< :id 10]] => [:and [[:> :id 5] [:< :id 10]]]

  [[:> :id 5] [:< :id 10]] => [:and [[:> :id 5] [:< :id 10]]]

  [:> :id 5] => [:and [[:> :id 5]]] "
  [condition]
  (cond
    (or (nil? condition) (empty? condition))
    nil

    (#{:and :or} (first condition))
    [(first condition) (vec (rest condition))]

    (vector? (first condition))
    [:and condition]

    :else
    [:and [condition]]))

(defn concat-bool-and-conditions
  "[:and [[:> :id 5] [:< :id 10]]] => [:and [:> :id 5] [:< :id 10]]
  [:and [[:> :id 5]] => [:> :id 5]"
  [c]
  (let [[bool-p conditions-p] c]
    (if (= (count conditions-p) 1)
      (first conditions-p)
      (into [bool-p] conditions-p))))

(defn add-where-cond
  "@condition : [:and honey-where-condition] or honey-where-condition
  eg: [:and [:> :id 5]]  or [:> :id 5] or [:and [:> :id 5] [:< :id 10]]"
  [where next-condition]
  (let [[w n] (map split-condition [where next-condition])]
    (cond
      (and w (nil? n)) (concat-bool-and-conditions w)

      (and n (nil? w)) (concat-bool-and-conditions n)

      (and where next-condition)
      (if (= (first n) (first w))
        (into [(first n)] (concat (second w) (second n)))
        (into [(first n) (concat-bool-and-conditions w)] (second n)))

      :else
      [:= 1 1])))

(defn wrap-where [hn-map condition]
  (if (some? condition)
    (update-in hn-map [:where] add-where-cond condition)
    hn-map))

(defn wrap-cursor [honey-map cursor]
  (let [{:keys [after before order limit order-key]
         :or {order-key :flake_id limit 10 order :desc}} cursor
        order (keyword order)
        ts-cursor (cursor-conditions-transform cursor)
        order-by (if (not= order-key :flake_id)
                   [[order-key order] [:flake_id order]]
                   [[order-key order]])]
    (-> (wrap-where honey-map ts-cursor)
        (assoc :order-by order-by)
        (assoc :limit limit))))

(defn count-total [db hn-map]
  (-> (dissoc hn-map :limit :offset :order-by)
      (update-in [:select] (constantly [:%count.*]))
      (#(query db %))
      first :count))

(fmt/register-clause! :inc 101)
(defmethod fmt/format-clause :inc [[op v] sqlmap]
  (format "SET %s = %s + 1" (name v) (name v)))

(fmt/register-clause! :dec 102)
(defmethod fmt/format-clause :dec [[op v] sqlmap]
  (format "SET %s = %s - 1" (name v) (name v)))

;; cool
(defmethod fmt/fn-handler "any" [_ v field]
  (format "'%s' = ANY(%s)" (name v) (name field)))

(defn filter-id
  [col id]
  (first (filter (fn [item] (= id (:id item))) col)))

(defn strip-id
  [x]
  (keyword (str/replace (name x) "_id" "")))

(defn with
  [entity fk f]
  (letfn [(switch [e] (if-let [fk-id (fk e)]
                        (-> e
                            (dissoc fk)
                            (assoc (strip-id fk) (f fk-id)))
                        e))]
    (if (sequential? entity)
      (let [ids (set (map fk entity))
            results (zipmap ids (map f ids))]
        (for [e entity]
          (assoc e (strip-id fk) (clojure.core/get results (fk e)))))
      (switch entity))))

(defn with-col
  [entity db m]
  (reduce
   (fn [entity [k {:keys [table fields]
                   :or  {fields [:*]}}]]
     (if-let [ids (clojure.core/get entity k)]
       (let [[q table & args] (sql/format {:select fields
                                           :from [table]
                                           :where [:in :id ids]})
             q (str/replace q "FROM ?" (str "FROM " table))
             sql-vector (vec (cons q args))]
         (assoc entity k (->> (j/query db sql-vector)
                              (remove nil?)
                              (vec))))
       entity))
   entity
   m))

(defn owner?
  [db uid table id]
  (= uid (select-one-field db table id :user_id)))
