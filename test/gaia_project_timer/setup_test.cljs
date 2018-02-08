(ns gaia-project-timer.setup-test
  (:require
    [cljs.test :refer-macros [deftest testing is]]
    [gaia-project-timer.setup :as setup]
    [gaia-project-timer.factions :as factions]
    [gaia-project-timer.test-formatter]))

; Test data

(def witches {:faction :witches :color :green})
(def auren {:faction :auren :color :green})
(def dwarves {:faction :dwarves :color :gray})
(def nomads {:faction :nomads :color :yellow})
(def darklings {:faction :darklings :color :black})
(def ice-maidens {:faction :ice-maidens :color :white})
(defn shapeshifters [color]
  {:faction :shapeshifters :color color})
(defn riverwalkers [color]
  {:faction :riverwalkers :color color})

; Tests

(deftest test-initial-state
  (testing "It should initialize with all factions unchosen"
    (is (= setup/initial-state {:mode    :setup
                                :players [nil nil nil nil nil]}))))

(deftest test-set-faction
  (testing "It should set faction and color for non-variable factions"
    (let [state (setup/set-faction setup/initial-state 1 :witches)
          expected {:mode    :setup
                    :players [nil witches nil nil nil]}]
      (is (= state expected))))
  (testing "It should set default color for variable factions"
    (let [state (setup/set-faction setup/initial-state 2 :shapeshifters)
          expected {:mode    :setup
                    :players [nil
                              nil
                              (shapeshifters (first factions/variable-faction-colors))
                              nil
                              nil]}]
      (is (= state expected))))
  (testing "It should set value to nil if faction is nil"
    (let [initial-state {:mode    :setup
                         :players [witches dwarves nil nil nil]}
          updated-state (setup/set-faction initial-state 0 nil)
          expected-state {:mode    :setup
                          :players [nil dwarves nil nil nil]}]
      (is (= updated-state expected-state)))))

(deftest test-set-color
  (testing "Simple case"
    (let [initial-state {:mode    :setup
                         :players [witches (shapeshifters :red) nil nil nil]}
          updated-state (setup/set-color initial-state 1 :black)
          expected-state {:mode    :setup
                          :players [witches (shapeshifters :black) nil nil nil]}]
      (is (= updated-state expected-state)))))

(deftest test-get-players
  (testing "It should return players and their colors"
    (let [state {:mode    :setup
                 :players [witches dwarves nomads darklings ice-maidens]}
          players (setup/get-players state)
          expected [witches dwarves nomads darklings ice-maidens]]
      (is (= players expected))))
  (testing "It should omit unchosen players"
    (let [state {:mode    :setup
                 :players [nil witches nil dwarves nil]}
          players (setup/get-players state)
          expected [witches dwarves]]
      (is (= players expected)))))

(deftest test-get-errors
  (testing "It should be empty on valid configuration"
    (let [state {:mode    :setup
                 :players [witches dwarves nil nil nil]}
          errors (setup/get-errors state)]
      (is (empty? errors))))
  (testing "It should report too few players"
    (let [state {:mode    :setup
                 :players [witches nil nil nil nil]}
          errors (setup/get-errors state)
          expected-error {:type :not-enough-players}]
      (is (some #{expected-error} errors))))
  (testing "It should report multiple players with same color"
    (let [state {:mode    :setup
                 :players [witches auren dwarves nil (shapeshifters :green)]}
          errors (setup/get-errors state)
          expected-error {:type    :same-color
                          :color   :green
                          :indices [0 1 4]}]
      (is (some #{expected-error} errors))))
  (testing "It should report multiple groups of duplicate colors"
    (let [state {:mode    :setup
                 :players [witches dwarves auren (shapeshifters :gray) nil]}
          errors (setup/get-errors state)
          expected-errors [{:type    :same-color
                            :color   :green
                            :indices [0 2]}
                           {:type    :same-color
                            :color   :gray
                            :indices [1 3]}]]
      (is (every? #(some #{%} errors) expected-errors))))
  (testing "It should report multiple players in variable factions"
    (let [state {:mode :setup
                 :players [(shapeshifters :green) (riverwalkers :red) witches nil nil]}
          errors (setup/get-errors state)
          expected-error {:type :multiple-variable
                          :indices [0 1]}]
      (is (some #{expected-error} errors)))))
()
