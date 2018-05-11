(ns ssr.middleware.rum
  (:require [rum.core :as rum]
            [share.reconciler :as r]
            [ring.util.response :as resp]
            [api.config :as config]
            [api.util :as util]
            [clj-social.core :as social]
            [api.cookie :as cookie]
            [clojure.string :as str]))

(defn- render
  [req resolver ui-root render-page res after-resolver]
  (let [state (resolver req)
        state (if after-resolver (after-resolver state) state)]
    (reset! (:state r/reconciler) state)
    (assoc res
           :body
           (-> (ui-root state)
               (rum/render-html)
               (render-page req state)))))

;; render web app
(defn wrap-rum [handler ui-root resolver render-page]
  (fn [req]
    (let [uri (:uri req)
          route (:ui/route req)
          datasource (get-in req [:context :datasource])]
      (cond
        ;; social login
        (= "/logout" (:uri req))
        (-> (resp/redirect (:website-uri config/config))
            (assoc :cookies cookie/delete-token))

        (not (:ui/route req))
        ;; not found
        util/not-found-resp

        :else
        (render req resolver ui-root render-page (handler req) nil)))))
