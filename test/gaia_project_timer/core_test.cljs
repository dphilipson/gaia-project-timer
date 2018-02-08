(ns ^:figwheel-always gaia-project-timer.core-test
  (:require
    [cljs.test :refer-macros [deftest testing is]]
    [figwheel.client :as fw]
    [gaia-project-timer.setup-test]
    [gaia-project-timer.game-test]
    [gaia-project-timer.test-formatter]))

;; Test runner

(defn run-tests []
  (.clear js/console)
  (cljs.test/run-all-tests #"gaia-project-timer.*-test"))

(run-tests)

;; FW connection is optional in order to simply run tests,
;; but is needed to connect to the FW repl and to allow
;; auto-reloading on file-save
(fw/start {:websocket-url "ws://localhost:3449/figwheel-ws"
           :build-id      "test"})
