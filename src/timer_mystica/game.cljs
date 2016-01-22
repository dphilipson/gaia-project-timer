(ns timer-mystica.game)

(defn new-game-state [factions]
  (let [new-player (fn [faction] {:faction faction :time-used-ms 0})]
    {:mode              :game
     :history           []
     :history-index     0
     :paused?           false
     :last-timestamp-ms nil
     :game-state        {:current-player  (new-player (first factions))
                         :active-players  (mapv new-player (rest factions))
                         :passed-players  []
                         :between-rounds? true
                         :round           1}}))

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

(def max-history-size 300)

(defn trim-history [{:keys [history history-index] :as state}]
  (let [trim-amount (max 0 (- (count history) max-history-size))]
    (assoc state :history (subvec history trim-amount)
                 :history-index (- history-index trim-amount))))

(defn update-game-state-add-history
  [{:keys [game-state history history-index] :as state} f & args]
  (-> state
      (assoc :game-state (apply f game-state args)
             :history (-> history (subvec 0 history-index) (conj game-state))
             :history-index (inc history-index))
      trim-history))

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