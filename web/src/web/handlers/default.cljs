(ns web.handlers.default)

(def handlers
  {
   ;; http request error
   :citrus/default-error
   (fn [state error path]
     {:state (update state path assoc :loading? false)})

   :citrus/default-update
   (fn [state path data]
     {:state (if (vector? path)
               (assoc-in state path data)
               (assoc state path data))})

   :citrus/default-merge
   (fn [state path data]
     {:state (if (vector? path)
               (update-in state path merge data)
               (update state path merge data))})

   :citrus/nothing
   (fn [state]
     {:state state})
   })
