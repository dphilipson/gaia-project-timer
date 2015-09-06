(ns ^:figwheel-always timer-mystica.core
  (:require
    [reagent.core :as r]
    [timer-mystica.components :as components]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(def setup-state
  {:mode :setup})

(defn new-game-state [factions]
  (let [new-player (fn [faction] {:faction faction :time-used-ms 0})]
    {:mode :game
     :history []
     :history-index 0
     :paused? false
     :game-state {:current-player (new-player (first factions))
                  :active-players (mapv new-player (rest factions))
                  :passed-players []
                  :between-rounds? true
                  :round 1}}))

(defonce app-state (r/atom setup-state))

; Game-state updates

(defn player-selected-next [{:keys [current-player active-players]
                             :as   game-state}]
  (if (empty? active-players)
    game-state
    (assoc game-state :current-player (first active-players)
                      :active-players (-> active-players (subvec 1) (conj current-player)))))

(defn player-selected-pass [{:keys [current-player active-players passed-players round]
                             :as   game-state}]
  (if (seq active-players)
    (assoc game-state :current-player (first active-players)
                      :active-players (subvec active-players 1)
                      :passed-players (conj passed-players current-player))
    (assoc game-state :current-player (first passed-players)
                      :active-players (-> passed-players (subvec 1) (conj current-player))
                      :passed-players []
                      :round (inc round)
                      :between-rounds? true)))

(defn advance-time [{{:keys [between-rounds?]} :game-state
                     :keys                     [paused?]
                     :as                       state}
                    ms]
  (if (or between-rounds? paused?)
    state
    (update-in state [:game-state :current-player :time-used-ms] + ms)))

(defn start-round [game-state]
  (assoc game-state :between-rounds? false))

; History

(defn update-game-state-add-history
  [{:keys [game-state history history-index] :as state} f & args]
  (assoc state :game-state (apply f game-state args)
               :history (-> history (subvec 0 history-index) (conj game-state))
               :history-index (inc history-index)))

(defn undo [{:keys [game-state history history-index] :as state}]
  (assoc state :history (if (= (count history) history-index)
                          (conj history game-state)
                          history)
               :history-index (dec history-index)
               :game-state (history (dec history-index))))

(defn redo [{:keys [history history-index] :as state}]
  (let [new-index (inc history-index)
        history-size (count history)]
    (assoc state :history (if (= new-index (dec history-size))
                            (subvec history 0 (dec history-size))
                            history)
                 :history-index new-index
                 :game-state (history new-index))))

; Side-effecting actions

(defn swap-game-state-add-history! [f & args]
  (apply swap! app-state update-game-state-add-history f args))

; Add components with Reagent

(when-let [app-container (.getElementById js/document "app")]
  (r/render-component [components/main app-state
                       {:on-start-round #(swap-game-state-add-history! start-round)
                        :on-next        #(swap-game-state-add-history! player-selected-next)
                        :on-pass        #(swap-game-state-add-history! player-selected-pass)
                        :on-pause       #(swap! app-state assoc :paused? true)
                        :on-unpause     #(swap! app-state assoc :paused? false)
                        :on-undo        #(swap! app-state undo)
                        :on-redo        #(swap! app-state redo)
                        :on-start-game  #(reset! app-state (new-game-state %))}]
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

(defonce timer-did-start
         (do
           ((fn request-frame []
              (advance-to-current-time)
              (.requestAnimationFrame js/window request-frame)))
           true))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
