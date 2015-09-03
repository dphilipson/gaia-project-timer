(ns timer-mystica.core-test
  (:require
    [cljs.test :refer-macros [deftest testing is]]
    [timer-mystica.core :as core]))

(deftest always-passes
  (is (= (+ 1 1) 2)))