(ns share.components.login
  (:require [rum.core :as rum]
            [appkit.citrus :as citrus]
            [share.ui :as ui]
            [share.util :as util]
            [share.helpers.form :as form]
            [clojure.string :as str]))

(defn- google-signin-success
  [user]
  ;; TODO: getBasicProfile needs to be added to externs, why?
  #?(:cljs
     (let [profile (.call (aget user "getBasicProfile") user)
           user {:id (.call (aget profile "getId") profile)
                 :name (.call (aget profile "getName") profile)
                 :avatar (.call (aget profile "getImageUrl") profile)
                 :email (.call (aget profile "getEmail") profile)}]
       (citrus/dispatch! :user/google-login user))))


(rum/defc login-button <
  {:did-mount (fn [state]
                #?(:cljs
                   (do
                     (if js/window.gapi
                       (js/window.gapi.signin2.render
                        "g-signin2"
                        (clj->js
                         {:scope "profile email"
                          :width 250
                          :height 40
                          :longtitle true
                          :onsuccess google-signin-success
                          :onfailure (fn [e]
                                       (.log js/console e))})))))
                state)}
  []
  [:div#g-signin2.top-right])

(defn signup-fields
  [user username-taken?]
  (let [screen-name-warning (if username-taken?
                              "Username is already taken."
                              "Your username can be up to 15 characters long.")]
    {:screen_name  (cond->
                     {:icon "user"
                      :warning screen-name-warning
                      :style {:width 300}
                      :validators [util/username? (fn [] (not username-taken?))]
                      :on-change (fn [form-data v]
                                   (when-not (str/blank? v)
                                     (citrus/dispatch! :citrus/default-update
                                                       [:user :username-taken?] nil)))}
                     (:screen_name user)
                     (assoc :value (:screen_name user)
                            :disabled true))}))

(rum/defc signup-modal < rum/reactive
  []
  (let [modal? (citrus/react [:user :signup-modal?])
        user (citrus/react [:user :temp])
        username-taken? (citrus/react [:user :username-taken?])]
    [:div#signup-modal
    (ui/dialog
     {:title (str "Welcome, " (:name user))
      :on-close #(citrus/dispatch! :user/close-signup-modal?)
      :visible modal?
      :wrap-class-name "center"
      :style {:width 600}
      :animation "zoom"
      :maskAnimation "fade"}
     (form/render
       {:init-state user
        :title "Choose your username"
        :footer (fn [_]
                  [:div {:style {:margin-top 24}}])
        :fields (signup-fields user username-taken?)
        :submit-text "Signup"
        :on-submit (fn [form-data]
                     (let [data (-> @form-data
                                    (assoc :oauth_id (str (:id user))
                                           :oauth_type "google"))]
                       (citrus/dispatch! :user/new (dissoc data :id) form-data)))}))]))
