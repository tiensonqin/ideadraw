(ns backend.servers
  (:require [taoensso.timbre :as timbre]
            [backend.free-port :as port]))

(defn- die-with "Terminates app after logging given message."
  ([message]           (timbre/fatal message)           (System/exit 1))
  ([exception message] (timbre/fatal exception message) (System/exit 1)))

(defonce servers (atom {}))

(defn start-server-or-die!
  [server-name port start-server!-fn]
  (when (and port (not (@servers server-name)))
    (timbre/info (str "Attempting to start " server-name " server..."))

    (try (when-let [;; Kill other binding processes & throw an exception on
                    ;; binding failure:
                    server (port/with-free-port! port start-server!-fn)]
           (swap! servers assoc server-name server)
           (timbre/report (str server-name " server is running on port " port))
           server)

         (catch Exception e
           (die-with e (str "Failed to start " server-name " server"))))))


(comment (start-server-or-die! :jetty 8080
                               (fn [] (ring.adapter.jetty/run-jetty my-handler {:port 8080 :join? false}))))
