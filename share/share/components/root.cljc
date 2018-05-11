(ns share.components.root
  (:require [rum.core :as rum]
            [appkit.citrus :as citrus]
            [clojure.string :as str]
            [share.ui :as ui]
            [share.dommy :as dommy]
            [share.util :as util]
            [share.mixins :as mixins]
            [share.helpers.form :as form]
            [share.helpers.image :as image]
            #?(:cljs [web.paper :as p])
            #?(:cljs [web.component :as dc])
            #?(:cljs [goog.dom :as gdom])
            #?(:cljs [web.keyboard :as keyboard])
            #?(:cljs [web.actions :as ac])
            #?(:cljs [web.events :as events])
            #?(:cljs [appkit.macros :refer [oget oset!]])))

(defn upload-images
  [files]
  #?(:cljs
     (image/upload
      files
      (fn [file file-form-data]
        (let [temp-id (long (str (.getTime (util/get-date)) (rand-int 100)))]
          (citrus/dispatch-sync! :draw/set-uploading? true)
          (citrus/dispatch!
           :image/upload
           file-form-data
           (fn [url]
             (citrus/dispatch! :draw/add-photo url))))))))

(defn attach-listeners
  [state]
  #?(:cljs
     (when-let [canvas (dommy/sel1 "#draw-canvas")]
       (mixins/listen state js/window :keydown keyboard/keydown-handler)

       (mixins/listen state js/window :mousemove
                      (fn [e]
                        (citrus/dispatch! :draw/mouse-move e)
                        ))

       (mixins/listen state js/window :wheel
                      (fn [e]
                        (citrus/dispatch! :draw/mouse-wheel (.getBrowserEvent e)))))
     :clj identity))

(defn tool-listeners
  [tool]
  #?(:cljs
     (do
       (oset! tool
              "onMouseDown"
              (fn [e]
                (citrus/dispatch! :draw/mouse-down e)))
       (oset! tool
              "onMouseDrag"
              (fn [e]
                (citrus/dispatch! :draw/mouse-drag e events/arrow-listeners)))
       (oset! tool
              "onMouseUp"
              (fn [e]
                (citrus/dispatch! :draw/mouse-up e))))))

(def stop? (atom false))

(rum/defc action-preview < rum/reactive
  (mixins/event-mixin (fn [state]
                        (let [input-mode? (first (:rum/args state))]
                          (mixins/esc-listeners state
                                                input-mode?
                                                :on-close
                                                (fn [e]
                                                  #?(:cljs
                                                     (citrus/dispatch! :draw/add-latest-text
                                                                       events/text-listeners))
                                                  (citrus/dispatch! :draw/clear-input)
                                                  (reset! stop? true))
                                                :stop? stop?))))
  (rum/local false ::input-mode?)
  [input-mode? current-action]
  (let [{:keys [x y] :as cursor} (citrus/react [:draw :cursor])
        bounds (citrus/react [:draw :selected-group-bounds])
        input-cursor (or bounds cursor)
        move-vector (citrus/react [:draw :move-vector])
        current-text (citrus/react [:draw :current-text-path])
        latest-text (citrus/react [:draw :latest-text])
        bounds bounds]
    (cond
      (and input-mode? bounds)
      (do
        (reset! stop? false)
        [:div#add-text {:style {:position "absolute"
                                :left (:x input-cursor)
                                :top (:y input-cursor)}}
         (ui/textarea-autosize {:auto-focus true
                                :placeholder "add text"
                                :style {:overflow "hidden"
                                        :border "none"
                                        :font-size 16
                                        :background "transparent"
                                        :resize "none"
                                        :padding 12
                                        :width (:width bounds)
                                        :white-space "pre-wrap"
                                        :overflow-wrap "break-word"}
                                :default-value (or latest-text "")
                                :on-change (fn [e]
                                             (citrus/dispatch-sync!
                                              :draw/set-latest-text
                                              (form/ev e)))
                                :on-height-change (fn [height instance]
                                                    #?(:cljs
                                                       (let [row-count (dec (oget instance "rowCount"))]
                                                         (citrus/dispatch! :draw/set-textarea-height height row-count))))
                                })])

      current-action
      [:div#action-preview
       {:on-click
        #?(:cljs
           (fn [e]
             (let [cursor {:x (oget e "clientX")
                           :y (oget e "clientY")}]
               ;; multi-methods
               (case current-action
                 :add-rectangle
                 (ac/add-rectangle cursor move-vector)

                 :add-circle
                 (ac/add-circle cursor move-vector)

                 :add-polygon
                 (ac/add-polygon cursor move-vector)

                 nil)))
           :clj identity)}
       (case current-action
         :add-rectangle
         [:div {:style {:position "absolute"
                        :left x
                        :top y
                        :z-index 2
                        :width 180
                        :height 90
                        :background "#FFF"
                        :border-radius "0 4px 4px 4px"}}]

         :add-circle
         [:div {:style {:position "absolute"
                        :left (- x 90)
                        :top (- y 90)
                        :z-index 2
                        :width 180
                        :height 180
                        :border-radius 90
                        :background "#FFF"}}]

         :add-polygon
         [:div {:style {:position "absolute"
                        :left (- x 90)
                        :top (- y 90)
                        :z-index 2}}
          [:svg {:height 180
                 :width 180}
           [:polygon {:points "90,0 0,180 180,180"
                      :fill "#FFF"}]]]

         nil
         )]

      :else
      nil)))

