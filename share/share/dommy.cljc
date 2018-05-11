(ns share.dommy
  #?(:cljs (:require [dommy.core :as d])))

(def bounding-client-rect #?(:cljs d/bounding-client-rect))

(defn by-id [id]
  #?(:cljs (d/by-id id)))

(defn sel
  ([data]
   (sel nil data))
  ([base data]
   #?(:cljs (d/sel (or base js/document) data))))

(defn sel1
  ([data]
   (sel1 nil data))
  ([base data]
   #?(:cljs (d/sel1 (or base js/document) data))))

(def attr #?(:cljs d/attr))

(def parent #?(:cljs d/parent))

(def children #?(:cljs d/children))

(def has-class? #?(:cljs d/has-class?))
(def toggle-class! #?(:cljs d/toggle-class!))
(def add-class! #?(:cljs d/add-class!))
(def set-class! #?(:cljs d/set-class!))
(def remove-class! #?(:cljs d/remove-class!))

(def closest #?(:cljs d/closest))

(def set-px! #?(:cljs d/set-px!))
(def set-style! #?(:cljs d/set-style!))
(def remove-style! #?(:cljs d/remove-style!))
(def set-attr! #?(:cljs d/set-attr!))

(def hidden? #?(:cljs d/hidden?))
(def show! #?(:cljs d/show!))
(def hide! #?(:cljs d/hide!))

(def create-element #?(:cljs d/create-element))

(def insert-before! #?(:cljs d/insert-before!))
(def insert-after! #?(:cljs d/insert-after!))
(def append! #?(:cljs d/append!))
(def remove! #?(:cljs d/remove!))

(def listen! #?(:cljs d/listen!))
(def unlisten! #?(:cljs d/unlisten!))
