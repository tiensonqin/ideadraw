(ns web.handlers.draw
  (:require [share.util :as util]
            [appkit.macros :refer [oget oset!]]
            [share.dommy :as dommy]
            [dommy.core :as d]
            [clojure.set :as set]
            [web.component :as dc]
            [web.history :as history]
            [web.paper :as p]))

(def vertex (atom []))

(defn clear-vertex
  []
  (when-let [vertices @vertex]
    (doseq [vertice vertices]
      (p/delete vertice))
    (reset! vertex nil)))

(def handlers
  {:draw/set-current-action
   (fn [state action]
     {:state {:current-action action
              :latest-text nil
              :dragging-arrow nil}
      :dispatch [:draw/remove-selected-style]})

   :draw/remove-selected-style
   (fn [state]
     (when-let [old-selected (:selected state)]
       (p/remove-selected-style old-selected))
     {:state {:selected nil}})

   :draw/clear-action
   (fn [state]
     {:state {:current-action nil}})

   :draw/set-latest-text
   (fn [state v]
     {:state {:latest-text v}})

   :draw/set-current-text-path
   (fn [state path]
     {:state {:current-text-path path
              :input-mode? true}})

   :draw/add-latest-text
   (fn [state listeners]
     (cond
       ;; update
       (and (:current-text-path state)
            (:latest-text state))
       (let [path ^js (:current-text-path state)
             rows (:textarea-row-count state)]
         (let [parent (.getParent path)
               has-parent? (p/has-parent? path)
               {:keys [x y width height] :as bounds} (if has-parent?
                                                       (p/get-bounds parent)
                                                       (p/get-bounds path))
               _ (p/delete path)
               text (dc/area-text
                     (if has-parent?
                       {:content (:latest-text state)
                        :fontSize 16
                        :fontFamily "Roboto"
                        :leading (* 16 1.5)
                        :bounds {:x (+ x 12)
                                 :y (+ y
                                       (/ (- height (* rows 24)) 2))
                                 :width (- width 24)
                                 :height (* rows 24)}}
                       {:content (:latest-text state)
                        :justification "left"
                        :fontSize 16
                        :fontFamily "Roboto"
                        :leading (* 16 1.5)
                        :bounds {:x x
                                 :y y
                                 :width width
                                 :height height}})
                     (if has-parent?
                       (assoc listeners :drag? false)
                       listeners))]
           (when-let [selected (:selected state)]
             (.addChild ^js selected text))))

       ;; wrap by group [rect, ...]
       (and (:selected state)
            (:latest-text state)
            (:selected-group-bounds state))
       (let [bounds (p/get-bounds (:selected state))
             rows (:textarea-row-count state)
             {:keys [x y width height]} bounds
             text (dc/area-text
                   {:content (:latest-text state)
                    :fontSize 16
                    :fontFamily "Roboto"
                    :leading (* 16 1.5)
                    :bounds {:x (+ x 12)
                             :y (+ y
                                   (/ (- height (* rows 24)) 2))
                             :width (- width 24)
                             :height (* rows 24)}}
                   (assoc listeners :drag? false))]
         (.addChild ^js (:selected state) text))

       ;; only text
       (and (:latest-text state)
            (:selected-group-bounds state))
       (dc/area-text
        (let [{:keys [x y]} (:cursor state)
              point (p/+ (p/point [x y]) (:move-vector state))]
          {:content (:latest-text state)
           :fontSize 16
           :fontFamily "Roboto"
           :leading (* 16 1.5)
           :justification "left"
           :bounds {:x (+ (oget point "x") 12)
                    :y (+ (oget point "y") 12)
                    :width 200
                    :height 80}})
        listeners)

       :else
       nil)
     {:state state})

   :draw/enable-expand?
   (fn [state path]
     (clear-vertex)

     (let [{:keys [expand-listeners resize-direction-changed]} (:callbacks state)]
       (cond
         (:grouped-selected state)
         {:state state}

         ;; arrow
         (= (p/klass path) "Arrow")
         (let [[start end] (oget path "start-end")
               vertices [(p/circle {:center start
                                    :radius 6
                                    :fillColor "#FFFFFF"
                                    :shadowColor "#999"
                                    :shadowBlur 12
                                    :shadowOffset [3 3]}
                           nil
                           ((get expand-listeners :arrow-start) vertex start end))
                         (p/circle {:center end
                                    :radius 6
                                    :fillColor "#FFFFFF"
                                    :shadowColor "#999"
                                    :shadowBlur 12
                                    :shadowOffset [3 3]}
                           nil
                           ((get expand-listeners :arrow-end) vertex start end))]]
           (reset! vertex vertices)
           {:state {:vertices vertices
                    :selected path}})

         ;; rectangle
         (= (p/klass path) "Group")
         (let [bounds (p/get-bounds path)
               {:keys [x y width height]} bounds
               vertices [(p/rect {:point (p/point (- x 4) (- y 4))
                                  :size (p/size 8 8)}
                           {:style {:fillColor "rgba(66,133,244,.8)"}}
                           ((get expand-listeners :top-left) path vertex))

                         (p/rect {:point (p/point (- x 4) (- (+ y height) 4))
                                  :size (p/size 8 8)}
                           {:style {:fillColor "rgba(66,133,244,.8)"}}
                           ((get expand-listeners :bottom-left) path vertex))

                         (p/rect {:point (p/point (- (+ x width) 4) (- y 4))
                                  :size (p/size 8 8)}
                           {:style {:fillColor "rgba(66,133,244,.8)"}}
                           ((get expand-listeners :top-right) path vertex))

                         (p/rect {:point (p/point (- (+ x width) 4) (- (+ y height) 4))
                                  :size (p/size 8 8)}
                           {:style {:fillColor "rgba(66,133,244,.8)"}}
                           ((get expand-listeners :bottom-right) path vertex))]]
           (reset! vertex vertices)

           {:state {:expand? true
                    :vertices vertices
                    :selected path}})

         :else
         {:state {:selected path}})))

   :draw/disable-expand?
   (fn [state]
     (clear-vertex)
     {:state {:expand? false
              :vertices nil}})

   :draw/disable-resize-arrow-mode?
   (fn [state]
     {:state {:resize-arrow-mode? false
              :vertices nil}})

   :draw/enable-input?
   (fn [state]
     {:state {:input-mode? true}})

   :draw/disable-input?
   (fn [state]
     {:state {:input-mode? false}})

   :draw/set-bounds
   (fn [state new-bounds set?]
     (when-let [group (:selected state)]
       (when-not (false? set?)
         (p/set-bounds! group new-bounds)))
     {:state {:selected-group-bounds new-bounds}})

   :draw/set-resize-direction
   (fn [state v]
     {:state {:resize-direction v}})

   :draw/clear-resize
   (fn [state]
     {:state {:resize-direction nil}})

   :draw/set-selected
   (fn [state path]
     (if (nil? (:vertices state))
       (clear-vertex))
     (p/set-selected-style path)

     (if (and (:selected state) (not= path (:selected state)))
       (do
         (p/remove-selected-style (:selected state))
         (clear-vertex)
         {:state {:selected path
                  :vertices nil}
          :dispatch [:draw/enable-expand? path]})
       {:state {:selected path}
        :dispatch [:draw/enable-expand? path]}))

   :draw/delete-selected-path
   (fn [state]
     (cond
       (:input-mode? state)
       {:state state}

       (seq (:selected-items state))
       (let [items (set (:selected-items state))
             connectors (:connectors state)
             arrows (filter (fn [x]
                              (or
                               (contains? items (:from x))
                               (contains? items (:to x))))
                            connectors)
             connectors (vec
                         (filter
                          (fn [x]
                            (and
                             (not (contains? items (:from x)))
                             (not (contains? items (:to x)))
                             (not (contains? items (:arrow x)))))
                          connectors))]
         (doseq [{:keys [arrow]} arrows]
           (p/delete arrow))
         (doseq [item items]
           (p/delete item))
         (history/transact! (->> (mapv :arrow arrows)
                                 (concat items)
                                 (remove nil?)
                                 (map (fn [x]
                                        [:item/delete x]))
                                 (vec)))
         {:state {:selected-items nil
                  :selected-group-bounds nil
                  :all-items nil
                  :connectors connectors}})

       :else
       (let [path (:selected state)]
         (p/delete path)
         (history/transact! [[:item/delete path]])
         {:state {:connectors (when-let [col (seq (:connectors state))]
                                (let [arrows (filter
                                              (fn [x] (or
                                                       (= (:from x) path)
                                                       (= (:to x) path)))
                                              col)]
                                  (doseq [{:keys [arrow]} arrows]
                                    (p/delete arrow)))
                                (vec (filter
                                      (fn [x] (and
                                               (not= (:from x) path)
                                               (not= (:to x) path)
                                               (not= (:arrow x) path)))
                                      col)))
                  :selected nil
                  :selected-group-bounds nil}
          :dispatch [:draw/disable-expand?]})))

   :draw/copy-selected-path
   (fn [state]
     (cond
       (:grouped-selected state)
       {:state {:copy (:grouped-selected state)}}

       (:selected state)
       {:state {:copy (:selected state)}}

       :else
       {:state state}))

   :draw/paste-selected-path
   (fn [state group-listeners text-listeners]
     (let [copy (:copy state)]
       (cond
         (:grouped-selected state)                     ; grouped selection
         (let [grouped (:grouped-selected state)
               {:keys [group-listeners text-listeners]} (:callbacks state)
               children (p/un-group grouped)
               _ (doseq [child children]
                   (p/set-listeners child group-listeners)
                   (p/remove-selected-style child))
               [grouped new-copy] (dc/clone (js->clj children)
                                            group-listeners
                                            text-listeners)]

           (doseq [item (p/get-items grouped)]
             (p/set-selected-style item))
           {:state {:copy new-copy
                    :selected-items (vec new-copy)
                    :grouped-copy grouped
                    :grouped-selected grouped}})

         copy
         (let [new-path (dc/clone copy
                                  group-listeners
                                  text-listeners)]
           (p/remove-selected-style copy)
           {:state {:copy new-path
                    :selected new-path}
            :dispatch [:draw/enable-expand? new-path]})

         :else
         {:state state})))

   :draw/mouse-down
   (fn [state e]
     (let [point (oget e "point")
           action (:current-action state)
           cursor (let [event (oget e "event")]
                    {:x (oget event "clientX")
                     :y (oget event "clientY")})]
       (cond
         (and (:grouped-selected state)
              (not-any? #(.contains % point)
                        (.getChildren ^js (:grouped-selected state))))
         {:state state
          :dispatch [:draw/clear-grouped-selected]
          }

         (and (:grouped-copy state)
              (not-any? #(.contains % point)
                        (.getChildren ^js (:grouped-copy state))))
         {:state state
          :dispatch [:draw/clear-grouped-copy]}

         ;; vertices
         (and (:vertices state)
              (some (fn [x] (.contains x point)) (:vertices state)))
         {:state state}

         (or (= action :add-text) (:input-mode? state))
         {:state {:input-mode? true
                  :selected-group-bounds (merge
                                          (:cursor state)
                                          {:width 180
                                           :height 90})
                  :current-action nil
                  :selected-items nil}}

         (:selected state)
         (do
           (if (.contains (:selected state)
                          point)
             {:state state}
             (do
               (p/remove-selected-style (:selected state))
               {:state {:selected nil
                        :selected-items nil}
                :dispatch [:draw/disable-expand?]})))

         (= action :pencil)
         {:state {:selected nil
                  :selected-items nil
                  :pencil-path (let [path (p/Path. (clj->js [point]))]
                                 (oset! path "strokeColor" "#222")
                                 path)}
          :dispatch [:draw/disable-expand?]}

         (= action :add-line)
         {:state {:selected nil
                  :selected-items nil}
          :dispatch [:draw/disable-expand?]}

         (= action :add-connect)
         {:state {:selected nil
                  :selected-items nil}
          :dispatch [:draw/disable-expand?]}

         ;; select
         :else
         (do
           (when-let [selected-items (:selected-items state)]
             (doseq [item selected-items]
               (p/remove-selected-style item)))
           {:state {:start-point point
                    :cursor cursor}
            :dispatch [[:draw/recompute-select-area]
                       [:draw/disable-expand?]]}))))

   :draw/set-input-cursor
   (fn [state cursor]
     {:state {:input-cursor cursor}})

   :draw/mouse-move
   (fn [state e]
     (util/stop e)
     (cond-> {:state {:cursor {:x (oget e "clientX")
                               :y (oget e "clientY")}}}
       (:start-point state)
       (assoc :dispatch [:draw/recompute-select-area])))

   :draw/path-enter
   (fn [state path e]
     (if (= :add-connect (:current-action state))
       (if (and (:connection-from state)
                (:dragging-arrow state)
                (not= path (:connection-from state)))
         {:state {:enter-path false
                  :connection-from nil
                  :leave-point nil
                  :enter-point (oget e "point")
                  :connection-to nil}
          :dispatch [:draw/add-connection (:connection-from state) path e]}
         {:state {:connection-from path
                  :enter-path true}})

       {:state state}))

   :draw/path-leave
   (fn [state path e]
     (if (= :add-connect (:current-action state))
       {:state {:connection-from path
                :enter-path false
                :enter-point nil
                :leave-point (oget e "point")}}
       {:state {:enter-point nil}}))

   :draw/resize-connect
   (fn [state e {:keys [start end]} group-listeners]
     (let [old-arrow (or (:dragging-arrow state)
                         (when-let [selected (:selected state)]
                           (and (= "Arrow" (p/klass selected))
                                selected)))
           arrow (p/arrow [start end] "#666" group-listeners)]
       (if old-arrow (p/delete old-arrow))
       {:state {:dragging-arrow arrow
                :resize-arrow-mode? true
                :selected arrow}}))

   :draw/mouse-drag
   (fn [state e group-listeners]
     (let [action (:current-action state)
           state (assoc state :group-listeners group-listeners)
           point (oget e "point")]
       (cond
         (true? (:connection-rendered state))
         {:state state}

         (and (= action :add-connect)
              (:connection-from state)
              (true? (:enter-path state)))
         {:state state}

         (= action :add-line)
         (let [start-point (oget e "downPoint")
               _ (when-let [line (:dragging-line state)] (p/delete line))
               line (dc/line {:from start-point
                              :to point
                              :strokeWidth 2.5
                              :strokeColor "#666"} nil group-listeners)]
           {:state {:dragging-line line}})

         (= action :pencil)
         (do
           (.add ^js (:pencil-path state) point)
           {:state state})

         (= action :add-connect)
         (let [point (if-let [enter-point (:enter-point state)]
                       enter-point
                       (oget e "point"))
               start-point (if-let [leave-point (:leave-point state)]
                             leave-point
                             (oget e "downPoint"))
               dragging-points (:dragging-points state)
               arrow (if dragging-points
                       (p/arrow dragging-points "#666" group-listeners))]

           (when-let [old-arrow (:dragging-arrow state)]
             (p/delete old-arrow))
           {:state {:dragging-arrow arrow
                    :dragging-points (if dragging-points
                                       (vec (conj dragging-points point))
                                       [start-point point])}})

         :else
         {:state state})))

   :draw/mouse-up
   (fn [state e]
     (let [action (:current-action state)]
       (cond
         (= action :add-connect)
         {:state {:dragging-arrow nil
                  :dragging-points nil
                  :current-action nil
                  :connection-rendered false}}

         (= action :add-line)
         {:state {:dragging-line nil
                  :current-action nil}}

         (= action :pencil)
         (let [path (:pencil-path state)]
           (.simplify ^js path)
           {:state {:pencil-path nil
                    :current-action nil}})


         :else
         (let [state (if (and (:select-mode? state)
                              (seq (:selected-items state)))
                       (let [grouped (p/group (:selected-items state))]
                         {:grouped-selected grouped})
                       state)
               area (dommy/sel1 "#select-area")]
           (d/set-attr! area "hidden" true)
           (d/remove-style! area :left)
           (d/remove-style! area :top)
           (d/remove-style! area :width)
           (d/remove-style! area :height)
           {:state (merge state
                          {:start-point nil
                           :select-mode? false
                           :select-zone nil})}))))

   :draw/arrow-recompute-start-end
   (fn [state path delta]
     (if (and path (not (:resize-arrow-mode? state)))
       (let [[start end] (oget path "start-end")]
         (oset! path "start-end" (clj->js [(p/+ start delta)
                                           (p/+ end delta)]))
         {:state {:expand? false
                  :selected nil
                  :vertices nil}})
       {:state state}))

   :draw/select-all
   (fn [state]
     (let [all-items (p/get-all-items)]
       (doseq [item all-items]
         (p/set-selected-style item))
       {:state {:select-mode? true
                :all-items all-items
                :selected-items all-items}}))

   :draw/recompute-select-area
   (fn [state]
     (let [area (dommy/sel1 "#select-area")
           ;; start point
           start-point (:start-point state)
           start-x (or (and start-point (oget start-point "x")) 0)
           start-y (or (and start-point (oget start-point "y")) 0)
           ;; end point
           {:keys [x y]} (:cursor state)
           end-x (or x 0)
           end-y (or y 0)
           x (min start-x end-x)
           y (min start-y end-y)
           width (util/abs (- end-x start-x))
           height (util/abs (- end-y start-y))
           length (if start-point
                    (.getLength ^js (p/point width height))
                    0)
           ]
       (dommy/set-px! area
                      :left x
                      :top  y
                      :width width
                      :height height)
       (let [select-zone (if start-point
                           {:x x
                            :y y
                            :width width
                            :height height}
                           nil)
             all-items (p/get-all-items)
             selected-items (p/get-selected-items
                             all-items
                             select-zone)]
         (let [unselected-items (set/difference
                                 (:selected-items state)
                                 selected-items)]
           (doseq [item unselected-items]
             (p/remove-selected-style item)))
         (doseq [item selected-items]
           (p/set-selected-style item))
         (let [area (dommy/sel1 "#select-area")]
           (d/remove-attr! area "hidden"))
         {:state {:select-zone select-zone
                  :selected-items selected-items
                  :all-items all-items
                  :select-mode? true}})))

   :draw/clear-grouped-copy
   (fn [state]
     (let [{:keys [group-listeners text-listeners]} (:callbacks state)
           children (p/un-group (:grouped-copy state))]
       (doseq [child children]
         (p/set-listeners child group-listeners)
         (p/remove-selected-style child))
       (clear-vertex)
       {:state {:all-items nil
                :grouped-copy nil
                :selected-items nil
                :current-action nil
                :copy nil
                :selected nil
                :vertices nil}}))

   :draw/clear-grouped-selected
   (fn [state]
     (let [{:keys [group-listeners text-listeners]} (:callbacks state)
           children (p/un-group (:grouped-selected state))]
       (doseq [child children]
         (p/set-listeners child group-listeners)
         (p/remove-selected-style child))
       (clear-vertex)
       {:state {:all-items nil
                :grouped-copy nil
                :grouped-selected nil
                :selected-items nil
                :current-action nil
                :copy nil
                :selected nil
                :vertices nil}}))

   :draw/esc
   (fn [state]
     (let [{:keys [group-listeners text-listeners]} (:callbacks state)]
       (cond
         (:grouped-copy state)
         {:state state
          :dispatch [:draw/clear-grouped-copy]}

         (:input-mode? state)
         {:state state}

         :else
         (do
           (when-let [path (:selected state)]
             (clear-vertex)
             (p/remove-selected-style path))

           (when-let [selected-items (:selected-items state)]
             (doseq [item selected-items]
               (p/remove-selected-style item)
               (p/attach-listeners item group-listeners)))

           {:state {:all-items nil
                    :selected-items nil
                    :current-action nil
                    :copy nil
                    :selected nil
                    :vertices nil}}))))

   :draw/clear-input
   (fn [state]
     {:state {:input-mode? false
              :current-text-path nil
              :latest-text nil
              :selected-group-bounds nil
              :input-cursor nil}})

   :draw/drag-path
   (fn [state path e]
     {:state (-> state
                 (assoc :selected path)
                 (update :connectors
                         (fn [col]
                           (->>
                            (for [{:keys [from to arrow] :as x} col]
                              (cond
                                (or (= path from)
                                    (= path to))
                                (p/recompute-connection state x path e)

                                :else
                                x))
                            (into [])))))
      :dispatch [:draw/disable-expand?]})

   :draw/drag-end
   (fn [state path e]
     (cond
       (and (= (:current-action state) :add-connect)
            (not= (p/klass path) "Arrow"))
       {:state state}

       :else
       (let [{:keys [x] :as bounds} (p/get-bounds path)]
         (if (not= 0 x)
           {:state state
            :dispatch [:draw/set-bounds bounds false]}
           {:state state}))))

   :draw/add-connection
   (fn [state from to e]
     ;; redraw arrow
     (let [arrow (:dragging-arrow state)
           bounds (p/get-bounds to)
           points (:dragging-points state)
           points (conj (vec (rest points))
                        (let [[x y] (p/->xy (last points))]
                          (p/point (- x 4) y)))
           _ (p/delete arrow)
           arrow (dc/arrow points "#666" (:group-listeners state))]
       {:state (-> (assoc state :connection-rendered true)
                   (update :connectors
                           (fn [col]
                             (vec (distinct (conj col {:from  from
                                                       :to    to
                                                       :arrow arrow}))))))}))

   :draw/set-textarea-height
   (fn [state height row-count]
     (let [selected (:selected state)
           bounds (p/get-bounds selected)
           row-count (if (or (nil? row-count)
                             (< row-count 1))
                       1
                       row-count)]
       (if selected
         (if (> height (:height bounds))
           (p/set-bounds-height! selected height)))
       {:state (cond-> {:textarea-row-count row-count}
                 bounds
                 (assoc :selected-group-bounds bounds))}))

   :draw/mouse-wheel
   (fn [state e]
     (cond
       ;; zoom
       (or (oget e "altKey")
           (oget e "ctrlKey"))
       (do
         (p/set-zoom e)
         (util/stop e)
         {:state state})

       ;; pan
       :else
       (let [new-center (p/set-center (oget e "deltaX")
                                      (oget e "deltaY"))]
         (util/stop e)
         {:state {:move-vector (p/- new-center (:init-center state))}})))

   :draw/undo
   (fn [state]
     (history/undo!)
     {:state {:selected nil
              :selected-items nil
              :selected-group-bounds nil}
      :dispatch [:draw/disable-expand?]})

   :draw/redo
   (fn [state]
     (history/redo!)
     {:state state})

   :draw/zoom-in
   (fn [state]
     (p/zoom-in)
     {:state state})

   :draw/zoom-out
   (fn [state]
     (p/zoom-out)
     {:state state})

   :draw/init-center
   (fn [state]
     {:state {:init-center (p/get-center)
              :move-vector (p/point 0 0)}})

   :draw/set-callbacks
   (fn [state callbacks]
     {:state {:callbacks callbacks}})

   :draw/set-paper-state
   (fn [state reconciler]
     (reset! p/state reconciler)
     {:state state})

   :draw/set-grouped-selected
   (fn [state path]
     {:state {:grouped-selected path}})

   :draw/set-uploading?
   (fn [state v]
     {:state {:photo-uploading? v}})

   :draw/add-photo
   (fn [state url]
     (let [group-listeners (get-in state [:callbacks :group-listeners])
           path (p/raster {:source url
                           :position (p/get-center)}
                          nil
                          group-listeners)]
       {:state {:photo-uploading? false}
        :dispatch [:draw/set-selected path]}))})
