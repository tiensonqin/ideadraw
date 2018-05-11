(ns backend.free-port
  (:require [clojure.string :as str]
            [clojure.java.shell :as shell]
            [taoensso.timbre :as timbre]))

;; copy from https://gist.github.com/ptaoussanis/4283150

;; A little utility to allow simple redeployment of Clojure web servers with zero downtime, and without the need for a proxy or load balancer. Just wrap any port-binding calls, and the utility will auto kill pre-existing servers as necessary. *nix only. Based on the blog post by Feng Shen, http://shenfeng.me/fast-restart-clojure-webapp.html

(defn with-free-port!
  "Attempts to kill any current port-binding process, then repeatedly executes
  nullary `bind-port!-fn` (which must return logical true on successful
  binding). Returns the function's result when successful, else throws an
  exception. *nix only.
  This idea courtesy of Feng Shen, Ref. http://goo.gl/kEolu."
  [port bind-port!-fn & {:keys [max-attempts sleep-ms]
                         :or   {max-attempts 50
                                sleep-ms     150}}]
  (let [binder-pid (str/trim (:out (shell/sh "sudo" "-S" "/usr/bin/lsof" "-t" "-sTCP:LISTEN"
                                             (str "-i:" port))))]
    (when-not (str/blank? binder-pid)
      (timbre/warn "Attempting to kill process" binder-pid "to free port" port)
      (let [kill-resp (shell/sh "sudo" "-S" "kill" binder-pid)]
        (when-not (= (:exit kill-resp) 0)
          (throw (Exception. (str "Failed to kill process " binder-pid
                                  " while trying to free port " port ": "
                                  (:err kill-resp)))))))
    (loop [attempt 1]
      (when (> attempt max-attempts)
        (throw (Exception. (str "Failed to bind to port " port " within "
                                max-attempts " attempts ("
                                (* max-attempts sleep-ms) "ms)"))))
      (let [result (try (bind-port!-fn) (catch java.net.BindException _))]
        (if result
          (do (timbre/info (str "Bound to port " port " after "
                                attempt " attempt(s)"))
              result)
          (do (Thread/sleep sleep-ms)
              (recur (inc attempt))))))))

(comment (with-free-port! 8080 (fn [] (ring.adapter.jetty/run-jetty my-handler {:port 8080 :join? false}))))
