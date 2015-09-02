(ns ^:figwheel-always timer-mystica.core
  (:require
    [reagent.core :as r]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(def test-data
  {:current-player {:faction "Witches" :time-used-ms 301000}
   :active-players [{:faction "Dwarves" :time-used-ms 128000}
                    {:faction "Nomads" :time-used-ms 777000}]
   :passed-players [{:faction "Giants" :time-used-ms 123000}
                    {:faction "Mermaids" :time-used-ms 345000}]})

(defonce app-state (r/atom test-data))

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

(defn active-faction-label [faction]
  [:p.active-faction-label faction])

;; Current player area

(defn current-player-area [{:keys [faction time-used-ms]}]
  [:div.current-player-area
   [active-faction-label faction]
   [main-clock time-used-ms]
   [:div.button-area
    [:button.pass-button "Pass"]
    [:button.done-button "Done"]]])

;; Active player area

(defn player-list-item [{:keys [faction time-used-ms]}]
  [:div.player-item
   [:p.faction-label faction]
   [:p.timer (format-time time-used-ms)]])

(defn active-players-area [active-players]
  [:div.active-players-area
   [:p.player-list-label "Active:"]
   (for [player active-players] [player-list-item player])])

;; Passed player area

(defn passed-players-area [passed-players]
  [:div.passed-players-area
   [:p.player-list-label "Passed:"]
   (for [player passed-players] [player-list-item player])])

;; Main component

(defn timer-mystica [{:keys [current-player active-players passed-players]}]
  [:div.timer-mystica
   [current-player-area current-player]
   [active-players-area active-players]
   [passed-players-area passed-players]])

(r/render-component [timer-mystica @app-state]
                    (. js/document (getElementById "app")))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )

