(ns api.components.http-kit
  (:require [com.stuartsierra.component :as component]
            [org.httpkit.server :as httpkit]
            [api.handler :as handler]
            [share.util :as util]))

(defonce api-dev-handler (atom nil))

(defrecord WebServer [hikari server http-port]
  component/Lifecycle
  (start [component]
    (let [context {:datasource {:datasource (:datasource hikari)}}
          api-handler (handler/app-handler context)
          api-server (httpkit/run-server api-handler {:port http-port})]
      (when util/development?
        (reset! api-dev-handler api-handler))
      (assoc component
             :server api-server)))
  (stop [component]
    (when server
      (server))
    (assoc component
           :server nil)))

(defn new-web-server
  [http-port]
  (map->WebServer {:http-port http-port}))
