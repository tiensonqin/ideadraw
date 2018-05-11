(ns share.mixins
  (:require [rum.core :as rum]
            [appkit.citrus :as citrus]
            [share.util :as util]
            #?(:cljs ["prop-types" :as prop-types])
            #?(:cljs [goog.dom :as dom]))
  #?(:cljs (:import [goog.events EventHandler])))

(defn detach
  "Detach all event listeners."
  [state]
  #?(:cljs (some-> state ::event-handler .removeAll)
     :clj nil))

(defn listen
  "Register an event `handler` for events of `type` on `target`."
  [state target type handler & [opts]]
  #?(:cljs (when-let [event-handler (::event-handler state)]
             (.listen event-handler target (name type) handler (clj->js opts)))
     :clj nil))

(def event-handler-mixin
  "The event handler mixin."
  {:will-mount
   (fn [state]
     (assoc state ::event-handler #?(:cljs (EventHandler.)
                                     :clj nil)))
   :will-unmount
   (fn [state]
     (detach state)
     (dissoc state ::event-handler))})

(def scroll-to-bottom
  {:did-update (fn [state]
                 #?(:cljs
                    (if-let [node (rum/dom-node state)]
                      (set! (.-scrollTop node) (.-scrollHeight node))))
                 state)})

(defn esc-listeners
  [state open? & {:keys [on-close stop?]}]
  #?(:clj
     nil

     :cljs
     (let [node (rum/dom-node state)]
       (when open?
         (listen state js/window "click"
                 (fn [e]
                   ;; If the click target is outside of current node
                   (if (and (or (nil? stop?)
                                (false? @stop?))
                            (not (dom/contains node (.. e -target))))
                     (on-close e))))

         (listen state js/window "keydown"
                 (fn [e]
                   (case (.-keyCode e)
                     ;; Esc
                     27 (on-close e)
                     nil)))))))

(defn event-mixin
  [attach-listeners]
  (merge
   event-handler-mixin
   {:did-mount (fn [state]
                 (attach-listeners state)
                 state)
    :did-remount (fn [old-state new-state]
                   (detach old-state)
                   (attach-listeners new-state)
                   new-state)}))

(defn dispatch-on-mount
  ([event]
   (dispatch-on-mount event identity))
  ([event data-fn]
   {:did-mount
    (fn [state]
      (util/debug :did-mount {:event event})
      (citrus/dispatch! event (data-fn (first (:rum/args state))))
      state)
    :did-remount
    (fn [old state]
      (util/debug :did-remount {:event event})
      (when (not= (:rum/args old) (:rum/args state))
        (citrus/dispatch! event (data-fn (first (:rum/args state)))))
      state)}))

(defn form []
  {:wrap-render (fn [render-fn]
                  (fn [state]
                    (render-fn (assoc state ::form-data {}))))})
