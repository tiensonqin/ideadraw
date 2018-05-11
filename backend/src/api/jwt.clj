(ns api.jwt
  (:require [buddy.sign.jwt :as jwt]
            [clj-time.core :as time]
            [api.config :as config]))

(defonce secret (:jwt-hash config/config))

(defn sign
  "Serialize and sign a token with defined claims"
  ([m]
   (sign m (* 60 60 12)))
  ([m expire-secs]
   (let [claims (assoc m
                       :exp (time/plus (time/now) (time/seconds expire-secs)))]
     (jwt/sign claims secret))))

(defn unsign
  [token]
  (jwt/unsign token secret))

(defn unsign-skip-validation
  [token]
  (jwt/unsign token secret {:skip-validation true}))
