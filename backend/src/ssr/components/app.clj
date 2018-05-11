(ns ssr.components.app
  (:require [com.stuartsierra.component :as component]
            [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ssr.middleware.bidi :refer [wrap-bidi]]
            [ssr.middleware.rum :refer [wrap-rum]]
            [ssr.middleware.etag :refer [wrap-etag]]
            [ssr.middleware.auth :refer [wrap-auth]]
            [api.handler.middleware :refer [inject-context wrap-exception wrap-authenticate custom-wrap-cors]]
            [ring.middleware.reload :refer [wrap-reload]]
            [org.httpkit.server :as httpkit]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [share.util :as util]
            [backend.servers :refer [start-server-or-die!]]
            [clojure.string :as str]
            ;; only for dev usage
            [api.components.http-kit :as api]))

(defn make-handler []
  (fn [req]
    {:status  200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body nil}))

(defn wrap-dev-file [handler]
  (fn [req]
    (cond->
      (if util/development?
       ((wrap-file handler "../web/public") req)
       ((wrap-resource handler "public") req))
      (str/ends-with? (:uri req) ".js")
      (assoc-in [:headers "Content-Type"] "application/javascript"))))

(defn wrap-dev-api [handler]
  (fn [req]
    (if (and util/development? (str/starts-with? (:uri req) "/api/"))
      (let [result (@api/api-dev-handler (update req :uri (fn [x]
                                                            (if x
                                                              (subs x 4)
                                                              x))))]
        result)
      (handler req))))

(defn wrap-production-etag [handler]
  (fn [req]
    (if-not util/development?
      ((wrap-etag handler) req)
      (handler req))))

(defn wrap-production-gzip [handler]
  (fn [req]
    (if util/development?
      (handler req)
      ((wrap-gzip handler) req))))

;; TODO: reload not works
(defn middlewares
  [app context root-ui routes resolver render-page]
  (-> app
      (wrap-reload)
      ;; (wrap-exception)
      (wrap-rum root-ui resolver render-page)
      (wrap-dev-api)
      (wrap-dev-file)
      (wrap-production-etag)
      (wrap-auth)
      (wrap-keyword-params)
      (wrap-params)
      (wrap-restful-format)
      (inject-context context)
      (wrap-bidi routes)
      (wrap-cookies)
      (custom-wrap-cors)
      (wrap-production-gzip)))

(defrecord Ssr [hikari ssr-server ssr-port root-ui routes resolver render-page rpc]
  component/Lifecycle
  (start [component]
    (let [context {:datasource {:datasource (:datasource hikari)}}
          handler (-> (make-handler)
                      (middlewares context root-ui routes resolver render-page))
          ssr-server (start-server-or-die!
                      :ssr-server
                      ssr-port
                      (fn []
                        (httpkit/run-server handler {:port ssr-port})))]
      (assoc component
            :ssr-server ssr-server)))
  (stop [component]
    (when ssr-server
      (ssr-server))
    (dissoc component :ssr-server)))

(defn new-ssr-server [ssr-port root-ui routes resolver render-page]
  (map->Ssr {:ssr-port ssr-port
             :root-ui root-ui
             :routes routes
             :resolver resolver
             :render-page render-page}))
