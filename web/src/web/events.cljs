(ns web.events
  (:require [rum.core :as rum]
            [appkit.citrus :as citrus]
            [share.util :as util]
            [clojure.string :as str]
            [appkit.macros :refer [oget oset!]]
            [web.paper :as p]))

(def group-listeners
  {:on-click
   (fn [path e]
     (citrus/dispatch! :draw/set-selected path))

   :on-mouse-enter
   (fn [path e]
     (citrus/dispatch-sync! :draw/path-enter path e))

   :on-mouse-leave
   (fn [path e]
     (citrus/dispatch-sync! :draw/path-leave path e))

   :on-drag
   (fn [path e]
     ;; select-mode not on
     (citrus/dispatch! :draw/drag-path path e))

   :on-drag-end
   (fn [path e]
     (citrus/dispatch! :draw/drag-end path e))})

(def text-listeners
  {:on-double-click
   (fn [^js path e]
     (citrus/dispatch-sync! :draw/set-latest-text
                            (.getContent path))
     (citrus/dispatch-sync! :draw/set-current-text-path
                            path)
     (p/set-visible path false)
     (citrus/dispatch! :draw/enable-input?))})

(def arrow-listeners
  {:on-click
   (fn [path e]
     (citrus/dispatch! :draw/set-selected path))
   :on-drag-end
   (fn [path e]
     (let [[x y] (p/->xy (oget e "delta"))]
       (when (or (not (zero? x))
                 (not (zero? y)))
         ;; compute new start-end
         (when (= (p/klass path) "Arrow")
           (citrus/dispatch-sync! :draw/arrow-recompute-start-end path (oget e "delta")))
         (citrus/dispatch! :draw/set-selected path))))})

(def resize-direction-changed
  (fn [direction]
    (citrus/dispatch!
     :draw/set-resize-direction direction)))

