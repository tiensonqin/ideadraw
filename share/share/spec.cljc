(ns share.spec
  "Draw spec."
  (:require #?(:clj [clojure.spec.alpha :as s])
            #?(:cljs [cljs.spec.alpha :as s])
            #?(:clj [expound.alpha :as ex])
            ))

;; TODO: Spec writing is pretty tedious, need more thoughts

;; TODO: nodes support picture, video

(s/def ::id keyword?)
(s/def ::text string?)
(s/def ::direction #{:top :down :left :right})
(s/def ::style (s/nilable (s/map-of keyword? any?)))
(s/def ::text-style ::style)
(s/def ::shape #{:square :circle})
(s/def ::link #{:arrow :open :dot :thick})
(s/def ::connect (s/or :id ::id
                       :map (s/keys
                             :req-un [::id ::link]
                             :opt-un [::text])))
(s/def ::to (s/coll-of ::connect))
(s/def ::from (s/coll-of ::connect))

;; show when one of the depends was clicked
(s/def ::depends (s/and (s/coll-of ::id) not-empty))

(s/def ::text-node
  (s/keys
   :req-un [::id ::text]
   :opt-un [::style ::to ::from ::depends]))

(s/def ::wrap-node
  (s/keys
   :req-un [::id ::shape]
   :opt-un [::text ::style ::text-style ::to ::from ::depends]))

(s/def ::node (s/or :wrap-node ::wrap-node
                    :text-node ::text-node
                    :graph     ::graph))

(s/def ::nodes (s/and (s/coll-of ::node) not-empty))

(s/def ::graph
  (s/keys
   :req-un [::direction ::nodes]
   :opt-un [::id ::depends ::style]))

(comment
  (def test-text {:id :hello-world-text
                  :text "hello world"
                  ;; :style {:font-size 12
                  ;;         :font-weight "600"
                  ;;         :color "red"}
                  })
  (s/valid? ::node test-text)
  (ex/expound ::node test-text)
  (s/conform ::node test-text)

  (def test-wrap-text {:id :hello-world-text
                       :text "hello world"
                       :text-style {:color "red"}
                       :shape :circle
                       :to [:hello
                            {:id :foo
                             :link :dot
                             :text "very cool"}]
                       })

  (s/valid? ::node test-wrap-text)
  (ex/expound ::node test-wrap-text)
  (s/conform ::node test-wrap-text)

  (def test-graph
    {:direction :top
     :nodes [{:id :a
              :text "a"
              :shape :square}
             {:id :b
              :text "b"
              :shape :circle}
             {:id :subgraph-1
              :direction :left
              :nodes [{:id :c
                       :text "c"}
                      {:id :d
                       :text "d"
                       :shape :square}]
              :depends [:a]}

             {:id :subgraph-2
              :direction :left
              :nodes [{:id :e
                       :text "e"}
                      {:id :f
                       :text "f"
                       :shape :square}]
              :depends [:b]}]})

  (s/valid? ::graph test-graph)
  (ex/expound ::graph test-graph)
  (s/conform ::graph test-graph))
