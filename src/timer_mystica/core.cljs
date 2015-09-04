(ns ^:figwheel-always timer-mystica.core
  (:require
    [reagent.core :as r]
    [timer-mystica.components :as components]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(def test-data
  {:current-player {:faction :witches :time-used-ms 301000}
   :active-players [{:faction :dwarves :time-used-ms 128000}
                    {:faction :nomads :time-used-ms 777000}]
   :passed-players [{:faction :darklings :time-used-ms 123000}
                    {:faction :ice-maidens :time-used-ms 345000}]
   :between-rounds? false
   :round 2})

(defonce app-state (r/atom test-data))

; State updates

(defn player-selected-next [{:keys [current-player active-players]
                             :as   state}]
  (if (empty? active-players)
    state
    (assoc state :current-player (first active-players)
                 :active-players (-> active-players (subvec 1) (conj current-player)))))

(defn player-selected-pass [{:keys [current-player active-players passed-players round]
                             :as   state}]
  (if (seq active-players)
    (assoc state :current-player (first active-players)
                 :active-players (subvec active-players 1)
                 :passed-players (conj passed-players current-player))
    (assoc state :current-player (first passed-players)
                 :active-players (-> passed-players (subvec 1) (conj current-player))
                 :passed-players []
                 :round (inc round)
                 :between-rounds? true)))

(defn advance-time [{:keys [between-rounds?] :as state}
                    ms]
  (if between-rounds?
    state
    (update-in state [:current-player :time-used-ms] + ms)))

(defn start-round [state]
  (assoc state :between-rounds? false))

; Add components with Reagent

(when-let [app-container (.getElementById js/document "app")]
  (r/render-component [components/main app-state
                       {:on-start-round #(swap! app-state start-round)
                        :on-next #(swap! app-state player-selected-next)
                        :on-pass #(swap! app-state player-selected-pass)}]
                      app-container))

; Call advance-time on ticks

(defn current-time-ms []
  (.getTime (js/Date.)))

(defonce advance-to-current-time
         (let [last-time-ms (atom (current-time-ms))]
           (fn []
             (let [time (current-time-ms)
                   delta (- time @last-time-ms)]
               (reset! last-time-ms time)
               (swap! app-state advance-time delta)))))

(defonce start-timer
         ((fn request-frame []
            (advance-to-current-time)
            (.requestAnimationFrame js/window request-frame))))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )

