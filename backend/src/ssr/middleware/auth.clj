(ns ssr.middleware.auth
  (:require [api.cookie :as cookie]
            [api.jwt :as jwt]
            [api.util :as util]
            [api.db.user :as u]
            [api.handler.middleware :as api]
            [clojure.java.jdbc :as j]
            [ring.util.response :as resp]
            [api.services.slack :as slack]
            ;; [clojure.repl]
            ))

;; route-handler
(def query-whitelist
  #{:home :user :file})

(defn wrap-auth
  [handler]
  (fn [req]
    (let [tokens (cookie/get-token req)
          {:keys [access-token refresh-token]} tokens]
      (if access-token
        (try
          (let [user (jwt/unsign access-token)]
            (-> (assoc-in req [:context :uid] (some-> (:id user) util/->uuid))
                (handler)))
          (catch Exception e
            (if (= :exp (:cause (ex-data e)))
              (j/with-db-connection [conn (get-in req [:context :datasource])]
                (let [user (jwt/unsign-skip-validation access-token)
                      uid (some-> (:id user) util/->uuid)]
                  (-> (assoc-in req [:context :uid] uid)
                      (handler)
                      (assoc :cookies (u/generate-tokens conn {:id uid})))))
              (do
                (slack/error e)
                (-> (resp/redirect "/error.html")
                    (assoc :cookies cookie/delete-token))))))

        (if (and
             (contains? #{:post :patch :delete} (:request-method req))
             (false? (api/authenticated? req)))
          (resp/redirect "/login")
          (handler req))))))
