(ns timer-mystica.game-components
  (:require
    [timer-mystica.factions :as factions]
    [timer-mystica.component-helpers :as helpers]))

(defn text-color-for-background [background-color]
  (if (= background-color :black)
    :text-light
    :text-dark))

;; Meta buttons

(defn reset-button [on-reset]
  [:button.reset-button.btn.btn-danger.btn-lg {:on-click on-reset}
   [:span.glyphicon.glyphicon-remove]])

(defn undo-button [{:keys [history-index]} on-undo]
  (let [enabled? (pos? history-index)
        action (if enabled? on-undo nil)
        disabled-class (if enabled? nil :disabled)]
    [:button.undo-button.btn.btn-default.btn-lg {:class disabled-class :on-click action}
     [:span.glyphicon.glyphicon-step-backward]]))

(defn redo-button [{:keys [history history-index]} on-redo]
  (let [enabled? (< history-index (count history))
        action (if enabled? on-redo nil)
        disabled-class (if enabled? nil :disabled)]
    [:button.redo-button.btn.btn-default.btn-lg {:class disabled-class :on-click action}
     [:span.glyphicon.glyphicon-step-forward]]))

(defn pause-button [{:keys [paused?]} {:keys [on-pause on-unpause]}]
  (let [action (if paused? on-unpause on-pause)
        glyphicon (if paused? :glyphicon-play :glyphicon-pause)]
    [:button.pause-button.btn.btn-default.btn-lg {:on-click action}
     [:span.glyphicon {:class glyphicon}]]))

(defn meta-button-area [state {:keys [on-reset on-undo on-redo] :as actions}]
  [:div.meta-button-area
   [reset-button on-reset]
   [:div.spacer]
   [undo-button state on-undo]
   [redo-button state on-redo]
   [pause-button state actions]])

;; Clock

(defn two-digit-str [n]
  (if (< n 10)
    (str "0" n)
    (str n)))

(defn format-time [ms]
  (let [total-seconds (quot ms 1000)
        minutes (quot total-seconds 60)
        seconds (rem total-seconds 60)
        seconds-str (two-digit-str seconds)]
    (str minutes ":" seconds-str)))

(defn subsecond-component [ms]
  (str "." (-> ms (quot 10) (rem 100) two-digit-str)))

(defn main-clock [time-used-ms]
  [:div.clock-area
   [:p
    [:span.clock (format-time time-used-ms)]
    [:span.clock-subsecond (subsecond-component time-used-ms)]]])

;; Buttons

(defn start-round-button [round on-start-round]
  (let [text (str "Start Round " round)]
    [:button.start-round-button.btn.btn-primary.btn-lg {:on-click on-start-round} text]))

(defn game-over-button []
  [:button.game-over-button.btn.btn-primary.btn-lg.disabled "Game Over"])

(defn pass-button [on-pass]
  [:button.pass-button.btn.btn-default.btn-lg {:on-click on-pass} "Pass"])

(defn last-player-next-button []
  [:button.next-button.btn.btn-primary.btn-lg.disabled "Last Player"])

(defn next-button [on-next]
  [:button.next-button.btn.btn-primary.btn-lg {:on-click on-next} "Next Player"])

(defn button-area [{:keys [between-rounds? round active-players]}
                   {:keys [on-start-round on-pass on-next]}]
  [:div.button-area
   (cond
     (> round 6) [game-over-button]
     between-rounds? [start-round-button round on-start-round]
     :else (list ^{:key :pass-button} [pass-button on-pass]
                 (if (seq active-players)
                   ^{:key :next-button} [next-button on-next]
                   ^{:key :last-player-next-button} [last-player-next-button])))])

;; Current player area

(defn current-player-area [{{:keys [faction color time-used-ms]} :current-player
                            :as                            game-state}
                           actions]
  [:div.current-player-area {:class (text-color-for-background color)}
   [:p.active-faction-label (factions/title faction)]
   [main-clock time-used-ms]
   [button-area game-state actions]])

;; Active player area

(defn player-list-item [{:keys [faction color time-used-ms]}]
  [:div.player-item {:class (helpers/class-string [color
                                                   (text-color-for-background color)])}
   [:p.faction-label (factions/title faction)]
   [:p.timer (format-time time-used-ms)]])

(defn active-players-area [active-players]
  (when (seq active-players)
    [:div.active-players-area
     [:p.player-list-label "Next:"]
     [helpers/css-transition-group {:transition-name "slide-up" :transition-leave false}
      (for [player active-players]
        ^{:key (:faction player)} [player-list-item player])]]))

;; Passed player area

(defn passed-players-area [passed-players]
  [helpers/css-transition-group {:transition-name "slide-up" :transition-leave false}
   (when (seq passed-players)
     ^{:key :passed-players-area}
     [:div.passed-players-area
      [:p.player-list-label "Passed:"]
      [helpers/css-transition-group {:transition-name "slide-up" :transition-leave false}
       (for [player passed-players]
         ^{:key (:faction player)} [player-list-item player])]])])

;; Game component

(defn main [state actions]
  (let [{:keys [game-state]} state
        {:keys [current-player active-players passed-players]} game-state
        current-color (:color current-player)]
    [:div.timer-mystica {:class (helpers/class-string [current-color
                                                       (text-color-for-background current-color)])}
     [meta-button-area state actions]
     [current-player-area game-state actions]
     [active-players-area active-players]
     [passed-players-area passed-players]]))