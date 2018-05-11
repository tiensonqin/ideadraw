(ns web.core
  (:require [appkit.db :as db]
            [share.reconciler :refer [reconciler]]
            [appkit.citrus :as citrus]
            [rum.core :as rum]
            [web.routes :as routes]
            [goog.dom :as dom]
            [share.components.root :as root]
            ;; [cognitect.transit :as t]
            ))

(defn start
  []
  (routes/start! reconciler)
  (citrus/dispatch-sync! :router/setup-pushy-history
                         (routes/start! reconciler))

  (rum/hydrate (root/root reconciler)
               (dom/getElement "app"))
  (citrus/dispatch-sync! :draw/set-paper-state reconciler))

(defn ^:export init [state]
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds

  ;; (let [state (t/read (t/reader :json) state)]
  ;;   (reset! db/state state))
  (start))

(defn stop []
  ;; stop is called before any code is reloaded
  ;; this is controlled by :before-load in the config
  (js/console.log "stop"))