(def expand-listeners
  {:arrow-start (fn [vertex start end]
                  {:on-drag (fn [path e]
                              (citrus/dispatch!
                               :draw/resize-connect
                               e
                               {:start (oget e "point")
                                :end end}
                               arrow-listeners))
                   :on-drag-end (fn [path e]
                                  (p/delete (first @vertex))
                                  (p/delete (second @vertex))
                                  (citrus/dispatch! :draw/disable-resize-arrow-mode?))})
   :arrow-end (fn [vertex start end]
                {:on-drag (fn [path e]
                            (let [[x y] (p/->xy (oget e "delta"))]
                              (when (or (not (zero? x))
                                        (not (zero? y)))
                                (citrus/dispatch!
                                 :draw/resize-connect
                                 e
                                 {:start start
                                  :end (oget e "point")}
                                 arrow-listeners))))
                 :on-drag-end (fn [path e]
                                (let [[x y] (p/->xy (oget e "delta"))]
                                  (when (or (not (zero? x))
                                            (not (zero? y)))
                                    (p/delete (first @vertex))
                                    (p/delete (second @vertex))
                                    (citrus/dispatch! :draw/disable-resize-arrow-mode?))))})

   :top-left (fn [path vertex]
               {:on-drag (fn [vertice e]
                           (let [bounds (p/get-bounds path)
                                 {:keys [x y width height]} bounds
                                 [dx dy] (p/->xy (oget e "delta"))
                                 width (- width dx)
                                 height (- height dy)
                                 x (+ x dx)
                                 y (+ y dy)]
                             (when (and (>= width 20)
                                        (>= height 20))
                               ;; set rect bounds
                               (p/set-bounds! path
                                              {:x x
                                               :y y
                                               :width width
                                               :height height})

                               ;; set other 2 vertices bounds
                               (p/set-bounds! (nth @vertex 1)
                                              {:x (- x 4)
                                               :y (- (+ y height) 4)
                                               :width 8
                                               :height 8})

                               (p/set-bounds! (nth @vertex 2)
                                              {:x (- (+ x width) 4)
                                               :y (- y 4)
                                               :width 8
                                               :height 8})
                               )))
                :on-mouse-enter (fn [path e]
                                  ;; set resize-direction
                                  (resize-direction-changed :nwse))
                :on-mouse-leave (fn [path e]
                                  ;; set resize-direction
                                  (resize-direction-changed ""))})

   :bottom-left  (fn [path vertex]
                   {:on-drag (fn [vertice e]
                               (let [bounds (p/get-bounds path)
                                     {:keys [x y width height]} bounds
                                     [dx dy] (p/->xy (oget e "delta"))
                                     width (- width dx)
                                     height (+ height dy)
                                     x (+ x dx)]
                                 (when (and (>= width 20)
                                            (>= height 20))
                                   (p/set-bounds! path
                                                  {:x x
                                                   :y y
                                                   :width width
                                                   :height height})

                                   ;; set other 2 vertices bounds
                                   (p/set-bounds! (nth @vertex 0)
                                                  {:x (- x 4)
                                                   :y (- y 4)
                                                   :width 8
                                                   :height 8})

                                   (p/set-bounds! (nth @vertex 3)
                                                  {:x (+ (- x 4) width)
                                                   :y (+ (- y 4) height)
                                                   :width 8
                                                   :height 8})
                                   )))
                    :on-mouse-enter (fn [path e]
                                      ;; set resize-direction
                                      (resize-direction-changed :nesw))
                    :on-mouse-leave (fn [path e]
                                      ;; set resize-direction
                                      (resize-direction-changed ""))})

   :top-right    (fn [path vertex]
                   {:on-drag (fn [vertice e]
                               (let [bounds (p/get-bounds path)
                                     {:keys [x y width height]} bounds
                                     [dx dy] (p/->xy (oget e "delta"))
                                     width (+ width dx)
                                     height (- height dy)
                                     y (+ y dy)]
                                 (when (and (>= width 20)
                                            (>= height 20))
                                   (p/set-bounds! path
                                                  {:x x
                                                   :y y
                                                   :width width
                                                   :height height})

                                   ;; set other 2 vertices bounds
                                   (p/set-bounds! (nth @vertex 0)
                                                  {:x (- x 4)
                                                   :y (- y 4)
                                                   :width 8
                                                   :height 8})

                                   (p/set-bounds! (nth @vertex 3)
                                                  {:x (+ (- x 4) width)
                                                   :y (+ (- y 4) height)
                                                   :width 8
                                                   :height 8}))))
                    :on-mouse-enter (fn [path e]
                                      ;; set resize-direction
                                      (resize-direction-changed :nesw))
                    :on-mouse-leave (fn [path e]
                                      ;; set resize-direction
                                      (resize-direction-changed ""))})

   :bottom-right (fn [path vertex]
                   {:on-drag (fn [vertice e]
                               (let [bounds (p/get-bounds path)
                                     {:keys [x y width height]} bounds
                                     [dx dy] (p/->xy (oget e "delta"))
                                     width (+ width dx)
                                     height (+ height dy)]
                                 (when (and (>= width 20)
                                            (>= height 20))
                                   (p/set-bounds! path
                                                  {:x x
                                                   :y y
                                                   :width width
                                                   :height height})

                                   ;; set other 2 vertices bounds
                                   (p/set-bounds! (nth @vertex 1)
                                                  {:x (- x 4)
                                                   :y (- (+ y height) 4)
                                                   :width 8
                                                   :height 8})

                                   (p/set-bounds! (nth @vertex 2)
                                                  {:x (+ (- x 4) width)
                                                   :y (- y 4)
                                                   :width 8
                                                   :height 8}))))
                    :on-mouse-enter (fn [path e]
                                      ;; set resize-direction
                                      (resize-direction-changed :nwse))
                    :on-mouse-leave (fn [path e]
                                      ;; set resize-direction
                                      (resize-direction-changed ""))})})

(def callbacks
  {:group-listeners group-listeners
   :text-listeners text-listeners
   :arrow-listeners arrow-listeners
   :expand-listeners expand-listeners
   :resize-direction-changed resize-direction-changed})
