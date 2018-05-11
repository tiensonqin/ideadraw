(ns web.component
  (:refer-clojure :exclude [clone])
  (:require [web.history :as history]
            [web.paper :as p]
            [appkit.macros :refer [oset!]]))

(defn area-text
  [props opts]
  (p/area-text props opts))

(defn rect
  [m opts listeners]
  (when-let [c (p/rect m opts listeners)]
    (history/transact! [[:item/new c]])
    c))

(defn circle
  [m opts listeners]
  (when-let [c (p/circle m opts listeners)]
    (history/transact! [[:item/new c]])
    c))

(defn line
  [m opts listeners]
  (when-let [c (p/line m opts listeners)]
    (history/transact! [[:item/new c]])
    c))

(defn raster
  [m opts listeners]
  (when-let [c (p/raster m opts listeners)]
    (history/transact! [[:item/new c]])
    c))

(defn polygon
  [m opts listeners]
  (when-let [c (p/polygon m opts listeners)]
    (history/transact! [[:item/new c]])
    c))

(defn arrow
  [points color opts]
  (when-let [c (p/arrow points color opts)]
    (history/transact! [[:item/new c]])
    c))

(defn clone
  [path group-listeners text-listeners]
  (if (and (coll? path) (seq path))
    (let [items (set (doall (for [item path]
                              (p/clone-and-place item text-listeners))))
          grouped (p/group items)]
      (history/transact! (mapv (fn [x] [:item/new x]) items))
      (p/set-listeners grouped group-listeners)
      [grouped items])

    (when-let [c (p/clone-and-place path text-listeners)]
      (p/set-listeners c group-listeners)
      (history/transact! [[:item/new c]])
      c)))
