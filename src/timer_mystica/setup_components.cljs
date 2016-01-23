(ns timer-mystica.setup-components
  (:require
    [timer-mystica.factions :as factions]))

(def no-faction-title "<None>")

(defn value-from-change-event [e]
  (let [value (-> e .-target .-value)]
    (if (= value no-faction-title)
      nil
      (factions/from-title value))))

(defn wrap-on-change-for-event
  "Takes a handler which receives the new faction/color value and transforms it to receive an
  on-change event instead."
  [on-change]
  (fn [e] (on-change (value-from-change-event e))))

(defn non-variable-faction-select [player on-change]
  [:select.form-control {:on-change (wrap-on-change-for-event on-change)
                         :value     (:faction player)}
   [:option {:value nil} "<None>"]
   (for [faction factions/all]
     ^{:key faction} [:option {:value faction} (factions/title faction)])])

(defn color-select [player on-change]
  [:select.form-control {:on-change (wrap-on-change-for-event on-change)
                         :value     (:color player)}
   (for [color factions/variable-faction-colors]
     ^{:key color} [:option {:value color} (factions/title color)])])

(defn variable-faction-select [player on-faction-change on-color-change]
  [:div.row
   [:div.col-xs-8 [non-variable-faction-select player on-faction-change]]
   [:div.col-xs-4 [color-select player on-color-change]]])

(defn faction-select [player on-faction-change on-color-change]
  (if (factions/variable? (:faction player))
    [variable-faction-select player on-faction-change on-color-change]
    [non-variable-faction-select player on-faction-change]))

(defn start-game-button [on-start-game]
  [:button.start-game-button.btn.btn-primary.btn-lg
   {:on-click on-start-game}
   "Start Game"])

(defn main [state {:keys [on-set-faction on-set-color on-start-game]}]
  [:div.timer-mystica
   [:div.faction-select-wrapper
    (for [i (range 5)]
      (let [player (get-in state [:players i])
            on-faction-change (partial on-set-faction i)
            on-color-change (partial on-set-color i)]
        ^{:key i} [faction-select player on-faction-change on-color-change]))
    [start-game-button on-start-game]]])