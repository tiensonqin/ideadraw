(ns api.util
  (:require [api.flake.core :as flake]
            [api.flake.utils :as fu]
            [clojure.string :as str]
            [environ.core :refer [env]])
  (:import  [java.util UUID]))

(defn production?
  []
  (= "production" (:env env)))

(defn uuid
  "Generate uuid."
  []
  (UUID/randomUUID))

(defn ->uuid
  [s]
  (if (uuid? s)
    s
    (UUID/fromString s)))

(defn update-if
  "Update m if k exists."
  [m k f]
  (if-let [v (get m k)]
    (assoc m k (f v))
    m))

(defn flake-id
  []
  (flake/generate!))

(defn flake-id->str
  []
  (fu/base62-encode (flake-id)))

(defn get-avatar
  [avatar]
  (let [type (cond
               (re-find #"google" avatar)
               :google

               :else
               :s3)]
    (cond
      (= :google type)
      (str/replace avatar "/s120/" "/s360/")

      :else
      avatar)))

(defn ok
  ([body]
   (ok body nil))
  ([body headers]
   (cond->
     {:status 200
      :body body}
     headers
     (assoc :headers headers))))

(defn bad
  [message]
  {:status 400
   :body {:message (str message)}})

(def not-found
  {:status 404
   :message "Not Found"})

(defn ->response
  [[type data]]
  (if (= type :ok)
    (ok data)
    (bad data)))

(defn ->tree
  [col parent-key assoc-path?]
  (loop [col col
         paths {}
         new-col {}]
    (if-let [item (first col)]
      (let [id (:id item)
            parent-id (parent-key item)]
        (recur (rest col)
               (if (parent-key item)
                 (assoc paths id (-> (get paths parent-id)
                                     (concat [:children id])
                                     (vec)))
                 (assoc paths id [id]))
               (if (parent-key item)
                 (let [p (concat (get paths parent-id)
                                 [:children id])]
                   (assoc-in new-col p
                             (if assoc-path?
                               (assoc item :path p)
                               item)))
                 (assoc new-col id item))))
      new-col)))

(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new
  nested structure. keys is a sequence of keys. Any empty maps that result
  will not be present in the new structure."
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
    (dissoc m k)))

(defmacro doseq-indexed
  "loops over a set of values, binding index-sym to the 0-based index of each value"
  ([[val-sym values index-sym] & code]
   `(loop [vals# (seq ~values)
           ~index-sym (long 0)]
      (if vals#
        (let [~val-sym (first vals#)]
          ~@code
          (recur (next vals#) (inc ~index-sym)))
        nil))))

(defn indexed [coll] (map-indexed vector coll))

(def not-found-resp
  {:status 404
   :body "<!DOCTYPE html>\n<html><body>not found</body></html>"})
