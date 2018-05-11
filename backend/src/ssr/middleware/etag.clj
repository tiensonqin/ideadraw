(ns ssr.middleware.etag
  (:require [clojure.string :as str])
  (:import (java.io File)))


(defn- to-hex-string [bytes]
  (str/join "" (map #(Integer/toHexString (bit-and % 0xff))
                            bytes)))

(defn sha1 [obj]
  (let [bytes (.getBytes (with-out-str (pr obj)))]
    (to-hex-string (.digest (java.security.MessageDigest/getInstance "SHA1") bytes))))


(defmulti calculate-etag class)

(defmethod calculate-etag String [s]
  (sha1 s))

(defmethod calculate-etag File [f]
  (str (.lastModified f) "-" (.length f)))

(defmethod calculate-etag java.io.InputStream [is]
  (let [baos (java.io.ByteArrayOutputStream. )]
    (clojure.java.io/copy is baos)
    {:sha1 (sha1 (.toString baos))
     :is (java.io.BufferedInputStream. (java.io.ByteArrayInputStream. (.toByteArray baos)))}))

(defmethod calculate-etag :default [x]
  nil)

(defn- not-modified [etag]
  {:status 304 :body "" :headers {"etag" etag}})

(defn wrap-etag [handler]
  (fn [req]
    (let [{body :body
           status :status
           {etag "etag"} :headers
           :as resp} (handler req)
          if-none-match (get-in req [:headers "if-none-match"])]
      (if (and etag (not= status 304))
        (if (= etag if-none-match)
          (not-modified etag)
          resp)
        (let [new-etag (calculate-etag body)
              is? (and (map? new-etag)
                       (:is new-etag))
              etag' (if (map? new-etag)
                      (:sha1 new-etag)
                      new-etag)]
          (if (and etag' (= etag' if-none-match))
            (not-modified etag')
            (cond->
              (assoc-in resp [:headers "etag"] etag')
              is?
              (assoc :body (:is new-etag)))))))))
