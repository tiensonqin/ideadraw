(ns web.handler
  (:require [web.handlers.user :as user]
            [web.handlers.draw :as draw]
            [web.handlers.router :as router]
            [web.handlers.default :as default]
            [web.handlers.image :as image]
            ))

(def handler
  (atom
   (merge
    default/handlers
    user/handlers
    draw/handlers
    router/handlers
    image/handlers)))
