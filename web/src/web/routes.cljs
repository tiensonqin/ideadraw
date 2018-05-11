(ns web.routes
  (:require [appkit.citrus.core :as citrus]
            [clojure.string :as str]
            [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [clojure.walk :as walk]
            [share.util :as util]
            [share.routes :as routes]))

(defn start! [reconciler]
  (let [history (pushy/pushy
                 (fn [])
                 #(citrus/dispatch! reconciler :router/push % false)
                 (fn [uri]
                   (bidi/match-route routes/routes uri)))]
    (pushy/start! history)
    history))
