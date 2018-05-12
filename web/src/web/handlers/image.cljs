(ns web.handlers.image)

(def handlers
  {:image/upload
   (fn [state form-data cb]
     {:state state
      :http {:endpoint "upload"
             :params form-data
             :type :raw
             :on-load [:image/upload-success cb]
             ;; :on-progress (fn [{:keys [loaded total] :as progress}]
             ;;                (let [percentage (->> (/ loaded total)
             ;;                                      (* 100))]
             ;;                  (prn "progressing: " percentage)))
             ;; :on-upload (fn [e]
             ;;              (prn "uploading: ")
             ;;              (.dir js/console e)
             ;;              (let [loaded (.-loaded e)
             ;;                    total (or (.-total e) 0)
             ;;                    [base max-pct] [0 100]
             ;;                    slope (- max-pct base)
             ;;                    x (if (= 0 total) 1 (/ loaded total))
             ;;                    percentage (js/Math.floor (+ base (* x slope)))]
             ;;                (prn {:percentage percentage}))
             ;;              )
             }})

   :image/upload-success
   (fn [state cb result]
     (if (:url result)
       (cb (:url result)))
     {:state state})
})