(rum/defc actions < rum/reactive
  [current-action]
  (let [uploading? (citrus/react [:draw :photo-uploading?])]
    [:div#actions {:style {:position "fixed"
                           :box-shadow "rgba(0, 0, 0, 0.25) 0px 2px 8px 0px"
                           :transition "height 0.1s ease-in-out, width 0.1s ease-in-out"
                           :top 80
                           :left 20
                           :background "#219653"
                           :padding "12px 6px"
                           :border-radius "3px"
                           :z-index 999}}
     [:div.item {:class (if (= current-action :add-rectangle)
                          "selected"
                          "")}

      [:a {:on-click (fn []
                       (citrus/dispatch! :draw/set-current-action :add-rectangle))}
       [:svg {:width 24
              :height 18}
        [:rect {:width 24
                :height 18
                :fill-opacity 0
                :stroke-width 4
                :stroke "#FFFFFF"}]]]]
     [:div.item {:class (if (= current-action :add-circle)
                          "selected"
                          "")}

      [:a {:on-click (fn []
                       (citrus/dispatch! :draw/set-current-action :add-circle))}
       [:svg {:height 24
              :width 24}
        [:circle {:cx "12"
                  :cy "12"
                  :r "11"
                  :fill-opacity 0
                  :stroke-width 2.5
                  :stroke "#FFFFFF"}]]]]

     [:div.item {:class (if (= current-action :add-polygon)
                          "selected"
                          "")}

      [:a {:on-click (fn []
                       (citrus/dispatch! :draw/set-current-action :add-polygon))}
       [:svg {:height 24
              :width 24}
        [:polygon {:points "12,0 0,24 24,24"
                   :style {:stroke-width 2.5
                           :stroke "#FFFFFF"}
                   :fill-opacity 0}]]]]

     [:div.item {:class (if (= current-action :add-line)
                          "selected"
                          "")}

      [:a {:on-click #?(:cljs ac/add-line
                        :clj identity)}
       [:svg {:height 24
              :width 24}
        [:line {:x1 0
                :y1 12
                :x2 24
                :y2 12
                :style {:stroke-width 2.5
                        :stroke "#FFFFFF"}
                :fill-opacity 0}]]]]

     [:div.item {:class (if (= current-action :add-connect)
                          "selected"
                          "")}
      [:a {:on-click #?(:cljs ac/add-arrow
                        :clj identity)}
       (ui/icon {:type :connect
                 :color "#FFFFFF"})]]

     [:div.item {:class (if (= current-action :pencil)
                          "selected"
                          "")}
      [:a {:on-click #?(:cljs ac/pencil
                        :clj identity)}
       (ui/icon {:type :edit
                 :color "#FFFFFF"})]]

     [:div.item {:class (if (= current-action :add-image)
                          "selected"
                          "")}
      (if uploading?
        (ui/donut-white)
        [:a {:on-click #?(:cljs ac/upload-picture
                          :clj identity)}
         (ui/icon {:type :add_a_photo
                   :color "#FFFFFF"})])
      [:input
       {:id "photo_upload"
        :accept "image/*"
        :type "file"
        :on-change (fn [e]
                     #?(:cljs
                        (upload-images
                         (-> (oget e "target")
                             (oget "files")))))
        :hidden true}]]

     [:div.item {:class (if (= current-action :add-text)
                          "selected"
                          "")}
      [:a {:on-click #?(:cljs ac/add-text
                        :clj identity)}
       (ui/icon {:type :t
                 :color "#FFFFFF"
                 :opts {:style {:margin-right -6}}})]]

     [:div.item
      [:a#png-export {:download "draw.png"
                      :on-click (fn []
                                  #?(:cljs
                                     (when-let [canvas (dommy/sel1 "#draw-canvas")]
                                       (let [data-url (.toDataURL canvas "image/png")
                                             button (dommy/sel1 "#png-export")]
                                         (oset! button "href" data-url)))))}
       (ui/icon {:type :file-download
                 :color "#FFFFFF"})]]
     ]))

