(ns share.ui
  (:require [rum.core :as rum]
            [share.util :as util]
            [share.dommy :as dommy]
            [appkit.rum :as r]
            [share.icons :as icons]
            [clojure.string :as str]
            #?(:cljs [goog.object :as gobj])
            #?(:cljs ["react" :as react])
            #?(:cljs ["rc-dialog" :as rc-dialog])
            #?(:cljs ["rc-tooltip" :as rc-tooltip])
            #?(:cljs ["react-textarea-autosize" :as textarea])
            ))

(rum/defc svg
  [opts html]
  [:div (cond-> {:dangerouslySetInnerHTML {:__html html}}
          opts
          (merge opts))])

(rum/defc icon
  [{:keys [type opts width height color]
    :or {color "#2c2c2c"
         width 24
         height 24}
    :as attrs}]
  (if-let [f (get icons/icons (keyword type))]
    (svg (assoc-in opts [:style :height] height)
      (f {:width width
          :height height
          :fill color}))
    (do
      (prn "Icon not found: " {:type type})
      [:div "You haven't provided the needed icon."])))

(rum/defc avatar
  [{:keys [shape src class]}]
  [:span
   {:class (str "ant-avatar ant-avatar-image ant-avatar-" shape " " class)}
   [:img {:src src}]])

(defn button
  ([title]
   (button {} title))
  ([{:keys [class style on-click icon-attrs tab-index]
     :or {tab-index 0}
     :as attrs} title]
   [:a (-> attrs
           (dissoc :icon :icon-attrs)
           (assoc :class (str "btn " class)
                  :tab-index tab-index))

    [:span.row1 {:style {:justify-content "center"}}
     (if (:icon attrs)
       (icon (merge
              {:type (:icon attrs)}
              icon-attrs)))

     (if (string? title)
       [:span.btn-contents title]
       title)]]))

;; copy from https://atomiks.github.io/30-seconds-of-css
(rum/defc donut
  []
  [:div.donut])

(rum/defc donut-white
  []
  [:div.donut-white])

(rum/defc bouncing-loader
  []
  [:div.bouncing-loader
   [:div]
   [:div]
   [:div]])


#?(:cljs
   (def dialog (r/adapt-class rc-dialog))
   :clj
   (rum/defc dialog [& opts]
     [:div]))

(defn- force-update-input
  [comp opts]
  (assoc (-> opts (dissoc :on-change))
         :on-change (fn [e]
                      (if-let [on-change (:on-change opts)]
                        (do
                          (on-change e)
                          (.forceUpdate comp))
                        (.forceUpdate comp)))))

(rum/defcc textarea-autosize
  [comp opts]
  #?(:clj
     ;; too-lazy
     [:textarea (merge
                 {:placeholder "Your thoughts here"
                  :style {:border "none"
                          :font-size 15
                          :background-color "#FFF"
                          :resize "none"
                          :width "100%"
                          :padding 12
                          :min-height 134}}
                 opts)]
     :cljs
     (let [autosize (r/adapt-class (gobj/get textarea "default"))]
       (autosize opts))))

(rum/defcc textarea
  "Notice: update should use `dispatch-sync!`"
  [comp opts]
  [:textarea (force-update-input comp opts)])

#?(:cljs (def Tooltip (r/adapt-class rc-tooltip)))

(rum/defc tooltip
  [{:keys [key title placement arrow-content]
    :or {placement "bottom"}
    :as attrs} child]
  #?(:clj
     child
     :cljs
     (Tooltip {:key (if key key (util/random-uuid))
               :placement placement
               :overlay title}
              child)))

(rum/defc hamburger
  []
  [:div#hamburger
   [:span]
   [:span]
   [:span]])
