(ns ^:figwheel-always timer-mystica.core-test
  (:require
    [cljs.test :refer-macros [deftest testing is]]
    [figwheel.client :as fw]
    [timer-mystica.core :as core]
    [timer-mystica.test-formatter]))

(deftest always-passes
  (is (= (+ 1 1) 2)))



(defn run-tests []
  (.clear js/console)
  (cljs.test/run-all-tests #"timer-mystica.*-test"))

(run-tests)

;; FW connection is optional in order to simply run tests,
;; but is needed to connect to the FW repl and to allow
;; auto-reloading on file-save
(fw/start {:websocket-url "ws://localhost:3449/figwheel-ws"
           :build-id "test"})
