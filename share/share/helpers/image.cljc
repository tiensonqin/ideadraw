(ns share.helpers.image
  (:require #?(:cljs [goog.object :as gobj])
            [appkit.rum :as r]
            [rum.core :as rum]
            [share.util :as util]
            #?(:cljs [appkit.promise :as p])
            #?(:cljs ["pica" :as pica])
            ))

;; support image/jpeg, image/gif, video?
#?(:cljs
   (defn resize
     [from to type blob-cb]
     (let [p (pica)]
       (let [resize-promise (.resize p from to)]
         (-> (.resize p from to (clj->js {:alpha true}))
             (p/then (fn [result]
                       (.toBlob p result type 1)))
             (p/then (fn [blob]
                       (blob-cb blob)))
             (p/catch
                 (fn [e]
                   (println "resize failed!: " e))))))))

(defn re-scale
  [width height]
  (let [ratio (/ width height)
        to-width (min 400 width)
        to-height (min 600 height)
        new-ratio (/ to-width to-height)]
    (let [[w h] (cond
                  (> new-ratio ratio)
                  [(* ratio to-height) to-height]

                  (< new-ratio ratio)
                  [to-width (/ to-width ratio)]

                  :else
                  [to-width to-height])]
      [(int w) (int h)])))

(defn upload
  [files cb]
  #?(:cljs
     (doseq [file (take 9 (array-seq files))]
       (let [type (gobj/get file "type")]
         (if (= 0 (.indexOf type "image/"))
           (let [img (js/Image.)]
             (set! (.-onload img)
                   (fn []
                     (let [width (gobj/get img "width")
                           height (gobj/get img "height")
                           off-canvas (js/document.createElement "canvas")
                           [to-width to-height] (re-scale width height)]
                       (set! (.-width off-canvas) to-width)
                       (set! (.-height off-canvas) to-height)

                       (let [ctx (.getContext off-canvas "2d")]
                         (.drawImage ctx img 0 0 to-width to-height))
                       (resize img off-canvas type
                               (fn [blob]
                                 (let [file-form-data (js/FormData.)]
                                   (.append file-form-data "file" blob)
                                   (cb file file-form-data)))))))
             (set! (.-src img)
                   (.createObjectURL (or (.-URL js/window)
                                         (.-webkitURL js/window))
                                     file))
             ))))
     :clj
     nil)
  )
