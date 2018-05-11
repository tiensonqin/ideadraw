(ns web.paper
  (:refer-clojure :exclude [clone + -])
  (:require [share.util :as util]
            [appkit.macros :refer [oget oset! oinc!]]))

(def Paper js/paper)
(def Path (oget Paper "Path"))
(def Point (oget Paper "Point"))
(def Rectangle (oget Paper "Rectangle"))
(def Size (oget Paper "Size"))
(def Group (oget Paper "Group"))
(def PointText (oget Paper "PointText"))
(def AreaText (oget Paper "AreaText"))
(def Color (oget Paper "Color"))
(def Layer (oget Paper "Layer"))
(def Tool (oget Paper "Tool"))

(defn klass
  [item]
  (oget item "_class"))

(defn point
  ([xy]
   (Point. (first xy) (second xy)))
  ([x y]
   (Point. x y)))

(defn path
  [opts]
  (Path. (clj->js opts)))

(def Line
  (oget Path "Line"))

(def PLine
  (oget Paper "Line"))

(def Circle
  (oget Path "Circle"))

(def PathRectangle
  (oget Path "Rectangle"))

(def RoundRectangle
  (oget Path "RoundRectangle"))

(def Arc
  (oget Path "Arc"))

(def Raster
  (oget Paper "Raster"))

(def RegularPolygon
  (oget Path "RegularPolygon"))

(defn size
  [width height]
  (Size. width height))

(defn group
  [paths]
  (Group. (clj->js paths)))

;; reference to reconciler (which is reference to the app state)
(defonce state (atom nil))

(defn set-bounds!
  [path bounds]
  (oset! path "bounds"
         (clj->js bounds)))

(defn set-bounds-height!
  [path height]
  (when-let [bounds (oget path "bounds")]
    (oset! bounds "height" height)))

(defn- get-bounds'
  [path]
  (let [bounds (oget path "bounds")]
    {:x (oget bounds "x")
     :y (oget bounds "y")
     :width (oget bounds "width")
     :height (oget bounds "height")}))

