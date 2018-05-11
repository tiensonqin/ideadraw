(ns web.handlers.router
  (:require [goog.object :as gobj]
            [pushy.core :as pushy]
            [bidi.bidi :as bidi]
            [share.routes :refer [routes]]))

(def handlers
  {:router/setup-pushy-history
   (fn [state history]
     {:state {:history history}})

   :router/push
   (fn [state route redirect?]          ; redirect?: whether from redirect effect
     (when (and redirect?
                (map? route)
                (:handler route))
         (pushy/set-token! (:history state)
                        (apply bidi/path-for routes
                          (:handler route)
                          (interleave (keys (:route-params route))
                                      (vals (:route-params route))))))
     {:state (cond-> state
               (map? route)
               (merge state route))})

   :router/replace
   (fn [state token]
     (if token (pushy/replace-token! (:history state) token))
     {:state state})

   :router/back
   (fn [state]
     (.back ^js (gobj/get js/window "history"))
     {:state state})})
