(ns share.components.user
  (:require [rum.core :as rum]
            [share.ui :as ui]))

(rum/defc avatar
  [current-user]
  [:div.top-right
   (ui/avatar {:shape "circle"
               :src (:avatar current-user)})])
