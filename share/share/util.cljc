(ns share.util
  (:refer-clojure :exclude [format uuid random-uuid])
  (:require [clojure.string :as s]
            [clojure.set :as set]
            [clojure.walk :as w]
            [bidi.bidi :as bidi]
            [share.version :refer [version]]
            [share.config :as config]
            [share.dommy :as dommy]
            #?(:clj [environ.core :refer [env]])
            #?(:cljs [goog.object :as gobj])
            #?(:cljs [goog.string.format]))
  #?(:clj (:import  [java.util.UUID])))

(defonce development? config/development?)

(defn get-date []
  #?(:clj (java.util.Date.)
     :cljs (js/Date.)))

(defn get-time
  []
  (.getTime (get-date)))

(defn get-layout
  []
  #?(:cljs {:width  (gobj/get js/window "innerWidth")
            :height (gobj/get js/window "innerHeight")}
     :clj {:width 1024
           :height 700}))

;; #?(:cljs
;;    (when-not config/development?
;;      (set! (.-log js/console)
;;            (fn [] nil))))

(defn ev
  [e]
  #?(:cljs (gobj/getValueByKeys e "target" "value")
     :clj nil))

(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new
  nested structure. keys is a sequence of keys. Any empty maps that result
  will not be present in the new structure."
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
    (dissoc m k)))

(defn cdn-image
  [name & {:keys [suffix width height]
           :or {width 40
                height 40
                suffix "jpg"}}]
  (if name
    (str config/img-cdn "/" name "." suffix "?w=" width "&h=" height)))

(defn format
  [fmt & args]
  #?(:cljs (apply goog.string.format fmt args)
     :clj (apply clojure.core/format fmt args)))

(defn non-blank? [v]
  (and (string? v)
       (not (s/blank? v))))

(defn username? [v]
  (re-find #"^@?([A-Za-z]){1}([A-Za-z0-9_]){0,14}$" v))

(defn encrypted-name? [v]
  (re-find #"^@?([%a-zA-Z0-9]){1,192}$" v))

(defn length? [{:keys [min max]}]
  (fn [v]
    (if v
      (let [length (.-length v)]
        (and (if min (if (>= length min) true false) true)
             (if max (if (<= length max) true false) true))))))

(defn remove-duplicates
  ([entities] (remove-duplicates nil entities))
  ([f entities]
   (let [f (or (and (or (fn? f) (keyword? f)) f) identity)
         meets (transient #{})]
     (persistent!
      (reduce (fn [acc item]
                (let [k (f item)]
                  (if (or (nil? item) (contains? meets k))
                    acc
                    (do (conj! meets k)
                        (conj! acc item)))))
              (transient [])
              entities)))))

(defn indexed [coll] (map-indexed vector coll))

(defonce user-agent (atom nil))

(defn mobile?
  []
  (re-find #"Mobi" #?(:cljs js/navigator.userAgent
                      :clj @user-agent)))

(defn set-timeout [t f]
  #?(:cljs (js/setTimeout f t)
     :clj  nil))

(def link-re #"https?://[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]")
(defn link?
  [s]
  (some->> s
           (re-find (re-pattern (str "^[<]?" link-re "[>]?$")))))

(def email-re #"[\w._%+-]+@[\w.-]+\.[\w]{2,4}")
(def youtube-re #"https://youtu\.be/([a-zA-Z0-9-_]+)(\?t=[a-zA-Z0-9]+)*")

(defn- image?
  [link]
  (contains? #{"jpg" "png" "gif"}
             (->> (take-last 3 link)
                  (apply str)
                  (s/lower-case))))

(defn parse-int
  [x]
  #?(:cljs (js/parseInt x)
     :clj (Integer/parseInt x)))

(defn start->secs
  [s]
  (let [s (s/replace s "?t=" "")
        result (s/split s #"[hms]")]
    (case (count result)
      3
      (+ (* 3600 (parse-int (first result)))
         (* 60 (parse-int (second result)))
         (parse-int (last result)))
      2
      (+ (* 60 (parse-int (first result)))
         (parse-int (last result)))
      1
      (parse-int (last result))

      0)))

(defn uuid [s]
  #?(:cljs (cljs.core/uuid s)
     :clj (java.util.UUID/fromString s)))

(def random-uuid
  #?(:cljs cljs.core/random-uuid
     :clj #(java.util.UUID/randomUUID)))

(defn deep-merge
  "Like merge, but merges maps recursively."
  [& maps]
  (if (every? #(or (map? %) (nil? %)) maps)
    (apply merge-with deep-merge maps)
    (last maps)))

;; TODO: pprint
(defn debug
  [& args]
  #?(:cljs
     nil
     ;; (. js/console log (apply str "%c " args " ") "background: #2e2e2e; color: white")

     :clj
     nil))

(defn map-remove-nil?
  [m]
  (reduce
   (fn [i [k v]]
     (if v (assoc i k v) i))
   {}
   m))

(defn get-file-base-name
  [s]
  (->> (s/split s #"\.")
       (drop-last)
       (interleave )
       (s/join "_")))

(defn keywordize
  [data]
  #?(:cljs (js->clj data :keywordize-keys true)
     :clj (w/keywordize-keys data)))

(defn get-domain
  [link]
  (let [d (second (s/split link #"//"))
        d (first (s/split d #":"))
        d (first (s/split d #"\?"))
        d (first (s/split d #"/"))
        d (first (s/split d #"\#"))
        parts (s/split d #"\.")
        parts (take-last 2 parts)]
    (str (first parts) "." (second parts))))

(defn capitalize
  [s]
  (some-> (name s)
          (s/replace #"[-_]+" " ")
          (s/split #" ")
          (->>
           (map s/capitalize)
           (interpose " ")
           (apply str))))

;; TODO: might need externs
;; (defn google-sign-out
;;   []
;;   #?(:cljs (if js/gapi
;;              (let [auth2 (.getAuthInstance js/gapi.auth2)]
;;                (.signOut auth2))))
;;   )

(defn cdn-version
  [x]
  (if config/development?
    x
    (str "/" version x)))

(defn scroll-top []
  #?(:clj nil
     :cljs
     (if (exists? (.-pageYOffset js/window))
       (.-pageYOffset js/window)
       (.-scrollTop (or (.-documentElement js/document)
                        (.-parentNode (.-body js/document))
                        (.-body js/document))))))

(defn page-height
  "Return the height of the page."
  []
  #?(:clj 1024
     :cljs js/document.documentElement.clientHeight))

(defn scroll-height
  []
  #?(:clj 1024
     :cljs js/document.documentElement.scrollHeight))

(defn abs
  [x]
  #?(:clj (Math/abs x)
     :cljs (js/Math.abs x)))

(defn stop [e]
  (doto e (.preventDefault) (.stopPropagation)))
