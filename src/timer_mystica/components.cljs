(ns timer-mystica.components
  (:require
    [timer-mystica.setup-components :as setup]
    [timer-mystica.game-components :as game]))

(defn main [app-state-atom actions]
  (let [{:keys [mode] :as state} @app-state-atom]
    (case mode
      :setup [setup/main (:on-start-game actions)]
      :game [game/main state actions])))
