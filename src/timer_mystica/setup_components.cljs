(ns timer-mystica.setup-components
  (:require
    [timer-mystica.factions :as factions]))

(defn faction-select [id]
  [:select.form-control {:id id}
   [:option "<None>"]
   (for [faction factions/all]
     ^{:key faction} [:option (factions/title faction)])])

(defn get-faction-for-player [i]
  (let [id (str "player-select-" i)
        select-elem (.getElementById js/document id)
        index (.-selectedIndex select-elem)]
    (if (= index 0)
      nil
      (factions/all (dec index)))))

(defn get-selected-factions []
  (->> (range 5)
       (map get-faction-for-player)
       (filter identity)))

(defn start-game-button [on-start-game]
  [:button.start-game-button.btn.btn-primary.btn-lg
   {:on-click (comp on-start-game get-selected-factions)}
   "Start Game"])

(defn main [on-start-game]
  [:div.timer-mystica
   [:div.faction-select-wrapper
    (for [i (range 5)]
      ^{:key i} [faction-select (str "player-select-" i)])
    [start-game-button on-start-game]]])