(ns timer-mystica.component-helpers
  (:require
    [reagent.core :as r]
    [clojure.string :as str]))

(defn class-string [classes]
  (->> classes (map name) (str/join " ")))

(def css-transition-group
  (r/adapt-react-class js/React.addons.CSSTransitionGroup))
