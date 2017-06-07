(ns haslett.client-test
  (:require [cljs.test :refer-macros [deftest is testing async]]
            [cljs.core.async :as a :refer [<! >!]]
            [haslett.client :as ht])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(deftest test-ws
  (testing "defaults"
    (async done
      (go (let [socket (ht/websocket "ws://echo.websocket.org")
                stream (<! (ht/connect socket))]
            (>! (:sink stream) {:hello "World"})
            (is (= (<! (:source stream)) {:hello "World"}))
            (ht/close socket)
            (done)))))

  (testing "transit formatting"
    (async done
      (go (let [socket (ht/websocket "ws://echo.websocket.org")
                stream (<! (ht/connect socket (ht/transit-source) (ht/transit-sink)))]
            (>! (:sink stream) {:hello "World"})
            (is (= (<! (:source stream)) {:hello "World"}))
            (ht/close socket)
            (done)))))

  (testing "json formatting"
    (async done
      (go (let [socket (ht/websocket "ws://echo.websocket.org")
                stream (<! (ht/connect socket (ht/json-source) (ht/json-sink)))]
            (>! (:sink stream) {:hello "World"})
            (is (= (<! (:source stream)) {"hello" "World"}))
            (ht/close socket)
            (done))))))
