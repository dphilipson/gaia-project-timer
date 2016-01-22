(ns timer-mystica.factions
  (:require
    [clojure.string :as str]))

(def all
  [:auren :witches :alchemists :darklings :halflings :cultists
   :engineers :dwarves :mermaids :swarmlings :chaos-magicians :giants
   :fakirs :nomads :ice-maidens :yetis :dragonlords :acolytes
   :shapeshifters :riverwalkers])

(def color
  {:auren           :green
   :witches         :green
   :alchemists      :black
   :darklings       :black
   :halflings       :brown
   :cultists        :brown
   :engineers       :gray
   :dwarves         :gray
   :mermaids        :blue
   :swarmlings      :blue
   :chaos-magicians :red
   :giants          :red
   :fakirs          :yellow
   :nomads          :yellow
   :ice-maidens     :white
   :yetis           :white
   :dragonlords     :orange
   :acolytes        :orange
   :shapeshifters   :variable
   :riverwalkers    :variable})

(def variable-faction-colors
  [:green :black :brown :gray :blue :red :yellow])

(defn title [faction]
  (->>
    (-> faction name (str/split "-"))
    (map str/capitalize)
    (str/join " ")))

(defn variable? [faction]
  (= (color faction) :variable))