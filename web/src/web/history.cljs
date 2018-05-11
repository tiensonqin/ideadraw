(ns web.history
  (:require [web.paper :as p]))

(def state (atom {:txs nil
                  :current-tx 0}))

;; TODO:
;; 1. connection resize
;; 2. item bounds change
;; 3. color or other attributes change

;; action maybe
;; 1. [:item/new item]
;; 2. [:item/delete item]
;; 3. [:item/resize item]
;; 4. [:change/color item-1 {:old-color :green :new-color :red}]

(defn transact!
  "v should be a vector of actions"
  [v]
  (let [tx (inc (:current-tx @state))]
    (swap! state
           (fn [state]
             (-> state
                 (assoc :current-tx tx)
                 (update :txs assoc tx v))))))

(defmulti undo-handler (fn [action & args] action))

(defmethod undo-handler :item/new [_ item]
  ;; delete item
  (p/delete item))

(defmethod undo-handler :item/delete [_ item]
  ;; delete item
  (p/restore item))

(defmethod undo-handler :default [action & args]
  (prn "unsupported action: " action))

(defn undo!
  []
  (let [tx-id (:current-tx @state)]
    (when-let [tx (get-in @state [:txs tx-id])]
     (when (> tx-id 0)
       (swap! state update :current-tx dec)
       (doseq [command tx]
         (apply undo-handler command))))))

(defmulti redo-handler (fn [action & args] action))

(defmethod redo-handler :item/new [_ item]
  ;; delete item
  (p/restore item))

(defmethod redo-handler :item/delete [_ item]
  ;; delete item
  (p/delete item))

(defmethod redo-handler :default [action & args]
  (prn "unsupported action: " action))

(defn redo!
  []
  (let [tx-id (:current-tx @state)]
    (when-let [tx (get-in @state [:txs (inc tx-id)])]
     (swap! state update :current-tx inc)
     (doseq [command tx]
       (apply redo-handler command)))))

(comment

  (require '[web.paper :as p])
  (defn new-rect
    []
    (let [r (p/rect
              (p/point (rand-int 600) (rand-int 600))
              (p/point (rand-int 600) (rand-int 600))
              nil)]
      (transact! [[:item/new r]])))

  (undo!)

  (redo!)
  )
