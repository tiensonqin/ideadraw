(ns api.services.s3
  (:require [clojure.java.io :as io]
            [api.config :refer [config]]
            [api.util :refer [flake-id->str] :as util]
            [share.util :as su]
            [taoensso.timbre :as t]
            [amazonica.core :as core]
            [amazonica.aws.s3 :as s3]
            [amazonica.aws.cloudfront :as cloudfront]
            [api.services.slack :as slack]
            [clojure.string :as str]
            [bidi.bidi :as bidi])
  (:import [java.io ByteArrayInputStream]))


(defn copy-uri-to-file [uri file]
  (with-open [in (io/input-stream uri)
              out (io/output-stream file)]
    (io/copy in out)))

(defn cloudfront-invalidate [paths]
  (let [{:keys [access-key secret-key endpoint]} (:aws config)]
    (core/with-credential [access-key secret-key endpoint]
      (cloudfront/create-invalidation {:distribution-id (:img-distribution-id config)
                                       :invalidation-batch {:paths {:items paths
                                                                    :quantity (count paths)}
                                                            :caller-reference (str (util/flake-id))}}))))

(defn save-url-image
  ([name uri]
   (save-url-image name uri "jpg" "image/jpeg"))
  ([name uri suffix content-type]
   (try
     (let [tmp-path (str "/tmp/" name "." suffix)
           name (format "pics/%s.%s" name suffix)
           {:keys [access-key secret-key endpoint]} (:aws config)]
       (copy-uri-to-file uri tmp-path)
       (let [file (io/file tmp-path)
             length (.length file)]
         (core/with-credential [access-key secret-key endpoint]
           (s3/put-object :bucket-name "ideadraw"
                          :key name
                          :file file
                          :metadata {
                                     ;; :server-side-encryption "AES256"
                                     :content-type content-type
                                     :content-length length
                                     :cache-control "public, max-age=31536000"}))
         (str (:img-cdn config)
              (str/replace name "pics" ""))))
     (catch Exception e
       (t/error e)
       false)))
  )

(defn put-image
  [{:keys [tempfile length name png? invalidate?]
    :or {name (flake-id->str)
         png? false
         invalidate? false}}]
  (let [[image-type content-type]
        (if png?
          ["png" "image/png"]
          ["jpg" "image/jpeg"])]
    (if (and tempfile length)
      (try
        (let [name (if name name (flake-id->str))
              name (if su/development? (str "development/" name) name)
              original-name name
              name (format "pics/%s.%s" name image-type)
              {:keys [access-key secret-key endpoint]} (:aws config)]
          (core/with-credential [access-key secret-key endpoint]
            (s3/put-object :bucket-name "ideadraw"
                           :key name
                           :metadata {
                                      ;; :server-side-encryption "AES256"
                                      :content-type content-type
                                      :content-length length
                                      :cache-control "public, max-age=31536000"
                                      }
                           :file tempfile))
          (str (:img-cdn config)
               (str/replace name "pics" "")))
        (catch Exception e
          (slack/error e)
          false)))))
