(ns timer-mystica.components
  (:require
    [reagent.core :as r]
    [clojure.string :as str]))

;; Faction info

(def faction-color-classes
  {:auren :green
   :witches :green
   :alchemists :black
   :darklings :black
   :halflings :brown
   :cultists :brown
   :engineers :gray
   :dwarves :gray
   :mermaids :blue
   :swarmlings :blue
   :chaos-magicians :red
   :giants :red
   :fakirs :yellow
   :nomads :yellow
   :ice-maidens :white
   :yetis :white
   :dragonlords :orange
   :acolytes :orange
   :changelings :variable
   :riverwalkers :variable})

(defn faction-name [faction]
  (->>
    (-> faction name (str/split "-"))
    (map str/capitalize)
    (str/join " ")))

;; Clock

(defn format-time [ms]
  (let [total-seconds (/ ms 1000)
        minutes (quot total-seconds 60)
        seconds (rem total-seconds 60)
        seconds-str (if (< seconds 10) (str "0" seconds) (str seconds))]
    (str minutes ":" seconds-str)))

(defn main-clock [time-used-ms]
  [:div.clock-area
   [:p.clock (format-time time-used-ms)]])

;; Current player area

(defn current-player-area [{:keys [faction time-used-ms]}]
  [:div.current-player-area
   [:p.active-faction-label (faction-name faction)]
   [main-clock time-used-ms]
   [:div.button-area
    [:button.pass-button.btn.btn-default.btn-lg "Pass"]
    [:button.done-button.btn.btn-primary.btn-lg "Next Player"]]])

;; Active player area

(defn player-list-item [{:keys [faction time-used-ms]}]
  [:div.player-item {:class (faction-color-classes faction)}
   [:p.faction-label (faction-name faction)]
   [:p.timer (format-time time-used-ms)]])

(defn active-players-area [active-players]
  [:div.active-players-area
   [:p.player-list-label "Active:"]
   (for [player active-players]
     ^{:key (:faction player)} [player-list-item player])])

;; Passed player area

(defn passed-players-area [passed-players]
  (when (not-empty passed-players)
    [:div.passed-players-area
     [:p.player-list-label "Passed:"]
     (for [player passed-players]
       ^{:key (:faction player)} [player-list-item player])]))

;; Main component

(defn main [app-state]
  (let [{:keys [current-player active-players passed-players]} @app-state]
    [:div.timer-mystica {:class (faction-color-classes (:faction current-player))}
     [current-player-area current-player]
     [active-players-area active-players]
     [passed-players-area passed-players]]))
