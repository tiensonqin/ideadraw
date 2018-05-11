#!/usr/bin/env lumo

(ns deploy.core
  (:require [cljs.reader :as reader]
            [cljs.nodejs :as nodejs]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [clojure.set :as set]
            [goog.string :as gstring]
            [goog.string.format]
            [goog.object :as gobj]))

;; TODO: add rollback, mv _next/* to archive, mv _prev/* to _next

(nodejs/enable-util-print!)
(def fs (js/require "fs"))

(def child-process (js/require "child_process"))
(def exec-sync (gobj/get child-process "execSync"))
(def exec (gobj/get child-process "exec"))

(def uuid-re #"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")

(defonce version-path "../share/share/version.cljc")
(defonce sw-path "./public/sw.js")
(defonce sw-template-path "./public/sw_template.js")
(defonce pre-version (atom nil))
(defonce new-version (random-uuid))

(defn read-version []
  (.readFile fs version-path "utf8"
             (fn [err data]
               (let [v (re-find uuid-re data)]
                 (reset! pre-version v)
                 (.writeFile fs version-path (str/replace data v new-version)
                             (fn [err]
                               (if err
                                 (println "Error: " err))))

                 ;; sw template, suffix, mainjs, mainjs-version
                 (.readFile fs sw-template-path "utf8"
                            (fn [err data]
                              (let [new-data (-> data
                                                 (str/replace "{{suffix}}" (random-uuid))
                                                 (str/replace "{{mainjs}}" (str "main-" new-version ".js"))
                                                 (str/replace "{{mainjs-version}}" (random-uuid)))]
                                (.writeFile fs sw-path new-data
                                           (fn [err]
                                             (if err
                                               (println "Error: " err)
                                               (exec-sync "cp public/sw.js ../backend/resources/public")))))))))))

(defn -main [& args]
  (read-version)
  (exec-sync "rm -f public/main*.js")
  (exec-sync "yarn release")
  (exec-sync (str "cp public/js/compiled/main.js "  (str "public/main-" new-version ".js")))
  (exec-sync "cleancss -o public/style.css public/css/style.css")
  (exec-sync "cd public && cp -R *.html *.png *.txt *.xml *.ico images manifest.json style.css main*.js paper-core.min.js ../../backend/resources/public"))

(set! *main-cli-fn* -main)
