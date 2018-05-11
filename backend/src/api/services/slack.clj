(ns api.services.slack
  (:require [org.httpkit.client :as http-client]
            [cheshire.core :refer [generate-string]]
            [api.util :refer [production?]]
            [clojure.string :as str]
            [taoensso.timbre :as t]
            [api.config :refer [config]]))

(defn slack-escape [message]
  "Escape message according to slack formatting spec."
  (str/escape message {\< "&lt;" \> "&gt;" \& "&amp;"}))

(defn send-msg
  ([hook msg]
   (send-msg hook msg nil))
  ([hook msg {:keys [specific-user]}]
   (when (production?)
     (let [body {"text" (slack-escape msg)}
           body (if specific-user
                  (assoc body "channel" (str "@" specific-user))
                  body)]
       (http-client/post hook
                         {:headers {"content-type" "application/json"
                                    "accept" "application/json"}
                          :body (generate-string body)})))))

(def rules {:new {:webhook (get-in config [:slack :new-hook])}
            :api-exception {:webhook (get-in config [:slack :api-exception-hook])}
            :api-latency {:webhook (get-in config [:slack :api-latency-hook])}})

(defn at-prefix
  [msg]
  (str "@channel\n" msg))

(defn notify
  [channel msg]
  (let [msg (at-prefix msg)]
    (send-msg (get-in rules [channel :webhook]) msg)))

(defn new-api-exception
  [msg]
  (notify :api-exception msg))

(defn new
  [msg]
  (notify :new msg))

(defn notify-latency
  [msg]
  (notify :api-latency msg))

(defn notify-exception
  [msg]
  (send-msg (get-in rules [:api-exception :webhook]) msg))

(defn to-string
  [& messages]
  (let [messages (cons (format "Environment: %s" (if (production?) "Production" "Dev")) messages)]
    (->> (map
           #(if (isa? (class %) Exception)
              (str % "\n\n"
                   (apply str (interpose "\n" (.getStackTrace %))))
              (str %))
           messages)
         (interpose "\n")
         (apply str))))

(defmacro error
  "Log errors, then push to slack,
  first argument could be throwable."
  [& messages]
  `(do
     (t/error ~@messages)
     (new-api-exception (to-string ~@messages))))

(defmacro debug
  [& messages]
  `(t/debug ~@messages))

(defmacro info
  [& messages]
  `(t/info ~@messages))