(rum/defc zoom
  []
  [:div#zoom {:style {:position "fixed"
                      :bottom 24
                      :right 24}}
   [:div.row
    (ui/button {:class "btn-sm"
                :on-click (fn [] (citrus/dispatch! :draw/undo))
                :style {:padding "0 6px"
                        :background "transparent"
                        :border "none"}}
      "Undo")

    (ui/button {:class "btn-sm"
                :on-click (fn [] (citrus/dispatch! :draw/redo))
                :style {:margin-left 12
                        :padding "0 6px"
                        :background "transparent"
                        :border "none"}}
      "Redo")

    (ui/button {:class "btn-sm"
                :on-click (fn [] (citrus/dispatch! :draw/zoom-out))
                :style {:margin-left 24
                        :padding "0 8px"
                        :background "transparent"
                        :border "none"}}
      [:span {:style {:font-size 24}}
       "-"])

    (ui/button {:class "btn-sm"
                :on-click (fn [] (citrus/dispatch! :draw/zoom-in))
                :style {:margin-left 12
                        :padding "0 6px"
                        :background "transparent"
                        :border "none"}}
      [:span {:style {:font-size 20}}
       "+"])]])

(def canvas-rendered? (atom false))
(rum/defc root < rum/reactive
  (mixins/event-mixin attach-listeners)
  {:will-mount
   (fn [state]
     (when-let [app (dommy/sel1 "#app")]
       (dommy/add-class! app "none-scroll"))
     (when-let [body (dommy/sel1 "body")]
       (dommy/set-style! body "height" "100%"))
     state)
   :will-unmount
   (fn [state]
     (when-let [app (dommy/sel1 "#app")]
       (dommy/remove-class! app "none-scroll"))
     (when-let [body (dommy/sel1 "body")]
       (dommy/remove-style! body "height")))
   :after-render
   (fn [state]
     #?(:cljs
        (do
          (when (false? @canvas-rendered?)
            (when-let [canvas (dommy/sel1 "#draw-canvas")]
              (.setup p/Paper canvas)
              (let [tool (p/Tool.)]
                (.draw (.-view p/Paper))
                (citrus/dispatch! :draw/init-center)
                (citrus/dispatch! :draw/set-callbacks
                                  events/callbacks)
                (reset! canvas-rendered? true)
                (tool-listeners tool))))))
     state)}
  [reconciler]
  (let [current-action (citrus/react [:draw :current-action])
        input-mode? (citrus/react [:draw :input-mode?])
        resize-direction (citrus/react [:draw :resize-direction])]
    [:div#draw {:style {:background "#ddd"}}
     [:div#select-area {:style {:border "1px dotted #000"
                                :position "absolute"}
                        :hidden true}]

     (actions current-action)

     (zoom)

     (let [{:keys [width height]} (util/get-layout)]
       [:canvas {:key "canvas"
                 :class (cond
                          current-action
                          "crosshair"
                          resize-direction
                          (str (name resize-direction) "-resize")
                          :else
                          "")
                 :id "draw-canvas"
                 :resize "true"}])

     (action-preview input-mode? current-action)])
  )
