(ns share.helpers.form
  (:require [rum.core :as rum]
            [share.ui :as ui]
            #?(:cljs [goog.object :as gobj])
            [clojure.string :as str]
            [appkit.citrus :as citrus])
  #?(:cljs (:import goog.format.EmailAddress)))

(defn required? [v]
  (not (str/blank? v)))

(defn matches? [regex value]
  (boolean (re-matches regex value)))

(defn email? [v]
  #?(:clj (matches? #"(?i)[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?" v)
     :cljs (.isValid (EmailAddress. v))))

(defn password? [v]
  (re-find #"^@?([a-zA-Z0-9-_]){8,64}$" v))

(defn phone? [v]
  (re-find #"^(0|86|17951)?(13[0-9]|15[012356789]|17[0135678]|18[0-9]|14[57])[0-9]{8}$" v))

(defn code? [v]
  (re-find #"^[0-9]{6,6}$" v))

(defn ev
  [e]
  #?(:cljs (gobj/getValueByKeys e "target" "value")
     :clj nil))

(rum/defc input < rum/static
  [form-data name {:keys [textarea? label value icon warning disabled placeholder type on-change validators style class]
                   :or {disabled false
                        type "text"}
                   :as attrs}]
  (let [validated? (get-in @form-data [:validators name])
        input-tag (if textarea? :textarea :input)]
    [:div {:class "field"}
     ;; label
     (if label [:label {:class "label"} label])

     ;; input
     [input-tag (cond-> {:tab-index 0
                         :class
                         (str (cond
                                class
                                class
                                disabled
                                "ant-input ant-input-disabled"
                                :else
                                "ant-input")
                              (case validated?
                                true
                                " is-success"

                                false
                                " is-danger"

                                ""))
                         :placeholder placeholder
                         :disabled disabled
                         :type (clojure.core/name type)
                         :on-change (fn [e]
                                      (swap! form-data (fn [v]
                                                         (assoc-in v [:validators name] nil)))
                                      (on-change e))
                         :on-blur (fn [e]
                                    (if validators
                                      (let [v (ev e)]
                                        (swap! form-data (fn [v]
                                                           (assoc-in v [:validators name] true)))

                                        (doseq [validator validators]
                                          (if-not (validator v)
                                            (swap! form-data (fn [v]
                                                               (assoc-in v [:validators name] false))))))))}
                  value (assoc :default-value value)
                  style (assoc :style style))]

     ;; help message
     (if (and (false? validated?) warning)
       [:p {:class "help is-danger"} warning])]))

;; TODO: replace with radio
(rum/defc radio
  [{:keys [options on-change checked current-value]
    :as opts}]
  [:div {:class "field"}
   [:div {:class "control"}
    (for [{:keys [value label default]} options]
      [:label {:key value
               :class "radio"}
       [:input {:type "radio"
                :value value
                :checked (if (and (nil? current-value)
                                  default)
                           default
                           (checked value))
                :on-change on-change}]
       label])]])

(rum/defc checkbox
  [form-data name {:keys [checked? side warning warning-show? disabled on-change]
                   :or {disabled false}}]
  [:div.field
   ;; {:style {:margin-bottom 12}}
   [:input.check
    {:name (clojure.core/name name)
     :type "checkbox"
     :checked (or checked? (get @form-data name))
     :on-change (fn [e]
                  (swap! form-data assoc name (.-checked (.-target e))))}]
   (if side
     [:label [:span.icon]
      side])

   (if (and warning warning-show?)
     [:div {:class "help is-danger"}
      warning])])

(rum/defc submit
  [on-submit {:keys [submit-text
                     cancel-button?
                     confirm-attrs
                     on-cancel]
              :or {cancel-button? true
                   submit-text "Submit"}
              :as opts}]
  [:div.field {:class "row"}
   (ui/button (merge {:tab-index 0
                      :on-click on-submit
                      :class "btn btn-primary"
                      :on-key-down (fn [e]
                                     (when (= 13 (.-keyCode e))
                                       ;; enter key
                                       (on-submit)))}
                     confirm-attrs)
     submit-text)

   (if cancel-button?
     (ui/button {
                 :style {:margin-left 12}
                 :on-click (fn []
                             (citrus/dispatch! :router/back)
                             (if on-cancel (on-cancel)))}
       "Cancel"))])

(rum/defcs render < (rum/local nil ::form-data)
  [state {:keys [init-state
                 title
                 fields
                 on-submit
                 submit-text
                 style
                 cancel-button?
                 confirm-attrs
                 header
                 footer]
          :or {submit-text "Submit"
               cancel-button? true
               style{:padding 40
                     :broder-radius "4px"
                     :min-width 360}}
          :as form}]
  (let [form-data (get state ::form-data)]
    (when (and init-state (map? init-state) (nil? @form-data))
      (reset! form-data init-state))
    [:div.column {:style style}
     ;; title
     (if title [:h1 {:class "title"} title])

     (if header
       (header form-data))

     (when-let [warning (:warning-message @form-data)]
       [:div
        [:p {:class "help is-danger"} warning]])

     ;; fields
     (for [[name attrs] fields]
       (let [f (case (:type attrs)
                 :checkbox checkbox
                 :radio radio
                 input)
             attrs (if (= (:type attrs) :textarea)
                     (assoc attrs :textarea? true)
                     attrs)
             attrs (assoc attrs :value (or (get @form-data name)
                                           (:value attrs)))
             attrs (if (:disabled attrs)
                     attrs
                     (assoc attrs :on-change (fn [e]
                                               (let [v (ev e)]
                                                 (swap! form-data assoc name v)
                                                 (when-let [on-change (:on-change attrs)]
                                                   (on-change form-data v))))))

             attrs (if (and
                        (= :checkbox (:type attrs))
                        (:warning attrs)
                        (false? (get @form-data name)))
                     (assoc attrs :warning-show? true)
                     attrs)

             attrs (if (= :radio (:type attrs))
                     (assoc attrs
                            :checked #(= % (get @form-data name))
                            :current-value (get @form-data name))
                     attrs)]
         (rum/with-key (if (or (= f input)
                               (= f checkbox))
                         (f form-data name attrs)
                         (f attrs)) name)))

     (if footer (footer form-data))
     ;; submit
     (submit
      (fn []
        ;; validate
        (doseq [[name {:keys [type validators]}] fields]
          (when (and
                 (or (nil? type)
                     (contains? #{:input :textarea "input" "textarea"} type))
                 (get @form-data name))
            (when validators
              (doseq [validator validators]
                (if (validator (get @form-data name))
                  (swap! form-data (fn [v]
                                     (assoc-in v [:validators name] true)))
                  (swap! form-data (fn [v]
                                     (assoc-in v [:validators name] false))))))))
        (when (every? #(or (true? %) (nil? %)) (vals (:validators @form-data)))
          (swap! form-data dissoc :validators)
          (when @form-data
            (on-submit form-data))))
      {:submit-text submit-text
       :cancel-button? cancel-button?
       :confirm-attrs confirm-attrs})]
    ))
