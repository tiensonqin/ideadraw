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
  (let [locale (:locale state)
        zh-cn? (= locale :zh-cn)
        {:keys [handler route-params]} (:ui/route req)
        [seo-title seo-content seo-image] (seo-title-content handler route-params state)]
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

      [:meta {:name "twitter:description"
              :content seo-content}]

      [:meta {:name "twitter:site"
              :content "@ideadraw"}]

      [:meta {:name "twitter:title"
              :content seo-title}]

      [:meta {:name "twitter:image:src"
              :content seo-image}]

      [:meta {:name "twitter:image:alt"
              :content seo-title}]

      ;; open graph
      [:meta {:property "og:title"
              :content seo-title}]

      [:meta {:property "og:type"
              :content (if (= handler :post)
                         "article"
                         "site")}]
      [:meta {:property "og:url"
              :content (str (:website-uri config) (:uri req))}]
      [:meta {:property "og:image"
              :content seo-image}]
      [:meta {:property "og:description"
              :content seo-content}]
      [:meta {:property "og:site_name"
              :content "Ideadraw"}]

      [:meta {:name "google-signin-scope"
              :content "profile email"}]
      [:meta {:name "google-signin-client_id"
              :content (get-in config [:oauth :google :app-key])}]
      [:title seo-title]
      [:meta {:name "description"
              :content seo-content}]
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
      (rum/render-static-markup (style "style.css"))]
     [:body
      [:div#app content]
      [:script {:src "/paper-core.min.js"}]
      ;; Google login
      [:script {:src "https://apis.google.com/js/platform.js"}]

      [:script {:src "https://apis.google.com/js/api:client.js"}]
      [:script {:src (if util/development?
                       "/js/compiled/main.js"
                       (str "/main-" version ".js"))}]
      [:script
       (str "web.core.init(" (state->str state) ")")]

      [:script
       (format
        "
// Check that service workers are registered
if ('serviceWorker' in navigator) {
  // Use the window load event to keep the page load performant
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw%s.js');
  });
}
"
        (if util/development?
          "_dev"
          ""))]])))
