(ns share.merge
  (:require [share.util :as util]))

(defn- update-merge
  [state path m]
  (update-in state path util/deep-merge m))

(defn- merge-concat
  [old new]
  (->> (concat old new)
       (remove nil?)
       (distinct)
       (vec)))

(defmulti mergef (fn [state route-handler q result k] k))

(defmethod mergef :home [state route-handler q {:keys [posts] :as result} _k]
  state)

(defmethod mergef :file [state route-handler q {:keys [posts] :as result} _k]
  state)

(defmethod mergef :user [state route-handler q {:keys [posts] :as result} _k]
  state)

;; default
