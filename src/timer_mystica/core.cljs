(ns ^:figwheel-always timer-mystica.core
  (:require
    [reagent.core :as r]
    [timer-mystica.setup :as setup]
    [timer-mystica.game :as game]
    [timer-mystica.components :as components]
    [cljs.reader :as reader]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(def storage-key "tm-state")

(defonce app-state-atom
         (let [saved-state-edn (.getItem js/localStorage storage-key)
               saved-state (when saved-state-edn (reader/read-string saved-state-edn))]
           (r/atom (or saved-state setup/initial-state))))

; Reset

(defn clear-state! []
  (reset! app-state-atom setup/initial-state)
  (.clear js/localStorage))

(defn clear-state-request-confirm! []
  (let [confirmed (js/confirm "Quit current game and return to faction select?")]
    (when confirmed (clear-state!))))

; Side-effecting actions

(defn save-state! []
  (.setItem js/localStorage storage-key (prn-str @app-state-atom)))

(defn swap-state-and-save! [f & args]
  (apply swap! app-state-atom f args)
  (save-state!))

(defn reset-state-and-save! [new-state]
  (reset! app-state-atom new-state)
  (save-state!))

(defn swap-game-state-push-history-save! [f & args]
  (apply swap-state-and-save! game/update-game-state-add-history f args))

; Add components with Reagent

(when-let [app-container (.getElementById js/document "app")]
  (r/render-component
    [components/main
     app-state-atom
     {:on-start-round #(swap-game-state-push-history-save! game/start-round)
      :on-next        #(swap-game-state-push-history-save! game/player-selected-next)
      :on-pass        #(swap-game-state-push-history-save! game/player-selected-pass)
      :on-pause       #(swap-state-and-save! assoc :paused? true)
      :on-unpause     #(swap-state-and-save! assoc :paused? false)
      :on-undo        #(swap-state-and-save! game/undo)
      :on-redo        #(swap-state-and-save! game/redo)
      :on-start-game  #(reset-state-and-save! (game/new-game-state %))
      :on-reset       #(clear-state-request-confirm!)}]
    app-container))

; Call advance-to-time on ticks

(defn current-time-ms []
  (.getTime (js/Date.)))

(defonce timer-did-start
         (do
           ((fn request-frame []
              (swap! app-state-atom game/advance-to-time (current-time-ms))
              (js/requestAnimationFrame request-frame)))
           true))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
