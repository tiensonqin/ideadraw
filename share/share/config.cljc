(ns share.config
  (:require #?(:clj [environ.core :refer [env]])
            [clojure.string :as s]))

(def development?
  #?(:cljs ^boolean goog.DEBUG
     :clj (or (nil? (:env env))
              (= "dev" (:env env)))))

(def website
  (if development?
    "http://localhost:8083"
    "https://ideadraw.app"))

(def api-host
  (str website "/api/"))

(def cookie-domain
  (if development?
    ""
    ".ideadraw.app"))

(def img-cdn "https://img.ideadraw.app")
