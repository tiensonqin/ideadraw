(ns web.actions
  (:require [appkit.citrus :as citrus]
            [web.component :as dc]
            [web.events :as events]
            [web.paper :as p]
            [goog.dom :as gdom]))

(defn- add-f
  [path cursor]
  (citrus/dispatch-sync! :draw/set-input-cursor cursor)
  (citrus/dispatch! :draw/set-selected
                    path)
  (citrus/dispatch! :draw/set-bounds
                    (p/get-bounds path))


  (citrus/dispatch! :draw/clear-action)
  (citrus/dispatch! :draw/enable-input?))

(defn add-rectangle
  [{:keys [x y] :as cursor} move-vector]
  (let [g (dc/rect {:point (if move-vector
                             (p/+ (p/point [x y]) move-vector)
                             (p/point [x y]))
                    :size (p/size 180 90)}
            {:size 2}
            events/group-listeners)]
    (add-f g cursor)))

(defn add-circle
  [{:keys [x y] :as cursor} move-vector]
  (let [g (dc/circle {:center (if move-vector
                                (p/+ (p/point [x y]) move-vector)
                                (p/point [x y]))
                      :radius 90
                      :shadowOffset 2}
            nil
            events/group-listeners)]
    (add-f g cursor)))

(defn add-polygon
  [{:keys [x y] :as cursor} move-vector]
  (let [g (dc/polygon {:center (if move-vector
                                 (p/+ (p/point [x y]) move-vector)
                                 (p/point [x y]))
                       :radius 90
                       :sides 3
                       :shadowOffset 2}
            nil
            events/group-listeners)]
    (add-f g cursor)))

(defn add-line
  []
  (citrus/dispatch! :draw/set-current-action :add-line))

(defn add-arrow
  []
  (citrus/dispatch! :draw/set-current-action :add-connect))

(defn pencil
  []
  (citrus/dispatch! :draw/set-current-action :pencil))

(defn upload-picture
  []
  (.click ^js (gdom/getElement "photo_upload")))

(defn add-text
  []
  (citrus/dispatch! :draw/set-current-action :add-text))
