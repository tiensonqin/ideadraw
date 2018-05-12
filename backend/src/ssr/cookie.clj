(ns ssr.cookie
  (:require [buddy.sign.compact :as buddy]
            [api.config :refer [config]]))

(def cookie-hash (:ssr-cookie-hash config))

(defn sign [token]
  (buddy/sign token cookie-hash))

(defn unsign [cookie]
  (buddy/unsign cookie cookie-hash))

(defn token-cookie [token]
  {"x" {:value   (sign token)
        :max-age (* (* 3600 24) 30)}})

(defn get-token [req]
  (when-let [cookie (get-in req [:cookies "access-token" :value])]
    (unsign cookie)))
