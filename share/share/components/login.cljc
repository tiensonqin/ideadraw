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


(defn attach-sign-in
  [el auth2]
  #?(:cljs
     (.attachClickHandler auth2
                          el
                          #js {}
                          google-signin-success
                          (fn [error]
                            (prn "error: " error)
                            (js/console.dir error)))))

(defn make-google-login
  "Turns a DOM element into a clickable Google login button.
  First pull down the public google client-id from the server."
  [e]
  #?(:cljs
     (when js/window.gapi
       (js/window.gapi.load
        "auth2"
        (fn []
          (let [auth2 (js/window.gapi.auth2.init
                       #js{:client_id "906517456-2v8s1h6uia0p5h9emmh8f7jj1lm62egp.apps.googleusercontent.com"})]
            (attach-sign-in e auth2)))))))

(rum/defc login-button <
  {:did-mount (fn [state]
                #?(:cljs
                   (let [el (js/document.getElementById "g-signin2")]
                     (make-google-login el)))
                state)}
  []
  [:div#g-signin2.top-right
   [:button "Login"]])

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
