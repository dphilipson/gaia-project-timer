(ns ^:figwheel-always timer-mystica.core-test
  (:require
    [cljs.test :refer-macros [deftest testing is]]
    [figwheel.client :as fw]
    [timer-mystica.core :as tm]
    [timer-mystica.test-formatter]))

; Test data

(def witches {:faction :witches :time-used-ms 111111})
(def dwarves {:faction :dwarves :time-used-ms 222222})
(def nomads {:faction :nomads :time-used-ms 333333})
(def darklings {:faction :darklings :time-used-ms 444444})
(def ice-maidens {:faction :ice-maidens :time-used-ms 55555})

(def base-state {:current-player  witches
                 :active-players  []
                 :passed-players  []
                 :round           2
                 :between-rounds? false})

(defn make-state [& kvs]
  (merge base-state (apply hash-map kvs)))

(deftest test-player-selected-next
  (testing "Simple case"
    (let [initial-state (make-state :current-player witches
                                    :active-players [dwarves nomads darklings])
          updated-state (tm/player-selected-next initial-state)
          expected-state (make-state :current-player dwarves
                                     :active-players [nomads darklings witches])]
      (is (= updated-state expected-state))))
  (testing "Last player in round"
    (let [initial-state (make-state :current-player witches
                                    :active-players []
                                    :passed-players [dwarves nomads darklings])
          updated-state (tm/player-selected-next initial-state)]
      (is (= updated-state initial-state)))))

(deftest test-player-selected-pass
  (testing "Simple case"
    (let [initial-state (make-state :current-player witches
                                    :active-players [dwarves nomads]
                                    :passed-players [darklings ice-maidens])
          updated-state (tm/player-selected-pass initial-state)
          expected-state (make-state :current-player dwarves
                                     :active-players [nomads]
                                     :passed-players [darklings ice-maidens witches])]
      (is (= updated-state expected-state))))
  (testing "Last player passes"
    (let [initial-state (make-state :current-player witches
                                    :active-players []
                                    :passed-players [dwarves nomads darklings]
                                    :round 2
                                    :between-rounds? false)
          updated-state (tm/player-selected-pass initial-state)
          expected-state (make-state :current-player dwarves
                                     :active-players [nomads darklings witches]
                                     :passed-players []
                                     :round 3
                                     :between-rounds? true)]
      (is (= updated-state expected-state)))))

(deftest test-advance-time
  (testing "Add to current player when in play"
    (let [initial-state (make-state :current-player {:faction :witches :time-used-ms 300}
                                    :between-rounds? false)
          updated-state (tm/advance-time initial-state 200)
          expected-state (make-state :current-player {:faction :witches :time-used-ms 500})]
      (is (= updated-state expected-state))))
  (testing "Don't add to time when between rounds"
    (let [initial-state (make-state :current-player {:faction :witches :time-used-ms 300}
                                    :between-rounds? true)
          updated-state (tm/advance-time initial-state 200)]
      (is (= updated-state initial-state)))))

(deftest test-start-round
  (let [initial-state (make-state :between-rounds? true)
        updated-state (tm/start-round initial-state)
        expected-state (make-state :between-rounds? false)]
    (is (= updated-state expected-state))))

(defn run-tests []
  (.clear js/console)
  (cljs.test/run-all-tests #"timer-mystica.*-test"))

(run-tests)

;; FW connection is optional in order to simply run tests,
;; but is needed to connect to the FW repl and to allow
;; auto-reloading on file-save
(fw/start {:websocket-url "ws://localhost:3449/figwheel-ws"
           :build-id      "test"})
