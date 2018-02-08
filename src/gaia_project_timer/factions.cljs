(ns gaia-project-timer.factions
  (:require
    [clojure.string :as str]))

(def all
  [:terrans :lantids
   :xenos :gleens
   :taklons :ambas
   :hadsch-hallas :ivits
   :geodens :bal-t'aks
   :firaks :bescods
   :nevals :itars])

(def color
  {:terrans :blue
   :lantids :blue
   :xenos :yellow
   :gleens :yellow
   :taklons :brown
   :ambas :brown
   :hadsch-hallas :red
   :ivits :red
   :geodens :orange
   :bal-t'aks :orange
   :firaks :gray
   :bescods :gray
   :nevals :white
   :itars :white})

(def variable-faction-colors
  [:blue :yellow :brown :red :orange :gray :white])

(defn title [faction]
  (->>
    (-> faction name (str/split "-"))
    (map str/capitalize)
    (str/join " ")))

(defn from-title [title]
  (-> title
      str/lower-case
      (str/replace " " "-")
      keyword))

(defn variable? [faction]
  (= (color faction) :variable))
