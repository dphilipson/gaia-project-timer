(ns timer-mystica.setup
  (:require
    [timer-mystica.factions :as factions]))

(def max-players 5)

(def initial-state
  {:mode    :setup
   :players (vec (repeat max-players nil))})

(defn set-faction [state index faction]
  (if (nil? faction)
    (assoc-in state [:players index] nil)
    (let [color (if (factions/variable? faction)
                  (first factions/variable-faction-colors)
                  (factions/color faction))]
      (assoc-in state [:players index] {:faction faction
                                        :color   color}))))

(defn set-color [state index color]
  (assoc-in state [:players index :color] color))

(defn get-players [state]
  (filter identity (:players state)))

(defn validate-player-count [state]
  (when (< (count (get-players state)) 2)
    {:type :not-enough-players}))

(defn validate-no-color-duplicates [state]
  (->> (range max-players)
       (group-by #(get-in state [:players % :color]))
       (filter (fn [[color indices]]
                 (and color (> (count indices) 2))))
       (map (fn [[color indices]]
              {:type    :same-color
               :color   color
               :indices indices}))))

(defn validate-no-multiple-variable-factions [state]
  (let [index-is-variable? #(when-let [faction (get-in state [:players % :faction])]
                             (factions/variable? faction))
        variable-players (filter index-is-variable? (range max-players))]
    (when (> (count variable-players) 1)
      {:type :multiple-variable
       :indices variable-players})))

(defn get-errors [state]
  (->> [validate-player-count validate-no-color-duplicates validate-no-multiple-variable-factions]
       (map #(% state))
       (flatten)
       (filter identity)
       vec))