(ns ssr.page
  (:import [java.io ByteArrayOutputStream])
  (:require [hiccup.page :as h]
            [cognitect.transit :as t]
            [clojure.data.json :as json]
            [api.config :refer [config]]
            [share.util :as util]
            [clojure.java.io :as io]
            [rum.core :as rum]
            [share.version :refer [version]]
            [api.services.slack :as slack]))

;; encode state hash into Transit format
(defn state->str [state]
  (let [out (ByteArrayOutputStream.)]
    (t/write (t/writer out :json) state)
    (json/write-str (.toString out))))

(defn seo-title-content
  [handler route-params state]
  (let [ideadraw-logo (str (:website-uri config) "/logo.png")]
    ["Ideadraw" "Draw your idea!" ideadraw-logo]))

(def style
  (memoize
   (fn [name]
     (if util/development?
       [:link {:rel "stylesheet"
               :href (str "/css/" name)}]
       (let [content (slurp (io/resource (str "public/" name)))]
         [:style {:type "text/css" :dangerouslySetInnerHTML {:__html content}}])))))

(defn render-page [content req state]
  (h/html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1"}]
    [:meta {:name "apple-mobile-web-app-capable"
            :content "yes"}]
    [:meta {:name "mobile-web-app-capable"
            :content "yes"}]
    [:meta {:http-equiv "X-UA-Compatible"
            :content "IE=edge"}]

    ;; twitter
    [:meta {:name "twitter:card"
            :content "summary"}]

    [:meta {:name "twitter:site"
            :content "@ideadraw"}]

    [:meta {:property "og:site_name"
            :content "Ideadraw"}]

    [:meta {:name "theme-color"
            :content "#FFFFFF"}]

    [:link
     {:href "/apple-touch-icon.png",
      :sizes "180x180",
      :rel "apple-touch-icon"}]

    [:link
     {:href "/favicon-32x32.png",
      :sizes "32x32",
      :type "image/png",
      :rel "icon"}]

    [:link
     {:href "/favicon-16x16.png",
      :sizes "16x16",
      :type "image/png",
      :rel "icon"}]

    [:link
     {:color "#5bbad5",
      :href "/safari-pinned-tab.svg",
      :rel "mask-icon"}]

    [:meta {:content "#00a300",
            :name "msapplication-TileColor"}]
    [:meta {:content "#2A64D1", :name "theme-color"}]

    [:link {:rel "manifest"
            :href "/manifest.json"}]
    [:link {:rel "stylesheet"
            :href "/css/style.css"}]]
   [:body
    [:div#app]
    [:script {:src "/paper-core.min.js"}]
    [:script {:src "/js/compiled/main.js"}]
    [:script "web.core.init()"]

    [:script
     "// Check that service workers are registered
if ('serviceWorker' in navigator) {
  // Use the window load event to keep the page load performant
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw_dev.js');
  });
}
"]]))
