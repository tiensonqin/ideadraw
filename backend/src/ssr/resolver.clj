(ns ssr.resolver
  (:require [api.cookie :as cookie]
            [api.handler.query :as query]
            [share.query :refer [queries]]
            [share.merge :as merge]
            [api.util :as util]
            [api.db.user :as u]
            [share.util :as su]
            [clojure.string :as str]))

(defn get-locale
  [req]
  (if-let [locale (get-in req [:cookies "locale" :value])]
    (keyword locale)
    (if-let [accept-language (get-in req [:headers "accept-language"])]
      (if-let [v (first (str/split accept-language #","))]
        (cond (str/starts-with? v "en-")
              :en

              (let [v (-> v
                          (str/replace "_" "-")
                          (str/lower-case))]
                (re-find #"zh-cn" v))
              :zh-cn

              :else
              (-> v
                  (str/replace "_" "-")
                  (str/lower-case)
                  (keyword v)))
        :en)
      :en)))

(defn make-resolver [req]
  (let [locale (get-locale req)
        {:keys [handler route-params]} (:ui/route req)
        uid (get-in req [:context :uid])
        q-fn (get queries handler)
        route-params (if (and (= handler :home) uid)
                       (assoc route-params :current-user uid)
                       route-params)
        db (:datasource (:context req))
        current-user (query/get-current-user (:context req) nil)
        state {:locale       (keyword locale)
               :router       (:ui/route req)
               :user         {:current current-user}}
        state (if q-fn
                (let [query (q-fn route-params)]
                  (if query
                    (let [{:keys [q args] :as q-opts} query
                          state (assoc state :query
                                       (merge
                                        {:loading? {handler false}}
                                        q-opts))
                          result (let [[query result] (query/query (:context req) q args)]
                                   (if (= query :ok)
                                     result
                                     nil))]
                      (reduce (fn [state [k v]]
                                (merge/mergef
                                 state
                                 handler
                                 q-opts
                                 result
                                 k))
                              state
                              (if (:merge q-opts)
                                (:merge q-opts)
                                {handler true})))
                    state))
                (assoc state :query {:loading? nil
                                     :q nil}))]
    (assoc-in state [:query :client-mode?] false)))
