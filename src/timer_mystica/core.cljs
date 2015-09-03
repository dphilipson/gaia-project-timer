(ns ^:figwheel-always timer-mystica.core
  (:require
    [reagent.core :as r]
    [timer-mystica.components :as components]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(def test-data
  {:current-player {:faction :witches :time-used-ms 301000}
   :active-players [{:faction :dwarves :time-used-ms 128000}
                    {:faction :nomads :time-used-ms 777000}]
   :passed-players [{:faction :darklings :time-used-ms 123000}
                    {:faction :ice-maidens :time-used-ms 345000}]})

(defonce app-state (r/atom test-data))

(r/render-component [components/main app-state]
                    (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )

