(ns haslett.client-test
  (:require [cljs.test :refer-macros [deftest is testing async]]
            [cljs.core.async :as a :refer [<! >!]]
            [haslett.client :as ws])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(deftest test-ws
  (testing "defaults"
    (async done
      (go (let [stream (<! (ws/connect "ws://echo.websocket.org"))]
            (>! (:sink stream) {:hello "World"})
            (is (= (<! (:source stream)) {:hello "World"}))
            (ws/close stream)
            (done)))))

  (testing "edn formatting"
    (async done
      (go (let [options {:source (ws/edn-source) :sink (ws/edn-sink)}
                stream  (<! (ws/connect "ws://echo.websocket.org" options))]
            (>! (:sink stream) {:hello "World"})
            (is (= (<! (:source stream)) {:hello "World"}))
            (ws/close stream)
            (done)))))

  (testing "json formatting"
    (async done
      (go (let [options {:source (ws/json-source) :sink (ws/json-sink)}
                stream  (<! (ws/connect "ws://echo.websocket.org" options))]
            (>! (:sink stream) {:hello "World"})
            (is (= (<! (:source stream)) {"hello" "World"}))
            (ws/close stream)
            (done))))))
