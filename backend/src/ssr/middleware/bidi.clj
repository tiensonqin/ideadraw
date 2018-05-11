(ns ssr.middleware.bidi
  (:require [bidi.bidi :as bidi]
            [share.routes :as routes]))

(defn wrap-bidi [handler routes]
  (fn [req]
    (if-let [route (bidi/match-route routes (:uri req))]
      (-> req
          (assoc :ui/route route)
          handler)
      (handler req))))