(defn get-bounds
  [path]
  (if path
    (if (instance? Group path)
      (if-let [items (seq (.getItems ^js path))]
        (get-bounds' (first items))
        (get-bounds' path))
      (get-bounds' path))))

(defn set-on-drag!
  [path & {:keys [stop? on-drag]
           :or {stop? false}}]
  (oset! path "onMouseDrag"
         (fn [e]
           (let [{:keys [selected-items
                         selected
                         dragging-arrow
                         current-action
                         select-mode?
                         grouped-selected]} (:draw @@state)]
             (cond
               select-mode?
               nil

               grouped-selected
               (let [position (oget grouped-selected "position")
                     x (oget position "x")
                     y (oget position "y")
                     x' (clojure.core/+ x (-> e (oget "delta") (oget "x")))
                     y' (clojure.core/+ y (-> e (oget "delta") (oget "y")))]
                 (oset! position "x" x')
                 (oset! position "y" y'))

               :else
               (cond
                 (or (= (klass path) "Arrow")
                     (not= current-action :add-connect))

                 (let [k (klass path)
                       grouped? (contains? #{"Group" "Arrow"} (klass path))]
                   (if on-drag
                     (on-drag path e))

                   (when (or (and grouped? selected)
                             (not grouped?))
                     (if stop? (util/stop e))

                     (let [position (oget path "position")
                           x (oget position "x")
                           y (oget position "y")
                           x' (clojure.core/+ x (-> e (oget "delta") (oget "x")))
                           y' (clojure.core/+ y (-> e (oget "delta") (oget "y")))]
                       (oset! position "x" x')
                       (oset! position "y" y'))))

                 :else
                 nil))))))

(defn set-on-drag-end!
  [path & {:keys [stop? on-drag-end]
           :or {stop? false}}]
  (oset! path "onMouseUp"
         (fn [e]
           ;; TODO:
           ;; (when-not (get @@state [:draw :selected-items])
           ;;   )
           (if stop? (util/stop e))
           (if on-drag-end
             (on-drag-end path e))
           )))


(defn set-listeners
  [path {:keys [on-click on-drag on-drag-end on-double-click on-mouse-move on-mouse-up on-mouse-enter on-mouse-leave on-arrow drag?]
         :or {drag? true}
         :as opts}]
  ;; on drag
  (if drag?
    (set-on-drag! path
                  :stop? true
                  :on-drag on-drag
                  :on-arrow on-arrow))

  ;; on drag end
  (if drag?
    (set-on-drag-end! path
                      :on-drag-end on-drag-end))

  (when on-mouse-move
    (oset! path "onMouseMove"
           (fn [e]
             (on-mouse-move path e))))

  (when on-mouse-enter
    (oset! path "onMouseEnter"
           (fn [e]
             (on-mouse-enter path e))))

  (when on-mouse-leave
    (oset! path "onMouseLeave"
           (fn [e]
             (on-mouse-leave path e))))

  (when on-mouse-up
    (oset! path "onMouseUp"
           (fn [e]
             (on-mouse-up path e))))

  ;; on click
  (when on-click
    (oset! path "onClick"
           (fn [e]
             (on-click path e)))
    (oset! path "onMouseDown"
           (fn [e]
             (on-click path e))))

  (if on-double-click
    (oset! path "onDoubleClick"
           (fn [e]
             (on-double-click path e)))))

;; compontents
(defn area-text
  [props opts]
  (let [path ^js (AreaText. (clj->js (dissoc props :bounds)))
        bounds (:bounds props)]
    (set-listeners path opts)
    (.setRectangle path (clj->js
                         {:point [(:x bounds) (:y bounds)]
                          :size [(:width bounds) (:height bounds)]}))
    (let [justification (get props :justification "center")]
      (.setJustification path justification))
    path))

(defn has-parent?
  [^js area-text]
  (not (instance? Layer (.getParent area-text))))

(defn shape
  [path {:keys [style size]
         :or {style {:fillColor "#FFF"}
              size 0}
         :as opts} listeners]
  (let [g (Group. (clj->js [path]))]
    (oset! g "style"
           (clj->js style))
    (set-listeners g listeners)
    g))

(defn rect
  [m opts listeners]
  (let [r (Rectangle. (clj->js m))
        path (RoundRectangle. r (Size. size size))]
    (shape path opts listeners)))

(defn raster
  [m opts listeners]
  (let [path ^js (Raster. (clj->js m))]
    (.scale path 0.5)
    (shape path opts listeners)))

(defn circle
  [m opts listeners]
  (let [path (Circle. (clj->js m))]
    (shape path opts listeners)))

(defn line
  [m opts listeners]
  (let [path (Line. (clj->js m))]
    (shape path opts listeners)))

(defn polygon
  [m opts listeners]
  (let [path (RegularPolygon. (clj->js m))]
    (shape path opts listeners)))

;; eof components

(defn set-selected-style
  [path]
  (oset! path "style"
         (clj->js {:shadowColor "rgba(66,133,244,.8)"
                   :shadowBlur 12})))

(defn remove-selected-style
  [path]
  (oset! path "style"
         (clj->js {:shadowColor "#FFF"
                   :shadowBlur 0})))

(defn delete
  [^js path]
  (if path
    (.remove path)))

(defn area-text-set-attrs!
  [^js text bounds]
  (.setRectangle text (clj->js
                       {:point [(:x bounds) (:y bounds)]
                        :size [(:width bounds) (:height bounds)]}))
  (.setJustification text "center"))

(defn- path-clone
  [^js path]
  (.clone path))

(defn attach-listeners
  [path group-listeners]
  (if (coll? path)
    (doseq [item path]
      (when (instance? Group path)
        (set-listeners item group-listeners)))
    (when (instance? Group path)
      (set-listeners path group-listeners))))

(defn clone-and-place
  [^js path text-listeners]
  (let [copy ^js (path-clone path)
        items (.getItems path)]
    ;; TODO: deep nested listeners
    ;; (set-listeners copy group-listeners)
    (doseq [[idx item] (map-indexed vector (.getItems copy))]
      (let [k (klass item)]
        (when (= k "AreaText")
          (set-listeners item (assoc text-listeners :drag? false))
          (area-text-set-attrs! item (get-bounds (aget items idx))))

        (when (contains?
               #{"PointText" "TextItem"}
               k)
          (set-listeners item text-listeners))))
    (oset! copy "position" (oget path "position"))
    (oinc! (oget copy "position") "x" 12)
    (oinc! (oget copy "position") "y" 12)
    copy))

(defn get-view
  []
  (oget Paper "view"))

(defn get-project
  []
  (oget (get-view) "_project"))

(defn get-active-layer
  [^js project]
  (.getActiveLayer project))

(defn get-layers
  []
  (.getLayers ^js (get-project)))

(defn get-items
  [^js path]
  (.getItems path))

(defn get-all-items
  []
  (let [layers (get-layers)]
    (->> (for [layer layers]
           (.getChildren ^js layer))
         (map js->clj)
         (flatten))))

(defn get-selected-items
  [all-items {:keys [x y width height] :as select-zone}]
  (let [x' x
        y' y]
    (set
     (filter
      (fn [item] (let [{:keys [x y]} (get-bounds item)]
                   (and
                    (>= x x')
                    (>= y y')
                    (<= x (clojure.core/+ x' width))
                    (<= y (clojure.core/+ y' height)))))
      all-items))))

(defn draw
  []
  (.draw ^js (get-view)))

;; vector
(defn +
  [p1 p2]
  (let [p1x (oget p1 "x")
        p1y (oget p1 "y")
        p2x (oget p2 "x")
        p2y (oget p2 "y")]
    (point (clojure.core/+ p1x p2x)
           (clojure.core/+ p1y p2y))))

(defn -
  [p1 p2]
  (let [p1x (oget p1 "x")
        p1y (oget p1 "y")
        p2x (oget p2 "x")
        p2y (oget p2 "y")]
    (point (clojure.core/- p1x p2x)
           (clojure.core/- p1y p2y))))

(defn center
  [p1 p2]
  (let [p1x (oget p1 "x")
        p1y (oget p1 "y")
        p2x (oget p2 "x")
        p2y (oget p2 "y")]
    (point (/ (clojure.core/+ p1x p2x) 2)
           (/ (clojure.core/+ p1y p2y) 2))))

(defn smooth
  [points]
  (let [start-point (first points)
        end-point (last points)
        points (loop [points (next points)
                      last-point start-point
                      last-angle 0
                      acc [start-point]]
                 (let [current (first points)
                       [v angle length angle'] (if (and current last-point)
                                                 (let [v ^js (- current last-point)
                                                       angle (.getAngle v)]
                                                   [v
                                                    angle
                                                    (.getLength v)
                                                    (util/abs (clojure.core/- angle last-angle))])
                                                 [nil 0 0 0])]
                   (cond
                     (or (<= (util/abs (clojure.core/- angle' 90)) 20)
                         (<= (util/abs (clojure.core/- angle' 270)) 20))
                     (recur (next points)
                            current
                            angle
                            (vec (conj acc current)))

                     (seq points)
                     (recur (next points)
                            current
                            angle
                            acc)

                     :else
                     (vec (distinct (conj acc end-point))))))]
    points))

(defn ->xy
  [point]
  [(oget point "x")
   (oget point "y")])

(defn almost-line?
  ([points]
   (almost-line? points 80))
  ([points threshold]
   (and (seq points)
        (let [line ^js (PLine. (first points) (last points))]
          (every? (fn [point]
                    (let [distance (.getDistance line point)]
                      (<= distance threshold)))
                  (next points))))))

(defn arrow
  [points color opts]
  (let [start-end [(first points)
                   (last points)]
        head-length 10
        head-angle 150
        [start end] start-end
        tail-line (Line. start end)
        tail-vector (- end start)
        head-line ^js (.normalize ^js tail-vector head-length)
        p1 (let [p (Path. (clj->js start-end))]
             (oset! p "strokeWidth" 3)
             p)
        p2 (let [p (Path. (clj->js
                           [(+ end (.rotate head-line head-angle))
                            end
                            (+ end (.rotate head-line (clojure.core/- head-angle)))]))]
             (oset! p "strokeWidth" 3)
             p)
        arrow (Group. (clj->js [p1 p2]))]
    (delete tail-line)
    (oset! arrow "_class" "Arrow")
    (oset! arrow "strokeColor" color)
    (oset! arrow "start-end" (clj->js start-end))
    (set-listeners arrow opts)
    arrow))

(defn set-visible
  [path visible?]
  (oset! path "visible" visible?))

(defn get-group-path
  [group]
  (and (instance? Group group)
       (first (.getItems ^js group))))

(defn recompute-start-end
  [from to arr p e]
  (let [from? (= from p)
        delta (oget e "delta")
        [start end] (oget arr "start-end")
        [start end] (if from?
                      [(+ start delta)
                       end]
                      [start
                       (+ end delta)])

        from-path ^js (get-group-path from)
        to-path ^js (get-group-path to)
        center-point (center start end)
        from-nearest-point  (.getNearestPoint from-path center-point)
        to-nearest-point  (.getNearestPoint to-path center-point)]
    (let [v ^js (- from-nearest-point to-nearest-point)
          angle (.getAngle v)]
      (cond
        (and (>= angle -30) (<= angle 30))
        [(- from-nearest-point (point 9 0))
         (+ to-nearest-point (point 12 0))]

        (and (>= angle -90) (<= angle -30) )
        [(+ from-nearest-point (point 0 9))
         (- to-nearest-point (point 0 9))]

        (and (>= angle 30) (<= angle 90) )
        [(- from-nearest-point (point 0 9))
         (+ to-nearest-point (point 0 9))]

        :else
        [(+ from-nearest-point (point 9 0))
         (- to-nearest-point (point 12 0))]))))

(defn recompute-connection
  [state {:keys [from to] :as connection} path e]
  (let [arr (:arrow connection)
        start-end (clj->js (recompute-start-end from to arr path e))]
    (oset! arr "selected" true)
    (delete arr)
    (let [new-arrow (arrow start-end "#666" (:group-listeners state))]
      {:from from
       :to to
       :arrow new-arrow})))

;; ideas from http://matthiasberth.com/tech/stable-zoom-and-pan-in-paperjs
(defn get-center
  []
  (oget (get-view) "center"))

;; TODO: FIX this, https://github.com/jquery/jquery-mousewheel/blob/33dc8f1090da2eaadbca8e782965d7fd6c28db42/jquery.mousewheel.js
(def pan-factor 1)
(defn new-center
  [old-center delta-x delta-y factor]
  (let [offset (point delta-x delta-y)
        offset (.multiply offset factor)]
    (.add old-center offset)))

(defn set-center
  ([delta-x delta-y]
   (set-center delta-x delta-y pan-factor))
  ([delta-x delta-y factor]
   (let [old-center (get-center)
         center (new-center old-center delta-x delta-y factor)]
     (oset! (get-view) "center" center)
     center)))

(def max-zoom 5)
(def min-zoom 0.1)
(def zoom-factor 1.05)
(defn get-zoom
  []
  (oget (get-view) "zoom"))

(defn set-zoom
  [e]
  (let [mouse-position (point (oget e "offsetX")
                              (oget e "offsetY"))
        view-position ^js (.viewToProject ^js (get-view) mouse-position)
        delta-y (oget e "deltaY")
        old-zoom (get-zoom)
        old-center ^js (get-center)
        new-zoom (cond
                   (< delta-y 0)
                   (max min-zoom (* old-zoom zoom-factor))

                   (> delta-y 0)
                   (min max-zoom (/ old-zoom zoom-factor))

                   :else
                   old-zoom)
        beta (/ old-zoom new-zoom)
        pc ^js (.subtract view-position old-center)
        offset (-> view-position
                   (.subtract (.multiply pc beta))
                   (.subtract old-center))]
    (oset! (get-view) "zoom" new-zoom)
    (oset! (get-view) "center"
           (.add old-center offset))))

(defn zoom-in
  []
  (let [new-zoom (min max-zoom (* (get-zoom) zoom-factor))]
    (oset! (get-view) "zoom" new-zoom)))

(defn zoom-out
  []
  (let [new-zoom (max min-zoom (/ (get-zoom) zoom-factor))]
    (oset! (get-view) "zoom" new-zoom)))

(defn clear
  []
  (.clear ^js (get-project)))

(defn restore
  [path]
  (when path
    (if-let [layer (get-active-layer (.getProject ^js path))]
      (.addChild ^js layer path))))

(defn export-json
  []
  (.exportJSON ^js (get-project)))

(defn import-json
  [json]
  (.importJSON ^js (get-project) json))

(defn json->edn
  [json]
  (-> json
      (js/JSON.parse)
      (js->clj :keywordize-keys true)))

(defn insert-children
  [^js parent index ^js children]
  (.insertChildren parent index children))

(defn un-group
  [^js group]
  (let [children ^js (.removeChildren group)]
    (insert-children (oget group "parent")
                     (oget group "index")
                     children)
    (delete group)
    children))
