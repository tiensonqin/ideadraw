(ns share.query
  (:require [clojure.string :as str]
            [share.util :as util]))

;; key: route handler
(def queries
  {:home
   (fn [args]
     {:q {}
      :args {}})
   :file
   (fn [args]
     {:q {}
      :args {}})
   :user
   (fn [args]
     {:q {}
      :args {}})})
