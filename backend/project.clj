(defproject backend "0.1.0-SNAPSHOT"
  :description "Ideadraw backend"
  :url "FIXME: https://github.com/fixme"

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [http-kit "2.5.3"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [primitive-math "0.1.6"]
                 [org.clojure/data.xml "0.0.8"]
                 [com.taoensso/encore "2.87.0"]
                 [ring/ring-defaults "0.1.4"]
                 [ring/ring-devel "1.6.3"]
                 [ring-middleware-format "0.7.2"]
                 [org.postgresql/postgresql "42.2.4"]
                 [org.clojure/java.jdbc "0.7.3"]
                 [honeysql "0.9.1"]
                 [hikari-cp "1.8.2"]
                 [buddy/buddy-sign "2.2.0"]
                 [buddy/buddy-hashers "1.3.0"]
                 [environ "1.1.0"]
                 [com.stuartsierra/component "0.2.3"]
                 [com.taoensso/timbre "5.1.2"]
                 [ring-cors "0.1.7"]
                 [clj-social "0.1.0"]
                 [amazonica "0.3.115"]
                 [org.clojure/core.async "0.3.465"]
                 [bk/ring-gzip "0.2.1"]
                 [bidi "2.1.2"]
                 [tiensonqin/appkit "0.1.0-SNAPSHOT"]
                 [javax.xml.bind/jaxb-api "2.3.1"]
                 ;; server side rendering
                 [org.clojure/clojurescript "1.10.879"]
                 [compojure "1.6.0"]]

  :source-paths ["src" "../share"]
  :jvm-opts ["-Duser.timezone=UTC" "-Dclojure.spec.check-asserts=true"]

  :plugins [[lein-environ "1.1.0"]]

  :min-lein-version "2.6.1"

  :main backend.application
  ;; :aot :all

  ;; nREPL by default starts in the :main namespace, we want to start in `user`
  ;; because that's where our development helper functions like (go) and (reset)
  ;; live.
  :repl-options {:init-ns user}

  :bikeshed {:max-line-length 200}

  :profiles {:dev {:dependencies [[org.clojure/tools.nrepl "0.2.13"]
                                  [reloaded.repl "0.2.3"]
                                  [enlive "1.1.6"]
                                  [expound "0.6.0"]]
                   :source-paths ["dev"]}
             ;; :uberjar {:main backend.application
             ;;           :aot [backend.application com.stuartsierra.component com.stuartsierra.dependency]}
             })
