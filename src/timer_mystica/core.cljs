(ns ^:figwheel-always timer-mystica.core
  (:require
    [reagent.core :as r]
    [timer-mystica.components :as components]
    [cljs.reader :as reader]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(def setup-state
  {:mode :setup})

(defn new-game-state [factions]
  (if (empty? factions)
    setup-state
    (let [new-player (fn [faction] {:faction faction :time-used-ms 0})]
      {:mode          :game
       :history       []
       :history-index 0
       :paused?       false
       :last-timestamp-ms nil
       :game-state    {:current-player  (new-player (first factions))
                       :active-players  (mapv new-player (rest factions))
                       :passed-players  []
                       :between-rounds? true
                       :round           1}})))

(def storage-key "tm-state")

(defonce app-state
         (let [saved-state-edn (.getItem js/localStorage storage-key)
               saved-state (when saved-state-edn (reader/read-string saved-state-edn))]
           (r/atom (or saved-state setup-state))))

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

(defn start-round [game-state]
  (assoc game-state :between-rounds? false))

(defn advance-to-time [{:keys [paused? last-timestamp-ms game-state] :as state} timestamp-ms]
  (let [{:keys [between-rounds?]} game-state
        time-passed-ms (if last-timestamp-ms
                         (- timestamp-ms last-timestamp-ms)
                         0)
        new-time-state (assoc state :last-timestamp-ms timestamp-ms)]
    (if (or between-rounds? paused?)
      new-time-state
      (update-in new-time-state [:game-state :current-player :time-used-ms] + time-passed-ms))))

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

; Reset

(defn clear-state! []
  (reset! app-state setup-state)
  (.clear js/localStorage))

(defn clear-state-request-confirm! []
  (let [confirmed (js/confirm "Quit current game and return to faction select?")]
    (when confirmed (clear-state!))))

; Side-effecting actions

(defn save-state! []
  (.setItem js/localStorage storage-key (prn-str @app-state)))

(defn swap-state-and-save! [f & args]
  (apply swap! app-state f args)
  (save-state!))

(defn reset-state-and-save! [new-state]
  (reset! app-state new-state)
  (save-state!))

(defn swap-game-state-push-history-save! [f & args]
  (apply swap-state-and-save! update-game-state-add-history f args))

; Add components with Reagent

(when-let [app-container (.getElementById js/document "app")]
  (r/render-component [components/main app-state
                       {:on-start-round #(swap-game-state-push-history-save! start-round)
                        :on-next        #(swap-game-state-push-history-save! player-selected-next)
                        :on-pass        #(swap-game-state-push-history-save! player-selected-pass)
                        :on-pause       #(swap-state-and-save! assoc :paused? true)
                        :on-unpause     #(swap-state-and-save! assoc :paused? false)
                        :on-undo        #(swap-state-and-save! undo)
                        :on-redo        #(swap-state-and-save! redo)
                        :on-start-game  #(reset-state-and-save! (new-game-state %))
                        :on-reset       #(clear-state-request-confirm!)}]
                      app-container))

; Call advance-to-time on ticks

(defn current-time-ms []
  (.getTime (js/Date.)))

(defonce timer-did-start
         (do
           ((fn request-frame []
              (swap! app-state advance-to-time (current-time-ms))
              (js/requestAnimationFrame request-frame)))
           true))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
