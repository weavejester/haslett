(ns haslett.client-test
  (:require [cljs.test :refer-macros [deftest is testing async]]
            [cljs.core.async :as a :refer [<! >!]]
            [haslett.client :as ws]
            [haslett.format :as fmt])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(deftest test-ws
  (testing "defaults"
    (async done
      (go (let [stream (<! (ws/connect "ws://echo.websocket.org"))]
            (>! (:sink stream) "Hello World")
            (is (= (<! (:source stream)) "Hello World"))
            (ws/close stream)
            (done)))))

  (testing "transit formatting"
    (async done
      (go (let [stream  (<! (ws/connect "ws://echo.websocket.org" {:format fmt/transit}))]
            (>! (:sink stream) {:hello "World"})
            (is (= (<! (:source stream)) {:hello "World"}))
            (ws/close stream)
            (done)))))

  (testing "edn formatting"
    (async done
      (go (let [stream  (<! (ws/connect "ws://echo.websocket.org" {:format fmt/edn}))]
            (>! (:sink stream) {:hello "World"})
            (is (= (<! (:source stream)) {:hello "World"}))
            (ws/close stream)
            (done)))))

  (testing "json formatting"
    (async done
      (go (let [stream  (<! (ws/connect "ws://echo.websocket.org" {:format fmt/json}))]
            (>! (:sink stream) {:hello "World"})
            (is (= (<! (:source stream)) {"hello" "World"}))
            (ws/close stream)
            (done))))))
