(ns api.handler.http
  (:require [taoensso.timbre :as timbre]
            [clj-social.core :as social]
            [api.config :as config]
            [api.util :as util]
            [api.db.user :as u]
            [api.db.file :as file]
            [api.db.comment :as comment]
            [api.db.report :as report]
            [api.db.util :as du]
            [api.db.refresh-token :as refresh-token]
            [api.services.s3 :as s3]
            [api.cookie :as cookie]
            [api.jwt :as jwt]
            [clojure.java.jdbc :as j]
            [clojure.string :as str]
            [ring.util.response :as resp]
            [api.services.slack :as slack]
            [share.util :as su]))

(defn reject-not-owner-or-admin?
  [db uid table id ok-result]
  (let [owner? (du/owner? db uid table id)
        screen-name (:screen_name (u/get db uid))]
    (cond
      (not owner?)
      {:status 401
       :message ""}

      :else
      ok-result)))

(defmulti handle last)

(defmethod handle :user/get-current [[{:keys [uid datasource]
                                       :as context} data]]
  (j/with-db-connection [conn datasource]
    (util/ok
     ;; TODO:
     {})))


(defmethod handle :user/google-login [[{:keys [datasource]} data]]
  (j/with-db-transaction [conn datasource]
    (if-let [user (u/oauth-authenticate conn :google (str (:id data)))]
      {:status 200
       :body {:user user}
       :cookies (u/generate-tokens conn user)}
      ;; continue to signup
      {:status 200
       :body {:temp-user data}})))

(defmethod handle :user/new [[{:keys [datasource]} data]]
  (j/with-db-transaction [conn datasource]
    (cond
      (and (:screen_name data)
           (du/exists? conn :users {:screen_name (:screen_name data)}))
      (util/bad :username-exists)

      (and (:email data)
           (du/exists? conn :users {:email (:email data)}))
      (util/bad :email-exists)

      :else
      (when-let [user (u/create conn data)]
        {:status 200
         :body {:user user}
         :cookies (u/generate-tokens conn user)}))))

(defmethod handle :file/new [[{:keys [datasource]} data]]
  (j/with-db-transaction [conn datasource]
    (when-let [file (file/create conn data)]
      {:status 200
       :body {:file file}})))

(defmethod handle :file/delete [[{:keys [datasource]} data]]
  (j/with-db-transaction [conn datasource]
    (file/delete conn (:id data))
    {:status 200
     :body {:deleted true}}))

(defmethod handle :report/new [[{:keys [uid datasource]} data]]
  (j/with-db-transaction [conn datasource]
    (do
      (future (slack/new (str "New report: "
                              "Data: " data
                              ".")))
      (util/ok
       (report/create conn (assoc data :user_id uid))))))

(defmethod handle :comment/new [[{:keys [uid datasource]} data]]
  (j/with-db-transaction [conn datasource]
    (if-let [comment (comment/create conn (assoc data :user_id uid))]
      (util/ok comment)
      (util/bad "comment failed!"))))

(defmethod handle :comment/delete [[{:keys [uid datasource]} data]]
  (j/with-db-transaction [conn datasource]
    (reject-not-owner-or-admin? conn uid :comments (:id data)
                                (do
                                  (comment/delete conn (:id data))
                                  (util/ok {:result true})))))
(defmethod handle :default [[_ data]]
  (util/ok data))

(defn handler
  [{:keys [context body-params]
    :as req}]
  (handle [context (second body-params) req (first body-params)]))

(defn upload
  [{:keys [context params]
    :as req}]
  (let [{:keys [tempfile size]} (:file params)]
    (if-let [name (s3/put-image {:tempfile tempfile
                                 :length  size
                                 :name (:name params)
                                 :png? (:png params)
                                 :invalidate? (:invalidate params)})]
      (util/ok {:url name})
      (util/bad "Upload failed!"))))
