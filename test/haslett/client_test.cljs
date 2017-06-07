(ns haslett.client-test
  (:require [cljs.test :refer-macros [deftest is testing async]]
            [cljs.core.async :as a :refer [<! >!]]
            [haslett.client :as haslett])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(deftest test-ws
  (testing "defaults"
    (async done
      (go (let [{:keys [sink source]} (<! (haslett/websocket "ws://echo.websocket.org"))]
            (>! sink {:hello "World"})
            (is (= (<! source) {:hello "World"}))
            (done)))))

  (testing "json formatting"
    (async done
      (go (let [{:keys [sink source]}
                (<! (haslett/websocket "ws://echo.websocket.org"
                                       {:source (haslett/json-source)
                                        :sink   (haslett/json-sink)}))]
            (>! sink {:hello "World"})
            (is (= (<! source) {"hello" "World"}))
            (done))))))
