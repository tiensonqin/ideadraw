(ns web.handlers.user
  (:require [appkit.caches :as caches]))

(defn google-sign-out
  []
  (if js/gapi
    (let [auth2 (.getAuthInstance js/gapi.auth2)]
      (.signOut auth2))))

(def handlers
  {:user/google-login
   (fn [state data]
     {:state {:loading? true}
      :http {:params [:user/google-login data]
             :on-load :user/google-login-ready}})

   :user/google-login-ready
   (fn [state data]
     ;; signout google to protect privacy, :)
     (google-sign-out)

     (if (:user data)
       ;; already signed up
       {:state {:loading? false}
        :dispatch [:user/authenticate-ready data]}

       ;; new user
       {:state {:temp (:temp-user data)
                :loading? false
                :signup-modal? true}}))

   :user/close-signup-modal?
   (fn [state]
     {:state {:signup-modal? false}})

   ;; :user/email-login
   ;; (fn [state form-data]
   ;;   {:state {:loading? true}
   ;;    :http {:params [:auth/email (dissoc @form-data :warning-message)]
   ;;           :on-load [:user/authenticate-ready]
   ;;           :on-error [:user/email-login-error form-data]}})

   ;; :user/email-login-error
   ;; (fn [state form-data reply]
   ;;   {:state (let [state {:loading? false}]
   ;;             (cond
   ;;               (not= (:status reply) 200)
   ;;               (do
   ;;                 (swap! form-data assoc :warning-message (t :invalid-email-or-password))
   ;;                 state)

   ;;               :else
   ;;               state))})

   :user/new
   (fn [state data form-data]
     {:state {:loading? true}
      :http {:params [:user/new data]
             :on-load [:user/authenticate-ready]
             :on-error [:user/new-error form-data]}})


   :user/new-error
   (fn [state form-data reply]
     {:state (cond
               (and (= (:status reply) 400)
                    (= (get-in reply [:body :message]) ":username-exists"))
               (do
                 (swap! form-data assoc-in [:validators :screen_name] false)
                 {:username-taken? true})

               (and (= (:status reply) 400)
                    (= (get-in reply [:body :message]) ":email-exists"))
               (do
                 (swap! form-data assoc-in [:validators :email] false)
                 {:email-taken? true})

               :else
               state)})

   :user/authenticate-ready
   (fn [state result]
     (let [user (:user result)]
       {:state {:loading? false
                :current user
                :signup-modal? false}}))

   :user/logout
   (fn [state]
     ;; clear caches
     (caches/clear
      #(set! (.-location js/window) "/logout"))
     ;; unregister web worker
     {:state {:current nil}})

   :citrus/clear-caches
   (fn [state]
     (caches/clear nil)
     {:state state})})
