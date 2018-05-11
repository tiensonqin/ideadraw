(ns web.handler
  (:require [web.handlers.draw :as draw]
            [web.handlers.router :as router]))

;; TODO: seperate modules
(def handler
  (atom
   (merge
    draw/handlers
    router/handlers)))
