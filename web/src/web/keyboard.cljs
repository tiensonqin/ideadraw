(ns web.keyboard
  (:require [appkit.citrus :as citrus]
            [appkit.macros :refer [oget oset!]]
            [web.events :as events]
            [web.paper :as paper]
            [web.actions :as actions]))

(defn keydown-handler
  [e]
  (let [code (oget e "keyCode")
        ctrl-key (oget e "ctrlKey")
        shift-key (oget e "shiftKey")
        signup-modal? (get-in @@paper/state [:user :signup-modal?])
        {:keys [input-mode? cursor move-vector]} (get @@paper/state :draw)]
    (cond
      signup-modal?
      nil

      input-mode?
      nil

      (and (or (= code 8) (= code 46)))             ;backspace or delete
      (citrus/dispatch! :draw/delete-selected-path)

      (= code 27)
      (citrus/dispatch-sync! :draw/esc)

      (and (= code 67) ctrl-key) ; Ctrl-c
      (citrus/dispatch! :draw/copy-selected-path)

      (and (= code 65) ctrl-key) ; Ctrl-a
      (citrus/dispatch! :draw/select-all)

      (and (= code 86) ctrl-key) ; Ctrl-v
      (citrus/dispatch! :draw/paste-selected-path events/group-listeners events/text-listeners)

      (and (= code 90) ctrl-key shift-key) ; Ctrl-Shift-z
      (citrus/dispatch! :draw/redo)

      (and (= code 90) ctrl-key) ; Ctrl-z
      (citrus/dispatch! :draw/undo)

      (and (not ctrl-key) shift-key (= code 80)) ; P
      (actions/add-polygon cursor move-vector)

      (and (not ctrl-key)
           (not shift-key))
      (case code
        82
        (actions/add-rectangle cursor move-vector)

        67
        (actions/add-circle cursor move-vector)

        76
        (actions/add-line)

        65
        (actions/add-arrow)

        80
        (actions/pencil)

        85
        (actions/upload-picture)

        84
        (actions/add-text)

        nil)

      :else
      nil)))
