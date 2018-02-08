(ns gaia-project-timer.setup-components
  (:require
    [gaia-project-timer.factions :as factions]
    [gaia-project-timer.setup :as setup]
    [clojure.string :as str]))

; Faction select

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
                         :value     (or (:faction player) :none)}
   [:option {:value :none} "<None>"]
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

; Start button

(defn start-game-button [on-start-game enabled?]
  [:button.start-game-button.btn.btn-primary.btn-lg
   {:on-click on-start-game
    :disabled (not enabled?)}
   "Start Game"])

; Validation errors

(defn player-message-prefix
  "Creates strings of the form \"Players 1 and 2 both \" or \"Players 1, 2, and 3 all \"."
  [indices]
  (let [inc-indices (map inc indices)]
    (str "Players "
         (if (= (count inc-indices) 2)
           (str (first inc-indices) " and " (second inc-indices) " both ")
           (str (str/join ", " (subvec inc-indices 0 (dec (count inc-indices))))
                ", and "
                (last inc-indices)
                " all "))
         " ")))

(defn message-for-error [error]
  (case (:type error)
    :not-enough-players "At least two players are required."
    :same-color (str (player-message-prefix (:indices error))
                     "have color "
                     (str/lower-case (factions/title (:color error)))
                     ".")
    :multiple-variable (str (player-message-prefix (:indices error))
                            "are variable-color factions.")))

(defn error-list [errors]
  [:ul
   (for [error errors]
     ^{:key error} [:li (message-for-error error)])])

; Main component

(defn main [state {:keys [on-set-faction on-set-color on-start-game validate-setup]}]
  (let [errors (validate-setup)]
    [:div.gaia-project-timer
     [:div.faction-select-wrapper
      [:h1 "Gaia Project Timer"]
      [:h4 "Faction Select"]
      (for [i (range setup/max-players)]
        (let [player (get-in state [:players i])
              on-faction-change (partial on-set-faction i)
              on-color-change (partial on-set-color i)]
          ^{:key i} [faction-select player on-faction-change on-color-change]))
      [start-game-button on-start-game (empty? errors)]
      [error-list errors]]]))
