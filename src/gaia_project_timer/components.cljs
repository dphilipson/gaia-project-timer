(ns gaia-project-timer.components
  (:require
    [gaia-project-timer.setup-components :as setup]
    [gaia-project-timer.game-components :as game]))

(defn main [app-state-atom actions]
  (let [{:keys [mode] :as state} @app-state-atom]
    (case mode
      :setup [setup/main state actions]
      :game [game/main state actions])))
