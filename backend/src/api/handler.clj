(ns api.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [api.handler.http :as http]
            [api.handler.middleware :as middleware]))

;; TODO: replace with bidi
(defroutes app
  (GET "/" [] "Hello world")

  (POST "/" req
    (http/handler req))

  (POST "/upload" req
    (http/upload req))

  (route/not-found "<h1>Page not found</h1>"))

(defn app-handler
  [context]
  (middleware/middlewares #'app context))
