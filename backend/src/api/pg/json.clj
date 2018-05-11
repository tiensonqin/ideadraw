(ns api.pg.json
  (:require [cheshire.core :refer [generate-string parse-string]]
            [clojure.java.jdbc :as j])
  (:import org.postgresql.util.PGobject))

(defn value-to-json-pgobject [value]
  (doto (PGobject.)
    (.setType "json")
    (.setValue (generate-string value))))

(defn value-to-inet
  [ip]
  (doto
      (PGobject.)
    (.setType "inet")
    (.setValue ip)))

(extend-protocol j/ISQLValue
  clojure.lang.Keyword
  (sql-value [value] (name value))

  clojure.lang.IPersistentMap
  (sql-value [value] (value-to-json-pgobject value))

  clojure.lang.IPersistentVector
  (sql-value [value] (value-to-json-pgobject value))

  clojure.lang.IPersistentSet
  (sql-value [value] (value-to-json-pgobject value))

  clojure.lang.LazySeq
  (sql-value [value] (value-to-json-pgobject value)))

(extend-protocol j/IResultSetReadColumn
  PGobject
  (result-set-read-column [pgobj metadata idx]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "json" (parse-string value true)
        :else value))))
