(ns share.reconciler
  (:require [appkit.reconciler :as r]
            [share.config :as config]
            #?(:cljs [web.handler :as handler])))

(def reconciler
  #?(:clj
     (r/reconciler nil nil nil)
     :cljs
     (r/reconciler handler/handler config/api-host {:domain config/cookie-domain})))
